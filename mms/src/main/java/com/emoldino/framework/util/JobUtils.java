package com.emoldino.framework.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.transaction.Transactional.TxType;

import org.apache.commons.lang3.ClassUtils;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;

import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.dto.NamedData;
import com.emoldino.framework.exception.LogicException;
import com.emoldino.framework.exception.SysException;
import com.emoldino.framework.repository.cachedata.CacheData;
import com.emoldino.framework.repository.cachedata.CacheDataRepository;
import com.emoldino.framework.repository.cachedata.QCacheData;
import com.emoldino.framework.repository.jobdata.JobData;
import com.emoldino.framework.repository.jobdata.JobDataRepository;
import com.emoldino.framework.repository.jobdata.QJobData;
import com.querydsl.core.BooleanBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import saleson.common.util.SecurityUtils;

@Slf4j
public class JobUtils {

	@Data
	public static class JobOptions {
		/**
		 * Maximum Thread Size for Run Concurrently Method
		 */
		@Accessors(chain = true)
		private int maxThreadSize = 10;

		/**
		 * If accessSummaryLogEnabled=true, AccessSummaryLog will be left in DB table.
		 */
		@Accessors(chain = true)
		private boolean accessSummaryLogEnabled = false;

		/**
		 * If clustered=true, The job logic inside will be run only once at the same time.
		 */
		@Accessors(chain = true)
		private boolean clustered = false;

		@Accessors(chain = true)
		private boolean onceOnly = false;
	}

	/**
	 * Jobs will be run concurrently in the options.maxThreadSize number of threads.
	 * <pre>
		List<ClosureWrapper<List<GetABCDEOut>>> jobs = new ArrayList<>();
		jobs.add(;
		Map<String, List<GetABCDEOut>> data = JobUtils.runConcurrently("Runs.getABCDE", new JobOptions().setAccessSummaryLogEnabled(true), 
			new ClosureWrapper<>("getA", () -> getA()),
			new ClosureWrapper<>("getB", () -> getB()),
			new ClosureWrapper<>("getC", () -> getC()),
			new ClosureWrapper<>("getD", () -> getD()),
			new ClosureWrapper<>("getE", () -> getE())
		);
	 * </pre>
	 * @param <T>
	 * @param prefix	prefix of taskName
	 * @param options
	 * @param wrappers	Wrapped jobs
	 * @return
	 */
	public static <T> Map<String, T> runConcurrently(String prefix, JobOptions options, @SuppressWarnings("unchecked") ClosureWrapper<T>... wrappers) {
		return runConcurrently(prefix, options, Arrays.asList(wrappers));
	}

	/**
	 * Jobs will be run concurrently in the options.maxThreadSize number of threads.
	 * <pre>
		List<ClosureWrapper<List<GetABCDEOut>>> jobs = new ArrayList<>();
		jobs.add(new ClosureWrapper<>("getA", () -> getA());
		jobs.add(new ClosureWrapper<>("getB", () -> getB());
		jobs.add(new ClosureWrapper<>("getC", () -> getC());
		jobs.add(new ClosureWrapper<>("getD", () -> getD());
		jobs.add(new ClosureWrapper<>("getE", () -> getE());
		Map<String, List<GetABCDEOut>> data = JobUtils.runConcurrently("Runs.getABCDE", new JobOptions().setAccessSummaryLogEnabled(true), jobs);
	 * </pre>
	 * @param <T>
	 * @param prefix	prefix of taskName
	 * @param options
	 * @param wrappers	Wrapped jobs
	 * @return
	 */
	public static <T> Map<String, T> runConcurrently(String prefix, JobOptions options, List<ClosureWrapper<T>> wrappers) {
		if (ObjectUtils.isEmpty(wrappers)) {
			return Collections.emptyMap();
		}

		ExecutorService executor = newExecutor(Math.min(options.maxThreadSize, wrappers.size()));
		try {
			HttpUtils.getRequestStr();
			HttpUtils.getReferer();
			HttpUtils.getParamsStr();
			SecurityUtils.getAuthentication();
			Map<String, Object> props = new HashMap<>(ThreadUtils.getProps());

			List<Future<NamedData<T>>> futures = wrappers.stream().map(closure -> {
				String name = closure.getName();
				String taskName = prefix + "." + name;
				return executor.submit(() -> JobUtils.run(taskName, options, () -> ThreadUtils.doScopeSupports("JobUtils.run", () -> {
					ThreadUtils.putProps(props);
					return new NamedData<T>(name, closure.getClosure().execute());
				})));
			}).collect(Collectors.toList());

			Map<String, T> results = futures.stream().map(future -> {
				try {
					return future.get();
				} catch (InterruptedException e) {
					log.warn(e.getMessage(), e);
					Thread.currentThread().interrupt();
					return null;
				} catch (ExecutionException e) {
					throw new RuntimeException(e);
				}
			}).collect(Collectors.toMap(NamedData::getName, NamedData::getData, (o1, o2) -> o1, LinkedHashMap::new));
			return results;

		} finally {
			executor.shutdown();
		}
	}

