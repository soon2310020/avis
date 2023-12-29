package com.emoldino.api.common.resource.composite.schexe.agent;

import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.emoldino.api.analysis.resource.base.data.service.DataService;
import com.emoldino.api.analysis.resource.base.data.service.moldchartstat.MoldChartStatService;
import com.emoldino.api.analysis.resource.base.data.service.moldprocchg.MoldProcChgService;
import com.emoldino.api.analysis.resource.composite.cdtisp.dto.CdtIspAdjustIn;
import com.emoldino.api.analysis.resource.composite.cdtisp.service.adjust.CdtIspAdjust1stGenDataService;
import com.emoldino.api.analysis.resource.composite.cyctimdev.dto.CycTimDevPostIn;
import com.emoldino.api.analysis.resource.composite.cyctimdev.service.CycTimDevService;
import com.emoldino.api.analysis.resource.composite.datcol.service.dist.DatColDistService;
import com.emoldino.api.analysis.resource.composite.datcol.service.resource.DatColResourceService;
import com.emoldino.api.analysis.resource.composite.datcol.service.software.DatColSoftwareService;
import com.emoldino.api.analysis.resource.composite.trscol.service.TrsColService;
import com.emoldino.api.common.resource.base.file.service.FileService;
import com.emoldino.api.common.resource.base.log.service.access.AccessLogService;
import com.emoldino.api.common.resource.base.log.service.error.ErrorLogService;
import com.emoldino.api.common.resource.base.noti.service.email.NotiEmailService;
import com.emoldino.api.common.resource.composite.ipc.dto.IpcAppVersionsPullIn;
import com.emoldino.api.common.resource.composite.ipc.service.IpcService;
import com.emoldino.api.common.resource.composite.mdtadj.service.MdtAdjService;
import com.emoldino.api.integration.kafka.config.KafkaConfig;
import com.emoldino.api.integration.resource.base.ai.dto.AiModelType;
import com.emoldino.api.integration.resource.base.ai.service.AiLaunchService;
import com.emoldino.api.integration.resource.composite.retry.service.AiRetryService;
import com.emoldino.api.supplychain.resource.base.product.service.moldstat.ProdMoldStatService;
import com.emoldino.api.supplychain.resource.base.product.service.stat.ProductStatService;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.CacheDataUtils;
import com.emoldino.framework.util.ConfigUtils;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.SyncCtrlUtils;

import saleson.api.mold.MoldService;

@Profile("!developer")
@EnableScheduling
@Component
public class SchExeAgent {

