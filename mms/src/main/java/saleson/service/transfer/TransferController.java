package saleson.service.transfer;

import java.net.URLEncoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.QTransferMessage;
import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.TransferMessage;
import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.TransferMessage.TransferData;
import com.emoldino.api.analysis.resource.base.data.repository.transfermessage.TransferMessageRepository;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.QTransferResult;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.TransferResult;
import com.emoldino.api.analysis.resource.base.data.repository.transferresult.TransferResultRepository;
import com.emoldino.api.analysis.resource.composite.trscol.service.TrsColService;
import com.emoldino.api.analysis.resource.composite.trscol.util.TrsColUtils;
import com.emoldino.api.asset.resource.base.mold.util.MoldUtils;
import com.emoldino.api.asset.resource.composite.rststp.service.RstStpService;
import com.emoldino.api.common.resource.base.log.enumeration.ErrorType;
import com.emoldino.api.common.resource.base.log.util.LogUtils;
import com.emoldino.framework.annotation.ApiOptions;
import com.emoldino.framework.dto.SuccessOut;
import com.emoldino.framework.util.BeanUtils;
import com.emoldino.framework.util.DateUtils2;
import com.emoldino.framework.util.DateUtils2.DatePattern;
import com.emoldino.framework.util.DateUtils2.Zone;
import com.emoldino.framework.util.JobUtils;
import com.emoldino.framework.util.JobUtils.JobCall;
import com.emoldino.framework.util.JobUtils.JobCallParam;
import com.emoldino.framework.util.JobUtils.JobOptions;
import com.emoldino.framework.util.ReflectionUtils;
import com.emoldino.framework.util.TranUtils;
import com.emoldino.framework.util.ValueUtils;
import com.querydsl.core.BooleanBuilder;

import lombok.extern.slf4j.Slf4j;
import saleson.api.counter.CounterRepository;
import saleson.api.mold.MoldRepository;
import saleson.common.config.Const;
import saleson.common.enumeration.PresetStatus;
import saleson.common.util.DateUtils;
import saleson.common.util.StringUtils;
import saleson.model.Counter;
import saleson.model.LogTransfer;
import saleson.model.Mold;
import saleson.model.Preset;
import saleson.model.Transfer;
import saleson.service.util.CryptoUtils;
import saleson.service.util.DecryptInfo;
import saleson.service.version.VersionService;

@Slf4j
@RestController
@RequestMapping("/mms/transfer")
public class TransferController {

	@PostMapping(value = "/data/health", produces = "text/html; charset=utf-8")
	public String health() {
		return "Connected";
	}

	/**
	 * The RawData of the 1st, 2nd generation Sensors are transferred.
	 * @param q	RawData
	 * @return
	 */
	@PostMapping(value = "/data", produces = "text/html; charset=utf-8")
	public String post(@RequestParam(value = "q", required = false) String[] q) {
		// at=TDATA&ti=TAP1200000001&rc=0&tv=1.8.3&dh=0&ip=192.168.123.183&gw=192.168.123.1&dn=192.168.123.2
		// at=CDATA&id={ti}/{ci}&sc={sc}&time={lst}/{rt}&ci=N/519/H&ctt=519/10/532/5/560/9/565/1/566/6/580/6/587/4/654/2/679/1/800/8&rmi=0/0/*/*/Y&temp=483/483/485/483/487/{tff}/483&ei=208/N&sn={sn}

		/**
		 * 1. Validation
		 */
		if (ObjectUtils.isEmpty(q)) {
			log.debug("[TRANSFER] ERROR_A : q is null or empty ");
			return toResult("ERROR_A");
		}

		List<String> content = Arrays.asList(q);

		return post(new TransferPostIn(content));
	}