	private static ExecutorService newExecutor(int maxThreadSize) {
		ExecutorService executor = Executors.newFixedThreadPool(maxThreadSize);
		SecurityContext context = SecurityContextHolder.getContext();
		if (context != null) {
			executor = new DelegatingSecurityContextExecutorService(executor, context);
		}
		return executor;
	}

	/**
	 * Run named Job
	 * @param name		Name of Job
	 * @param closure	Job
	 */
	public static void run(String name, ClosureNoReturn closure) {
		run(name, () -> {
			closure.execute();
			return null;
		});
	}

	private static final JobOptions defaultOptions = new JobOptions();

	/**
	 * Run named Job
	 * @param <T>
	 * @param name		Name of Job
	 * @param closure	Job
	 * @return	Return of Job
	 */
	public static <T> T run(String name, Closure<T> closure) {
		return run(name, defaultOptions, closure);
	}

	/**
	 * Run named Job
	 * @param <T>
	 * @param name		Name of Job
	 * @param options
	 * @param closure	Job
	 * @return	Return of Job
	 */
	public static <T> T run(String name, JobOptions options, Closure<T> closure) {
		LogicUtils.assertNotEmpty(name, "name");
		log.info(name + " job starts!");
		return ThreadUtils.doScopeSupports("JobUtils.run", () -> {
			Job job = new Job();
			Throwable t = null;
			try {
				return closure.execute();
			} catch (RuntimeException e) {
				t = e;
				throw e;
			} finally {
				jobEnds(name, job, options, t);
			}
		});
	}

	/**
	 * Run named Job (Failure(Error Logged in DB table) or If same named job is already running in other thread, skip.)
	 * @param name		Name of Job
	 * @param closure	Job
	 */
	public static boolean runIfNotRunningQuietly(String name, ClosureNoReturn closure) {
		return runIfNotRunningQuietly(name, defaultOptions, closure);
	}

	/**
	 * Run named Job (Failure(Error Logged in DB table) or If same named job is already running in other thread, skip.)
	 * @param name		Name of Job
	 * @param options
	 * @param closure	Job
	 */
	public static boolean runIfNotRunningQuietly(String name, JobOptions options, ClosureNoReturn closure) {
		boolean done = ThreadUtils.doScopeSupports(name, () -> {
			try {
				return runIfNotRunning(name, options, closure);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
				LogUtils.saveErrorQuietly(LogUtils.getErrorType(e), "JOB_FAIL", HttpUtils.getStatus(e, null), "Job Fail at " + closure.getClass().getName(), e);
				return false;
			}
		});
		return done;
	}

	private static Map<String, Job> jobs = new ConcurrentHashMap<>();

	private static String toJobNameByClustered(String name, boolean clustered) {
		return clustered ? ("j." + name) : name;
	}

	private static boolean isRunningAtOtherCluster(String name) {
		String jobName = toJobNameByClustered(name, true);
		String serverName = ServerUtils.getName();
		return TranUtils.doNewTran(() -> isRunningAtOtherClusterByJobName(jobName, serverName));
	}

	private static boolean isRunningAtOtherClusterByJobName(String jobName, String serverName) {
		return CacheDataUtils.exists(jobName) && !CacheDataUtils.exists(jobName, serverName);
	}

	private static final String CLUSTER_PROP = "JobUtils.CLUSTER";

	private static boolean firstUpdateCluster = true;