	/**
	 * Pull Updated App Version Information from Central Server
	 */
	@Scheduled(cron = "0 45 * ? * ?")
	public void pullAppVersionsJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		IpcAppVersionsPullIn input = new IpcAppVersionsPullIn();
		input.setAppCode("MMS");
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(IpcService.class, "pullAppVersions"), () -> BeanUtils.get(IpcService.class).pullAppVersions(input));
	}

	/**
	 * Upload Manageable Resources to Central Server
	 */
	@Scheduled(cron = "0 25 * ? * ?")
	public void uploadResourcesJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(DatColResourceService.class, "upload"), () -> BeanUtils.get(DatColResourceService.class).upload());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 20), initialDelay = (1000 * 60 * 20))
	public void saveAccessSummaryLogBatchJob() {
		if (!ConfigUtils.isAccessSummaryLogEnabled()) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AccessLogService.class, "saveBatch"), () -> BeanUtils.get(AccessLogService.class).saveBatch());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 24), initialDelay = (1000 * 60 * 60 * 24))
	public void cleanOldAccessSummaryLogBatchJob() {
		if (!ConfigUtils.isAccessSummaryLogEnabled()) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AccessLogService.class, "cleanOldBatch"), () -> BeanUtils.get(AccessLogService.class).cleanOldBatch());
	}

	@Scheduled(fixedDelay = (60000), initialDelay = (1000))
	public void uploadClusterJob() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(JobUtils.class, "updateCluster"), () -> JobUtils.updateCluster());
	}

	@Scheduled(fixedDelay = (600000), initialDelay = (5000))
	public void cleanGarbageClustersJob() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(JobUtils.class, "cleanGarbageClusters"), () -> JobUtils.cleanGarbageClusters());
	}

	@Scheduled(cron = "0 20 2 * * *")
	public void cleanFileTmpBatchJob() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(FileService.class, "cleanTmpBatch"), () -> BeanUtils.get(FileService.class).cleanTmpBatch());
	}

	@Scheduled(fixedDelay = 10000, initialDelay = (15000))
	public void distData() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(DatColDistService.class, "dist"), () -> BeanUtils.get(DatColDistService.class).dist());
	}

	@Scheduled(fixedDelay = 600000, initialDelay = (40000))
	public void forwardData() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(DatColDistService.class, "forward"), () -> BeanUtils.get(DatColDistService.class).forward());
	}

	@Scheduled(fixedDelay = (10000), initialDelay = (20000))
	public void runDataTaskBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(JobUtils.class, "runByThreadPoolAll::data"), () -> JobUtils.runByThreadPoolAll("dataTaskExecutor"));
	}

	@Scheduled(cron = "0 40 * * * *")
	public void postCommandsBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(DatColSoftwareService.class, "postCommandsBatch"),
				() -> BeanUtils.get(DatColSoftwareService.class).postCommandsBatch());
	}

	@Scheduled(fixedDelay = (30000), initialDelay = (60000))
	public void runTransferTaskBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(JobUtils.class, "runByThreadPoolAll::transfer"), () -> JobUtils.runByThreadPoolAll("transferTaskExecutor"));
	}

	@Scheduled(fixedDelay = (20000), initialDelay = (20000))
	public void runTransfer2TaskBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(JobUtils.class, "runByThreadPoolAll::transfer2"), () -> JobUtils.runByThreadPoolAll("transfer2TaskExecutor"));
	}

	@Scheduled(fixedDelay = (20000), initialDelay = (40000))
	public void runWoTaskBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(JobUtils.class, "runByThreadPoolAll::wo"), () -> JobUtils.runByThreadPoolAll("woTaskExecutor"));
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 12), initialDelay = (60000))
	public void cleanDataBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(DataService.class, "cleanBatch"), () -> BeanUtils.get(DataService.class).cleanBatch());
	}

	@Scheduled(fixedDelay = (180000), initialDelay = (30000))
	public void adjustDataBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(DataService.class, "adjustBatch"), () -> BeanUtils.get(DataService.class).adjustBatch());
	}

	@Scheduled(cron = "0 0 1 * * *")
	public void adjust1stGenDataBatchJob() {
		if (!"dyson".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(CdtIspAdjust1stGenDataService.class, "adjustBatch"),
				() -> BeanUtils.get(CdtIspAdjust1stGenDataService.class).adjustBatch(new CdtIspAdjustIn()));
	}

	@Scheduled(fixedDelay = (8000), initialDelay = (15000))
	public void runNotiTaskBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(JobUtils.class, "runByThreadPoolAll::noti"), () -> JobUtils.runByThreadPoolAll("notiTaskExecutor"));
	}

	@Scheduled(fixedDelay = (20000), initialDelay = (20000))
	public void runEmailTaskBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(NotiEmailService.class, "sendBatch"), () -> BeanUtils.get(NotiEmailService.class).sendBatch());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 3), initialDelay = (1000 * 60 * 60 * 4))
	public void cleanOldTrsMessageBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(TrsColService.class, "cleanOldMessageBatch"), () -> BeanUtils.get(TrsColService.class).cleanOldMessageBatch());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 5), initialDelay = (1000 * 60 * 60 * 5))
	public void cleanOldErrorLogBatchJob() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(ErrorLogService.class, "cleanOldBatch"), () -> BeanUtils.get(ErrorLogService.class).cleanOldBatch());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 2), initialDelay = (10000))
	public void adjust2HoursBatchJob() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(MdtAdjService.class, "post"), () -> BeanUtils.get(MdtAdjService.class).post());
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(CacheDataUtils.class, "cleanOldBatch"), () -> CacheDataUtils.cleanOldBatch());
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(SyncCtrlUtils.class, "cleanOldDataBatch"), () -> SyncCtrlUtils.cleanOldDataBatch());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60), initialDelay = (80000))
	public void summarizeMoldChartStatBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(MoldChartStatService.class, "summarizeBatch"),
				() -> BeanUtils.get(MoldChartStatService.class).summarizeBatch());
	}

	@Scheduled(cron = "0 0 0 * * *")
	public void procWactAllBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(MoldService.class, "procWactAll"), () -> BeanUtils.get(MoldService.class).procWactAll());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60), initialDelay = (300000))
	public void summarizeMoldProcChgBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(MoldProcChgService.class, "summarizeBatch"), () -> BeanUtils.get(MoldProcChgService.class).summarizeBatch());
	}