	@ApiOptions(longElapsedTimeMillisThreshold = 50000)
	public String post(TransferPostIn input) {
		List<String> content = input == null ? null : input.getContent();
		if (ObjectUtils.isEmpty(content)) {
			return toResult("ERROR_A");
		}

		if (log.isInfoEnabled()) {
			content.forEach(n -> log.info("[TRANSFER] q = {}", n));
		}

		try {
			/**
			 * 2. Build Transfers
			 */
			List<Transfer> transfers = new ArrayList<>();
			DecryptInfo decryptInfo = null;
			String at = null;
			int i = 0;
			for (String es : content) {
				if (StringUtils.isEmpty(es)) {
					continue;
				}

				// 2.1 Decrypt
				try {
					decryptInfo = new CryptoUtils().decrypt(es);
				} catch (Exception e) {
					LogUtils.saveErrorQuietly(ErrorType.REQ, "TRS_DECODE_FAIL", HttpStatus.BAD_REQUEST, "Decode Fail: " + es);
					return toResult("ERROR_A");
				}
				log.info("[TRANSFER] decryptInfo : {}", decryptInfo);

				String ds = decryptInfo.getDecryptText();

				// 2.2 Build Transfer
				Transfer transfer = toTransfer(es, ds);
				transfers.add(transfer);

				at = transfer.getAt();

				// 2.4 CDATA Case
				/**
				 * Transferred CDATA Case (not a batch), Enqueue Whole Message and Return Right Away for Preventing Timeout
				 */
				if (i++ == 0 && !input.isBatch() && "CDATA".equals(at)) {
					TranUtils.doNewTran(() -> {
						TransferMessage message = BeanUtils.get(TrsColService.class).saveMessage(transfer.getAt(), transfer.getCi(), new TransferData(content));
						input.setBatch(true);
						enqueue(message, "O", "X");
					});

					return toResult(String.valueOf(content.size()));
				}
			}
			if (transfers.isEmpty()) {
				return toResult("ERROR_A");
			}

			if ("CDATA".equals(at) && transfers.size() > 1 && //
					!ObjectUtils.isEmpty(transfers.get(0).getCi()) && transfers.get(0).getCi().startsWith("NCM")) {
				int j[] = { 0 };
				Map<String, Transfer> map = new TreeMap<>();
				transfers.forEach(transfer -> {
					String key = ValueUtils.toString(transfer.getTff(), transfer.getRt());
					if (ObjectUtils.isEmpty(key)) {
						key = DateUtils2.getString(DatePattern.yyyyMMddHHmmss, Zone.SYS);
					}
					if (map.containsKey(key)) {
						map.put(key + ++j[0], transfer);
					} else {
						map.put(key, transfer);
					}
				});
				transfers = new ArrayList<>(map.values());
			}
			List<Transfer> _transfers = transfers;

			/**
			 * 3. Save RawData
			 */
			TranUtils.doNewTran(() -> BeanUtils.get(TransferService.class).saveLog(_transfers));

			/**
			 * 4. Process Data
			 */
			Transfer firstTransfer = transfers.get(0);
			Long firstLi = firstTransfer.getLi();

			// 4.1 Terminal Data
			if ("TDATA".equals(at)) {
				BeanUtils.get(TransferService.class).procTdata(transfers);
				return toResult("OK");
			}
			// 4.2 Counter Data (Shot Count, Cycle Time, Temperature)
			else if ("CDATA".equals(at)) {
				TranUtils.doNewTran(() -> {
					_transfers.forEach(transfer -> {
						if (ObjectUtils.isEmpty(transfer.getCi())) {
							LogUtils.saveErrorQuietly(ErrorType.REQ, "TRS_EMPTY_CI", HttpStatus.BAD_REQUEST, "Counter ID is required!! li:" + firstLi);
							return;
						}

						// 4.2.2 Activate Counter (Only if when it is not activated, yet)
						Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(transfer.getCi()).orElse(null);
						if (counter != null && counter.getActivatedAt() == null) {
							counter.setActivatedAt(Instant.now());
							BeanUtils.get(CounterRepository.class).save(counter);
						}

						// 4.2.3 Separate to Each Proc CDATA Message and Enqueue
						enqueue(transfer, "-", "O", "X");
					});
				});

				// 4.2.1 Response
				return toResult(String.valueOf(transfers.size()));
			}
			// Acceleration Data
			else if ("ADATA".equals(at)) {
				BeanUtils.get(TransferService.class).procAdata(transfers);
				return toResult(String.valueOf(transfers.size()));
			}
			// Preset Data
			else if ("PRESET".equals(at)) {
				Transfer transfer = firstTransfer;
				Integer shotCount = transfer.getSc(); // PRESET을 등록한 카운터의 최종 Shot 수
				BeanUtils.get(TransferService.class).save(transfer);

				PresetStatus status = null;
				Preset preset = null;
				if (shotCount == null) {
					preset = BeanUtils.get(RstStpService.class).getLastByCounterCode(transfer.getCi());
					if (preset != null) {
						status = preset.getPresetStatus();
						if (PresetStatus.READY.equals(status)) {
							preset = BeanUtils.get(RstStpService.class).completeAllByCounterCode(transfer.getCi());
						}
					}
				} else {
					preset = BeanUtils.get(RstStpService.class).completeAllByCounterCode(transfer.getCi());
					if (preset != null) {
						status = preset.getPresetStatus();
					}
				}

				if (preset == null || PresetStatus.APPLIED.equals(status)) {
					return toResultEncoded(Preset.failureResponse(transfer), decryptInfo);
				} else {
					return toResultEncoded(preset.responseToTerminal(transfer), decryptInfo);
				}

			}
			// UPURL
			else if ("UPURL".equals(at)) {
				Transfer transfer = firstTransfer;
				String upurl = BeanUtils.get(VersionService.class).getUpurlBy(transfer);
				return toResultEncoded(upurl, decryptInfo);
			}
			// Unknown Type 'at'
			else {
				log.debug("[TRANSFER] ERROR_A : at is wrong!");
				LogUtils.saveErrorQuietly(ErrorType.REQ, "TRS_UNKNOWN_AT", HttpStatus.BAD_REQUEST, "Unknown transfer's at: " + at + " li:" + firstLi);
				return toResult("ERROR_A");
			}

		} catch (Exception e) {
			log.error("[TRANSFER] ERROR_A : Exception occurred: " + e.getMessage(), e);
			LogUtils.saveErrorQuietly(ErrorType.SYS, "TRS_SYS_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
			return toResult("ERROR_A");
		}
	}

	@GetMapping(value = "/data/retry")
	public SuccessOut retry(TransferRetryIn input) {
		if (input.getId() != null) {
			retry(input.getId());
			return SuccessOut.getDefault();
		}

		ValueUtils.assertNotEmpty(input.getFromTime(), "fromTime");
		ValueUtils.assertNotEmpty(input.getToTime(), "toTime");

		Instant fromTime = DateUtils2.toInstant(input.getFromTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
		Instant toTime = DateUtils2.toInstant(input.getToTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);

		JobUtils.runIfNotRunning("Transfer.retry", new JobOptions().setClustered(true), () -> {
			// TransferMessage
			{
				QTransferMessage table = QTransferMessage.transferMessage;
				int counter = 0;
				Long[] lastId = { 0L };
				while (counter++ < 1000) {
					BooleanBuilder filter = new BooleanBuilder();
					filter.and(table.procStatus.eq("X"));
					filter.and(table.createdAt.goe(fromTime));
					filter.and(table.createdAt.loe(toTime));
					filter.and(table.id.gt(lastId[0]));
					Page<TransferMessage> page = TranUtils
							.doNewTran(() -> BeanUtils.get(TransferMessageRepository.class).findAll(filter, PageRequest.of(0, 100, Direction.ASC, "id")));
					if (page.isEmpty()) {
						break;
					}
					page.forEach(item -> {
						lastId[0] = item.getId();
						BeanUtils.get(TrsColService.class).changeMessageStatusQuietly(item.getId(), "R");
						enqueue(item, "RO", "RX");
					});
				}
			}

			// TransferResult
			{
				QTransferResult table = QTransferResult.transferResult;
				int counter = 0;
				Long[] lastId = { 0L };
				while (counter++ < 1000) {
					BooleanBuilder filter = new BooleanBuilder();
					filter.and(table.procStatus.eq("X"));
					filter.and(table.createdAt.goe(fromTime));
					filter.and(table.createdAt.loe(toTime));
					filter.and(table.id.gt(lastId[0]));
					Page<TransferResult> page = TranUtils
							.doNewTran(() -> BeanUtils.get(TransferResultRepository.class).findAll(filter, PageRequest.of(0, 100, Direction.ASC, "id")));
					if (page.isEmpty()) {
						break;
					}
					page.forEach(item -> {
						lastId[0] = item.getId();
						retry(item.getId());
					});
				}
			}
		});

		return SuccessOut.getDefault();
	}

	private void retry(Long logTransferId) {
		LogTransfer logTransfer = TranUtils.doNewTran(() -> BeanUtils.get(LogTransferRepository.class).findById(logTransferId).orElse(null));
		if (logTransfer == null || !"CDATA".equals(logTransfer.getAt()) || ObjectUtils.isEmpty(logTransfer.getDs())) {
			return;
		}

		Transfer transfer = toTransfer(logTransfer.getEs(), logTransfer.getDs());
		transfer.setLi(logTransfer.getId());

		enqueue(transfer, "R", "RO", "RX");
	}

	public static Transfer toTransfer(String es, String ds) {
		Map<String, Object> map = new HashMap<>();
		// Get Params
		MultiValueMap<String, String> params;
		String paramsStr = ds;
		if (!ds.startsWith("?")) {
			paramsStr = "?" + ds;
		}
		params = UriComponentsBuilder.fromUriString(paramsStr).build().getQueryParams();
		log.info("[TRANSFER] parameters : {}", params);

		// Use Only First Value of MultiValueParams
		params.forEach((k, v) -> TrsColUtils.bind(map, k, v.get(0)));
		// Adjust idSecondValue -> tv or ci
		if (map.get("idSecondValue") != null) {
			String at = ValueUtils.toString(map.get("at"));
			if ("TDATA".equals(at)) {
				map.put("tv", map.get("idSecondValue"));
			} else if ("CDATA".equals(at)) {
				map.put("ci", map.get("idSecondValue"));
				// parsing ctt -> ulct and llct
				TrsColUtils.parseCtt(map);
			} else if ("ADATA".equals(at)) {
				map.put("ci", map.get("idSecondValue"));
			}
			map.remove("idSecondValue");
		}
		log.info("[TRANSFER] Map Data : {}", map);

		Transfer transfer = ValueUtils.fromMap(map, Transfer.class);
		transfer.setEs(es);
		transfer.setDs(ds);
		transfer.setCreatedAt(DateUtils2.newInstant());
		return transfer;
	}

	private void enqueue(TransferMessage message, String succeedStatus, String errorStatus) {
		JobCall call = new JobCall();
		call.setLogicName(ReflectionUtils.toName(TrsColService.class, "procMessage"));
		call.getParams().add(new JobCallParam("id", Long.class, message.getId()));

		JobCall afterCall = new JobCall();
		afterCall.setLogicName(ReflectionUtils.toName(TrsColService.class, "changeMessageStatusQuietly"));
		afterCall.getParams().add(new JobCallParam("id", Long.class, message.getId()));
		afterCall.getParams().add(new JobCallParam("status", String.class, succeedStatus));

		JobCall throwsCall = new JobCall();
		throwsCall.setLogicName(ReflectionUtils.toName(TrsColService.class, "changeMessageStatusQuietly"));
		throwsCall.getParams().add(new JobCallParam("id", Long.class, message.getId()));
		throwsCall.getParams().add(new JobCallParam("status", String.class, errorStatus));
		throwsCall.getParams().add(new JobCallParam("t", Throwable.class, null));

		JobUtils.enqueue("transferTaskExecutor", "DISTRIBUTOR", false, call, null, afterCall, throwsCall);
	}

	private void enqueue(Transfer transfer, String initialStatus, String succeedStatus, String errorStatus) {
		BeanUtils.get(TrsColService.class).changeStatusQuietly(transfer.getLi(), initialStatus);

		JobCall call = new JobCall();
		call.setLogicName(ReflectionUtils.toName(TransferService.class, "procCdata"));
		call.getParams().add(new JobCallParam("transfer", Transfer.class, transfer));

		JobCall afterCall = new JobCall();
		afterCall.setLogicName(ReflectionUtils.toName(TrsColService.class, "changeStatusQuietly"));
		afterCall.getParams().add(new JobCallParam("id", Long.class, transfer.getLi()));
		afterCall.getParams().add(new JobCallParam("status", String.class, succeedStatus));

		JobCall throwsCall = new JobCall();
		throwsCall.setLogicName(ReflectionUtils.toName(TrsColService.class, "changeStatusQuietly"));
		throwsCall.getParams().add(new JobCallParam("id", Long.class, transfer.getLi()));
		throwsCall.getParams().add(new JobCallParam("status", String.class, errorStatus));
		throwsCall.getParams().add(new JobCallParam("t", Throwable.class, null));

		JobUtils.enqueue("transfer2TaskExecutor", transfer.getCi(), false, call, null, afterCall, throwsCall);
	}

	private String toResult(String message) {
		if (StringUtils.isEmpty(message)) {
			message = "ERROR_A";
		}

		StringBuilder buf = new StringBuilder();
		buf.append("[STX]").append(System.lineSeparator());
		buf.append(message.length()).append(System.lineSeparator());
		buf.append(message).append(System.lineSeparator());
		buf.append("[ETX]");
		return buf.toString();
	}

	private String toResultEncoded(String message, DecryptInfo decryptInfo) {
		String result;
		try {
			result = new CryptoUtils().encrypt(message, decryptInfo.getKey(), decryptInfo.getIv());
			result = URLEncoder.encode(result, "UTF-8");
			result = result.replaceAll("\\*", "%2A");
		} catch (Exception e) {
			result = "ERROR_B";
			log.warn("[TRANSFER] Response encrypt error ({}, {}, {})", message, decryptInfo.getKey(), decryptInfo.getIv(), e);
		}
		return toResult(result);
	}

	@PostMapping(value = "/dataTest", produces = "text/html; charset=utf-8")
	public String regTest(@RequestParam(value = "q", required = false) String[] q) throws Exception {
		CryptoUtils cryptoUtils = new CryptoUtils();
		String[] qEnc = new String[q.length];
		for (int i = 0; i < q.length; i++) {
			qEnc[i] = cryptoUtils.encrypt(q[i]);
		}
		return post(qEnc);
	}

	@PostMapping(value = "/dataTestByDateRange", produces = "text/html; charset=utf-8")
	public String regTestByDateRange(@RequestParam(value = "from") String from, @RequestParam(value = "to") String to, @RequestParam(value = "terminal") String terminal,
			@RequestParam(value = "counter") String counter, @RequestParam(value = "sc") String sc, @RequestParam(value = "ct") String ct) throws Exception {

		String qString = "at=CDATA&id={terminal}/{counter}&sc={sc}&time={lst}/{rt}&ci=N/200/H&ctt={ct}/{sc}/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*&rmi=0/0/*/*/Y&temp=315/321/319/323/313/20210821082526/317&ei=0/N&sn={sn}";

		List<String> dates = DateUtils.getListStringDateBetween(from, to, "yyyyMMdd", "yyyyMMdd");

		String returnString = "";
		for (String date : dates) {
			Thread.sleep(1000);
			String lst = date + "130000";
			Random random = new Random();
			Integer sn = random.ints(160797, 241097).findFirst().getAsInt();

			String qInput = qString.replace("{terminal}", terminal).replace("{counter}", counter).replace("{lst}", lst).replace("{rt}", lst).replace("{sc}", sc).replace("{ct}", ct)
					.replace("{sn}", String.valueOf(sn));

			System.out.println(qInput);

			String[] qEnc = new String[1];

			qEnc[0] = new CryptoUtils().encrypt(qInput);

			returnString += "\n" + post(qEnc);
		}
		return returnString;
	}

	@GetMapping("/proc-valid-shot-all")
	public ResponseEntity<String> procValidShotAll() {
		BeanUtils.get(TransferService.class).procValidShotAll();
		return ResponseEntity.ok(Const.SUCCESS);
	}

	@GetMapping("/proc-valid-shot-by-mold-id")
	public ResponseEntity<String> procValidShot(@RequestParam(name = "moldId", required = true) Long moldId, @RequestParam(name = "month") List<String> month) {
		BeanUtils.get(TransferService.class).procValidShotByMoldId(moldId, month);
		return ResponseEntity.ok(Const.SUCCESS);
	}

	@GetMapping("/wact")
	public ResponseEntity<Double> getWact(@RequestParam(name = "moldId", required = true) Long moldId) {
		Mold mold = BeanUtils.get(MoldRepository.class).findById(moldId).orElse(null);
		return ResponseEntity.ok(MoldUtils.getWact(moldId, mold.getContractedCycleTime(), null));
	}

	@GetMapping("/reset-all-valid-data-shot")
	public ResponseEntity<String> resetStoredAllValidShotVals() {
		BeanUtils.get(TransferService.class).resetStoredAllValidShotVals();
		return ResponseEntity.ok(Const.SUCCESS);
	}

	@GetMapping("/proc-wut-all")
	public ResponseEntity<String> procWutAll() {
		BeanUtils.get(TransferService.class).procWutAll();
		return ResponseEntity.ok(Const.SUCCESS);
	}

}