	public static void updateCluster() {
		String clusterName = ServerUtils.getName();

		if (firstUpdateCluster) {
			firstUpdateCluster = false;

			log.info("clean garbage clustered job of cluster: " + clusterName);
			Instant instant = DateUtils2.getInstant().minus(Duration.ofMinutes(5));
			QCacheData table = QCacheData.cacheData;
			TranUtils.doNewTran(() -> {
				BeanUtils.get(CacheDataRepository.class).findAll(new BooleanBuilder()//
						.and(table.cacheName.startsWith("j."))//
						.and(table.cacheKey.eq(clusterName))//
						.and(table.lastUsedAt.lt(instant))//
				).forEach(job -> deleteJob(job.getCacheName(), job.getCacheKey()));
			});
		}

		log.info("update cluster: " + clusterName);
		SyncCtrlUtils.wrapWithLock(CLUSTER_PROP + "." + clusterName, () -> {
			if (CacheDataUtils.exists(CLUSTER_PROP, clusterName)) {
				CacheDataUtils.update(CLUSTER_PROP, clusterName);
			} else {
				CacheDataUtils.create(CLUSTER_PROP, clusterName, null);
			}
		});

	}

	public static void cleanGarbageClusters() {
		QCacheData table = QCacheData.cacheData;

		String clusterName = ServerUtils.getName();

		log.info("clean garbage clusters");
		{
			Instant instant = DateUtils2.getInstant().minus(Duration.ofMinutes(20));
			TranUtils.doNewTran(() -> {
				return BeanUtils.get(CacheDataRepository.class).findAll(new BooleanBuilder()//
						.and(table.cacheName.eq(CLUSTER_PROP))//
						.and(table.cacheKey.ne(clusterName))//
						.and(table.lastUsedAt.lt(instant))//
				);
			}).forEach(item -> {
				SyncCtrlUtils.wrapWithLock(CLUSTER_PROP + "." + item.getCacheKey(), () -> {
					CacheData cluster = BeanUtils.get(CacheDataRepository.class).findOne(new BooleanBuilder()//
							.and(table.cacheName.eq(CLUSTER_PROP))//
							.and(table.cacheKey.eq(item.getCacheKey()))//
					).orElse(null);
					if (cluster == null || cluster.getLastUsedAt().compareTo(instant) >= 0) {
						return;
					}

					log.info("clean garbage cluster: " + cluster.getCacheKey());
					BeanUtils.get(CacheDataRepository.class).findAll(new BooleanBuilder()//
							.and(table.cacheName.startsWith("j."))//
							.and(table.cacheKey.eq(cluster.getCacheKey()))//
							.and(table.lastUsedAt.lt(cluster.getLastUsedAt().plus(Duration.ofMinutes(2))))//
					).forEach(job -> deleteJob(job.getCacheName(), job.getCacheKey()));
					BeanUtils.get(CacheDataRepository.class).delete(cluster);
				});
			});
		}

		log.info("clean garbage batch jobs");
		{
			Instant instant = DateUtils2.getInstant().minus(Duration.ofDays(2));
			TranUtils.doNewTran(() -> {
				BooleanBuilder filter = new BooleanBuilder()//
						.and(table.cacheName.startsWith("j."))//
						.and(table.lastUsedAt.lt(instant));
				return BeanUtils.get(CacheDataRepository.class).findAll(filter);
			}).forEach(batch -> {
				LogUtils.saveErrorQuietly(ErrorType.SYS, "LONG_GARBAGE_BATCH", // 
						HttpStatus.SERVICE_UNAVAILABLE, batch.getCacheName() + " from " + batch.getCacheKey() + " ", clusterName);
				TranUtils.doNewTran(() -> BeanUtils.get(CacheDataRepository.class).delete(batch));
			});
		}
	}