//	@Scheduled(fixedDelay = (1000 * 60 * 60), initialDelay = (80000))
//	public void summarizeProductionBatchJob() {
//		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(ProductionService.class, "summarizeBatch"), //
//				() -> BeanUtils.get(ProductionService.class).summarizeBatch(new ProductionSummarizeBatchIn()));
//	}

	@Scheduled(fixedDelay = (1000 * 60 * 60), initialDelay = (80000))
	public void summarizeProductStatBatchJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(ProductStatService.class, "summarizeBatch"), () -> BeanUtils.get(ProductStatService.class).summarizeBatch());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 7 + 600000), initialDelay = (150000))
	public void summarizeProdMoldStatBatchJob() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(ProdMoldStatService.class, "summarizeBatch"), () -> BeanUtils.get(ProdMoldStatService.class).summarizeBatch());
	}

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 12), initialDelay = 60000)
	public void postCycTimDevJob() {
		if ("central".equals(ConfigUtils.getServerName())) {
			return;
		}
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(CycTimDevService.class, "postCycTimDev"),
				() -> BeanUtils.get(CycTimDevService.class).post(new CycTimDevPostIn()));
	}

//	@Scheduled(fixedDelay = (1000 * 60 * 60 * 12), initialDelay = 60000)
//	public void postCycTimFluJob() {
//		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(CycTimFluService.class, "postCycTimFlu"), //
//				() -> BeanUtils.get(CycTimFluService.class).post());
//	}

	/*@Scheduled(fixedDelay = (1000 * 60 * 60), initialDelay = (1000 * 60 * 10))
	public void launchPQ() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AiLaunchService.class, "launch"), () -> BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_PQ));
	}*/

	/*@Scheduled(cron = "0 50 1 ? * SUN")
	public void launchMFE() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AiLaunchService.class, "launch"), () -> BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_MFE));
	}*/

	/*@Scheduled(cron = "0 40 2 * * *")
	public void launchTSD() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AiLaunchService.class, "launch"), () -> BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_TSD));
	}*/

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 4), initialDelay = (1000 * 60 * 10))
	public void launchPC() {
		if (BeanUtils.get(KafkaConfig.class).isKafkaEnabled()) {
			JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AiLaunchService.class, "launch"), () -> BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_PC));
		}
	}

	/*@Scheduled(cron = "")
	public void launchPPF() {
		JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AiLaunchService.class, "launch"), () -> BeanUtils.get(AiLaunchService.class).launch(AiModelType.AI_PPF));
	}*/

	@Scheduled(fixedDelay = (1000 * 60 * 60 * 12), initialDelay = (1000 * 60 * 60 * 2))
	public void retryFailedInfMessage() {
		if (BeanUtils.get(KafkaConfig.class).isKafkaEnabled()) {
			JobUtils.runIfNotRunningQuietly(ReflectionUtils.toShortName(AiRetryService.class, "launch"), () -> BeanUtils.get(AiRetryService.class).retryFailedKafkaMessage());
		}
	}

}