	/**
	 * Run named Job (If same named job is already running in other thread, skip.)
	 * @param name		Name of Job
	 * @param options
	 * @param closure	Job
	 */
	public static boolean runIfNotRunning(String name, JobOptions options, ClosureNoReturn closure) {
		boolean done = ThreadUtils.doScopeSupports(name, () -> {
			LogicUtils.assertNotEmpty(name, "name");

			boolean onceOnly = options != null && options.isOnceOnly();
			if (onceOnly && TranUtils.doNewTran(() -> CacheDataUtils.exists("JobOnceOnly", name))) {
				return false;
			}

			boolean clustered = options != null && options.isClustered();

			String jobName = toJobNameByClustered(name, clustered);

			Job prevJob = SyncCtrlUtils.wrap(jobName, () -> {
				if (jobs.containsKey(jobName)) {
					return jobs.get(jobName);
				}
				jobs.put(jobName, new Job());
				return null;
			});
			if (prevJob != null) {
				log.info(jobName + " job is already running. So skipped! preivios job started " + getElapsedTimeMillis(prevJob) + " millis ago");
				return false;
			}

			try {
				if (clustered) {
					try {
						boolean runnable = TranUtils.doNewTran(() -> {
							SyncCtrlUtils.lock(jobName);
							String clusterName = ServerUtils.getName();
							if (isRunningAtOtherClusterByJobName(jobName, clusterName)) {
								log.info(jobName + " job is already running from other cluster. So skipped!");
								return false;
							}
							if (CacheDataUtils.exists(jobName, clusterName)) {
								CacheDataUtils.update(jobName, clusterName);
							} else {
								CacheDataUtils.create(jobName, clusterName, null);
							}
							if (CacheDataUtils.exists(jobName, "one")) {
								CacheDataUtils.update(jobName, "one");
							} else {
								CacheDataUtils.create(jobName, "one", null);
							}
							return true;
						});
						if (!runnable) {
							return false;
						}
					} catch (Exception e) {
						log.info(jobName + " job seems already running from other cluster. So skipped!");
						return false;
					}

					if (onceOnly && TranUtils.doNewTran(() -> CacheDataUtils.exists("JobOnceOnly", name))) {
						return false;
					}
				}

				Throwable t = null;
				try {
					log.info(jobName + " job starts!");
					ThreadUtils.doScopeSupports("JobUtils.runIfNotRunning", () -> closure.execute());

					if (onceOnly) {
						TranUtils.doNewTran(() -> CacheDataUtils.save("JobOnceOnly", name, null));
					}

				} catch (RuntimeException e) {
					t = e;
					throw e;

				} finally {
					if (clustered) {
						String clusterName = ServerUtils.getName();
						TranUtils.doNewTran(() -> deleteJob(jobName, clusterName));
					}

					Job job = jobs.remove(jobName);
					jobEnds(jobName, job, options, t);
				}

			} finally {
				if (jobs.containsKey(jobName)) {
					jobs.remove(jobName);
				}
			}

			return true;
		});
		return done;
	}

	private static void deleteJob(String jobName, String clusterName) {
		CacheDataUtils.delete(jobName, clusterName);
		CacheDataUtils.delete(jobName, "one");
	}

	private static void jobEnds(String name, Job job, JobOptions options, Throwable t) {
		long elapsedTimeMillis = getElapsedTimeMillis(job);
		log.info(name + " job ends! elapsedTimeMillis: " + elapsedTimeMillis);

		if (ConfigUtils.isAccessSummaryLogEnabled() && options != null && options.isAccessSummaryLogEnabled()) {
			LogUtils.putAccessSummaryQuietly(name, elapsedTimeMillis, t);
		}
	}

	private static long getElapsedTimeMillis(Job job) {
		return System.currentTimeMillis() - job.getStartTimeMillis();
	}

	@Data
	private static class Job {
		private long startTimeMillis = System.currentTimeMillis();
	}

	/**
	 * TODO Prove it later
	 * Run JobCalls(call, afterCall, throwsCall) by Thread Pool
	 * @param poolName		Thread Pool Name
	 * @param syncKey		Sequence Order Guarantee Key
	 * @param call			Job to Run
	 * @param beforeCall	Before Job to Run	
	 * @param afterCall 	After Job to Run
	 * @param throwsCall	throws Job to Run
	 */
	protected static void runByThreadPool(String poolName, String syncKey, JobCall call, JobCall beforeCall, JobCall afterCall, JobCall throwsCall) {
		LogicUtils.assertNotEmpty(poolName, "poolName");
		LogicUtils.assertNotEmpty(call, "call");
		LogicUtils.assertNotEmpty(call.getLogicName(), "call.logicName");

		// Without SyncKey
		if (ObjectUtils.isEmpty(syncKey)) {
			runByThreadPool(poolName, null, () -> run(toCallInternal(call), toCallInternal(beforeCall), toCallInternal(afterCall), toCallInternal(throwsCall)));
		}
		// With SyncKey
		else {
			enqueue(poolName, syncKey, call, beforeCall, afterCall, throwsCall);
			runByThreadPool(poolName, syncKey, null);
		}
	}

	public static void enqueue(String poolName, String syncKey, JobCall call, JobCall beforeCall, JobCall afterCall, JobCall throwsCall) {
		enqueue(poolName, syncKey, true, call, beforeCall, afterCall, throwsCall);
	}

	public static void enqueue(String poolName, String syncKey, boolean newTran, JobCall call, JobCall beforeCall, JobCall afterCall, JobCall throwsCall) {
		LogicUtils.assertNotEmpty(poolName, "poolName");
		LogicUtils.assertNotEmpty(syncKey, "syncKey");
		LogicUtils.assertNotEmpty(call, "call");
		LogicUtils.assertNotEmpty(call.getLogicName(), "call.logicName");

		JobData data = new JobData();
		data.setExecutorName(poolName);
		data.setSyncKey(syncKey);
		data.setLogicName(call.getLogicName());
		data.setLogicTxType(call.getTxType() == null ? null : call.getTxType().name());
		if (beforeCall != null) {
			data.setBeforeLogicName(beforeCall.getLogicName());
			data.setBeforeLogicTxType(beforeCall.getTxType() == null ? null : beforeCall.getTxType().name());
		}
		if (afterCall != null) {
			data.setAfterLogicName(afterCall.getLogicName());
			data.setAfterLogicTxType(afterCall.getTxType() == null ? null : afterCall.getTxType().name());
		}
		if (throwsCall != null) {
			data.setThrowsLogicName(throwsCall.getLogicName());
			data.setThrowsLogicTxType(throwsCall.getTxType() == null ? null : throwsCall.getTxType().name());
		}

		JobDetail jobDetail = new JobDetail(call, beforeCall, afterCall, throwsCall);
		String jobDetailStr = ValueUtils.toJsonStr(jobDetail);
		data.setJobDetail(jobDetailStr);

		if (newTran) {
			TranUtils.doNewTran(() -> BeanUtils.get(JobDataRepository.class).save(data));
		} else {
			TranUtils.doTran(() -> BeanUtils.get(JobDataRepository.class).saveAndFlush(data));
		}
	}

	private static void run(JobData data) {
		if (data == null) {
			return;
		}
		try {
			JobCallInternal call;
			JobCallInternal beforeCall;
			JobCallInternal afterCall;
			JobCallInternal throwsCall;
			try {
				JobDetail jobDetail = ValueUtils.fromJsonStr(data.getJobDetail(), JobDetail.class);
				call = toCallInternal(jobDetail.getCall());
				beforeCall = toCallInternal(jobDetail.getBeforeCall());
				afterCall = toCallInternal(jobDetail.getAfterCall());
				throwsCall = toCallInternal(jobDetail.getThrowsCall());
			} catch (Exception e) {
				LogUtils.saveErrorQuietly(ErrorType.LOGIC, "JOB_DETAIL_INVALID", HttpStatus.NOT_ACCEPTABLE, "Failed to deserialize Job Detail", e);
				throw e;
			}
			try {
				run(call, beforeCall, afterCall, throwsCall);
			} catch (Exception e) {
				throw e;
			}
		} finally {
			if (!ObjectUtils.isEmpty(data.getId())) {
				TranUtils.doNewTranQuietly(() -> BeanUtils.get(JobDataRepository.class).delete(data));
			}
		}
	}

	private static JobCallInternal toCallInternal(JobCall call) {
		if (call == null) {
			return null;
		}
		JobCallInternal _call = toCallInternal(call.getLogicName(), call.getTxType(), call.getParams());
		return _call;
	}

	private static JobCallInternal toCallInternal(String name, TxType txType, List<JobCallParam> params) {
		if (ObjectUtils.isEmpty(name)) {
			return null;
		}
		JobCallInternal call = new JobCallInternal();
		call.setLogicName(name);
		call.setParams(params);
		call.setTxType(txType);
		int index = name.lastIndexOf('.');
		String className = name.substring(0, index);
		String methodName = name.substring(index + 1);
		Class<?> beanClass;
		try {
			beanClass = ClassUtils.getClass(className);
			call.setBeanClass(beanClass);
			call.setBean(BeanUtils.get(beanClass));
		} catch (ClassNotFoundException e) {
			throw new LogicException("BEAN_CLASS_NOT_FOUND", e.getMessage(), e);
		}

		Class<?>[] paramTypes;
		if (ObjectUtils.isEmpty(params)) {
			paramTypes = new Class[0];
		} else {
			paramTypes = new Class[params.size()];
			int i = 0;
			for (JobCallParam param : params) {
				Class<?> dataType = param.getDataType();
				paramTypes[i++] = dataType;
				if (dataType == null || param.getValue() == null || dataType.isAssignableFrom(param.getValue().getClass())) {
					continue;
				}
				Object value = ValueUtils.toRequiredType(param.getValue(), dataType);
				param.setValue(value);
			}
		}
		try {
			Method method = beanClass.getMethod(methodName, paramTypes);
			call.setMethod(method);
		} catch (NoSuchMethodException e) {
			throw new LogicException("METHOD_NOT_FOUND", e.getMessage(), e);
		}
		return call;
	}

	private static void run(JobCallInternal call, JobCallInternal beforeCall, JobCallInternal afterCall, JobCallInternal throwsCall) {
		if (call == null) {
			return;
		}
		try {
			if (TxType.REQUIRED.equals(call.getTxType())) {
				if (beforeCall != null && TxType.NEVER.equals(beforeCall.getTxType())) {
					run(beforeCall);
				}
				TranUtils.doTran(() -> {
					if (beforeCall != null && !TxType.NEVER.equals(beforeCall.getTxType())) {
						run(beforeCall);
					}
					run(call);
					if (afterCall != null && !TxType.NEVER.equals(afterCall.getTxType())) {
						run(afterCall);
					}
				});
				if (afterCall != null && TxType.NEVER.equals(call.getTxType())) {
					run(afterCall);
				}

			} else {
				run(beforeCall, null, null, null);
				if (call.getTxType() == null || TxType.NEVER.equals(call.getTxType())) {
					TranUtils.doNeverTran(() -> run(call));
				} else if (TxType.REQUIRES_NEW.equals(call.getTxType())) {
					TranUtils.doNewTran(() -> run(call));
				} else {
					run(call);
				}
				run(afterCall, null, null, null);
			}
		} catch (Exception e) {
			if (throwsCall == null) {
				return;
			} else if (!TxType.NEVER.equals(throwsCall.getTxType())) {
				TranUtils.doNewTran(() -> run(throwsCall, e));
			} else {
				run(throwsCall, e);
			}
			throw e;
		}
	}

	private static void run(JobCallInternal call) {
		run(call, null);
	}

	private static void run(JobCallInternal call, Throwable th) {
		if (call == null) {
			return;
		}
		LogicUtils.assertNotEmpty(call.getMethod(), "call.method");
		try {
			List<Object> args = new ArrayList<>();
			if (!ObjectUtils.isEmpty(call.getParams())) {
				call.getParams().forEach(param -> {
					if (th != null && Throwable.class.isAssignableFrom(param.getDataType())) {
						args.add(th);
					} else {
						args.add(param.getValue());
					}
				});
			}
			call.getMethod().invoke(call.getBean(), args.toArray());
		} catch (IllegalAccessException e) {
			throw new LogicException("METHOD_ILLEGAL_ACCESS", e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new LogicException("METHOD_ILLEGAL_ARGUMENT", e.getMessage(), e);
		} catch (InvocationTargetException e) {
			Throwable t = ValueUtils.unwrap(e);
			throw ValueUtils.toAe(t, null);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class JobDetail {
		private JobCall call;
		private JobCall beforeCall;
		private JobCall afterCall;
		private JobCall throwsCall;
	}

	@Data
	public static class JobCall {
		private String logicName;
		private TxType txType;
		private List<JobCallParam> params = new ArrayList<>();
	}

	@Data
	public static class JobCallInternal {
		private String logicName;
		private TxType txType;
		private List<JobCallParam> params = new ArrayList<>();
		private Class<?> beanClass;
		private Object bean;
		private Method method;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class JobCallParam {
		private String name;
		private Class<?> dataType;
		private Object value;
	}

	public static void runByThreadPoolAll(String poolName) {
		LogicUtils.assertNotEmpty(poolName, "poolName");

		String cacheName = "batch." + poolName;
		String clusterName = ServerUtils.getName();

		TranUtils.doNewTran(() -> {
			if (CacheDataUtils.exists(cacheName, clusterName)) {
				CacheDataUtils.update(cacheName, clusterName);
			} else {
				CacheDataUtils.create(cacheName, clusterName, null);
			}
		});

		try {
			Page<JobData> page;
			QJobData table = QJobData.jobData;
			int counter = 0;
			long[] id = { 0 };
			while (counter++ < 1000 //
					&& !(page = TranUtils.doNewTran(() -> BeanUtils.get(JobDataRepository.class).findAll(//
							new BooleanBuilder()//
									.and(table.executorName.eq(poolName))//
									.and(table.id.gt(id[0])), //
							PageRequest.of(0, 10, Direction.ASC, "id")//
					))).isEmpty()) {
				page.forEach(jobData -> {
					id[0] = jobData.getId();
					runByThreadPool(poolName, jobData.getSyncKey());
				});
			}
		} finally {
			TranUtils.doNewTran(() -> CacheDataUtils.delete(cacheName, clusterName));
		}
	}

	public static void runByThreadPool(String poolName, String syncKey) {
		LogicUtils.assertNotEmpty(poolName, "poolName");
		LogicUtils.assertNotEmpty(syncKey, "syncKey");
		String name = poolName + "." + syncKey;
		if (isRunningAtOtherCluster(name)) {
			return;
		}
		runByThreadPool(poolName, syncKey, null);
	}

	public static void runByThreadPool(String poolName, final ClosureNoReturn closure) {
		if (closure == null) {
			return;
		}
		runByThreadPool(poolName, null, closure);
	}

	/**
	 * Run Logic Block by Thread Pool
	 * @param poolName	Thread Pool Name
	 * @param syncKey	Sequence Order Guarantee Key
	 * @param closure	Logic Block
	 * @return	Return	Whether run or not
	 */
	private static boolean runByThreadPool(String poolName, final String syncKey, final ClosureNoReturn closure) {
		// 1. Control Block
		boolean run = SyncCtrlUtils.wrap("JobUtils.run.theadPool." + poolName, () -> {
			final TE te = getThreadPool(poolName);

			// 2. Sync Block
			{
				if (!syncin(te, syncKey)) {
					return false;
				}

				// 3. Retry Block
				TaskRejectedException tre = null;
				int retryCount = 0;
				do {
					// 4. Thread Pool Block
					try {
						tein(te);
					} catch (Exception e) {
						syncout(te, syncKey);
						throw e;
					}
					try {
						te.taskExecutor.execute(() -> {
							try {
								// Without SyncKey
								if (ObjectUtils.isEmpty(syncKey)) {
									ThreadUtils.doScope("JobUtils.run.threadPool." + te.name, () -> {
										try {
											if (log.isInfoEnabled()) {
												log.info("Run Job at " + toStatusString(te));
											}
											closure.execute();
										} catch (Exception e) {
											LogUtils.saveErrorQuietly(e);
										}
									});
								}
								// With SyncKey
								else {
									BooleanBuilder filter;
									{
										QJobData table = QJobData.jobData;
										filter = new BooleanBuilder();
										filter.and(table.executorName.eq(poolName));
										filter.and(table.syncKey.eq(syncKey));
									}
									String name = poolName + "." + syncKey;
									JobUtils.runIfNotRunningQuietly(name, new JobOptions().setClustered(true), () -> {
										Closure<JobData> getJobData = () -> {
											Page<JobData> page = BeanUtils.get(JobDataRepository.class).findAll(filter, PageRequest.of(0, 1, Direction.ASC, "id"));
											return page.isEmpty() ? null : page.getContent().get(0);
										};
										JobData[] job = { null };
										while ((job[0] = TranUtils.doNewTran(() -> getJobData.execute())) != null) {
											ThreadUtils.doScope("JobUtils.run.threadPool." + te.name, () -> {
												try {
													if (log.isInfoEnabled()) {
														log.info("Run JobData at " + toStatusString(te));
													}
													run(job[0]);
												} catch (Exception e) {
													log.error(e.getMessage(), e);
													LogUtils.saveErrorQuietly(ErrorType.SYS, "SYNC_JOB_FAIL", HttpStatus.INTERNAL_SERVER_ERROR, "Sync Job Fail: " + e.getMessage(),
															e);
												}
											});
										}
									});
								}

							} finally {
								syncout(te, syncKey);
								teout(te);
							}
						});
						tre = null;

					} catch (Exception e) {
						teout(te);
						LogUtils.saveErrorQuietly(e);
						if (e instanceof TaskRejectedException) {
							tre = (TaskRejectedException) e;
							Throwable t = e;
							while (t.getCause() != null) {
								t = t.getCause();
								LogUtils.saveErrorQuietly(t);
							}
						} else {
							syncout(te, syncKey);
							throw e;
						}
					}

				} while (tre != null && retryCount++ < 3);

				if (tre != null) {
					syncout(te, syncKey);
					throw tre;
				}

				return true;
			}
		});

		return run;
	}

	private static boolean syncin(TE te, String syncKey) {
		if (ObjectUtils.isEmpty(syncKey)) {
			return true;
		}
		return SyncCtrlUtils.wrap("JobUtils.syncSet." + te.name, () -> {
			if (te.syncSet.contains(syncKey)) {
				return false;
			}
			te.syncSet.add(syncKey);
			return true;
		});
	}

	private static void syncout(TE te, String syncKey) {
		if (ObjectUtils.isEmpty(syncKey)) {
			return;
		}
		SyncCtrlUtils.wrap("JobUtils.syncSet." + te.name, () -> te.syncSet.remove(syncKey));
	}

	private static class TE {
		String name;
		ThreadPoolTaskExecutor taskExecutor;

		Set<String> syncSet = new HashSet<>();

		public String toString() {
			return name;
		}
	}

	private static Map<String, Object> monitors = new ConcurrentHashMap<>();

	public synchronized static Object getMonitor(String name) {
		Object monitor;
		if (monitors.containsKey(name)) {
			monitor = monitors.get(name);
		} else {
			monitor = new Object();
			monitors.put(name, monitor);
		}
		return monitor;
	}

	private static void tein(TE te) {
		if (te == null) {
			return;
		}

		Object monitor = getMonitor(te.name);

		synchronized (monitor) {
			int activeCount = te.taskExecutor.getActiveCount();
			int maxPoolSize = te.taskExecutor.getMaxPoolSize();

			if (activeCount < maxPoolSize) {
				if (log.isInfoEnabled()) {
					log.info("TE-In " + toStatusString(te));
				}
				return;
			}

			try {
				if (log.isInfoEnabled()) {
					log.info("TE-Wait " + toStatusString(te));
				}
				monitor.wait(600000);
				if (log.isInfoEnabled()) {
					log.info("TE-Release " + toStatusString(te));
				}
			} catch (InterruptedException e) {
				throw new SysException("TE_WAIT_FAIL", e);
			}
		}
	}

	private static void teout(TE te) {
		if (te == null) {
			return;
		}

		Object monitor = getMonitor(te.name);

		synchronized (monitor) {
			if (log.isInfoEnabled()) {
				log.info("TE-Out " + toStatusString(te));
			}
			monitor.notify();
		}
	}

	private static String toStatusString(TE te) {
		if (te == null) {
			return null;
		}
		return "threadPool: " + te.name + ", activeCount:" + te.taskExecutor.getActiveCount() + ", poolSize:" + te.taskExecutor.getPoolSize();
	}

	private static Map<String, TE> TEs = new ConcurrentHashMap<>();

	private static TE getThreadPool(final String name) {
		TE te = SyncCtrlUtils.wrap("JobUtils.getThreadPool." + name, TEs, name, () -> {
			TE e = new TE();
			e.name = name;
			e.taskExecutor = BeanUtils.get(name, ThreadPoolTaskExecutor.class);
			return e;
		});
		return te;
	}

	public synchronized static void setThreadPool(String name, ThreadPoolTaskExecutor threadPool) {
		if (TEs.containsKey(name)) {
			throw new LogicException("TE_REG_DUPLICATED", "The TE already set: " + name);
		}

		TE te = new TE();
		te.name = name;
		te.taskExecutor = threadPool;
		TEs.put(name, te);
	}

}
