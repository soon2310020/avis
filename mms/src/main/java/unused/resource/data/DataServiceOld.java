package unused.resource.data;

//@Deprecated
//@Slf4j
//public class DataServiceOld {
//
//	@Deprecated
//	public void save(DataDto dataDto) {
//		List<Data> list = BeanUtils.get(DataMapper.class).toListFrom(dataDto);
//
//		List<Data> insertList = new ArrayList<>();
//
//		list.stream().forEach(d -> insertList.add(d));
//
//		if (!insertList.isEmpty()) {
//			BeanUtils.get(DataRepository.class).saveAll(insertList);
//		}
//	}
//
//	@Deprecated
//	public void save(HeartbeatDto heartbeatDto) {
//		DataTerminal data = BeanUtils.get(DataMapper.class).toDataTerminal(heartbeatDto);
//		BeanUtils.get(DataTerminalRepository.class).save(data);
//		try {
//			TerminalUtils.setTerminalOperated(data.getTerminalId(), data.getIp());
//		} catch (Exception e) {
//			log.warn(e.getMessage(), e);
//			LogUtils.saveErrorQuietly(ErrorType.SYS, "TERMINAL_STATUS_FAIL", null, "Failed to save terminal status: " + data.getTerminalId(), e);
//		}
//	}
//
//	private static final BigDecimal _10 = new BigDecimal(10);
//
//	@Deprecated
//	public static List<Transfer> toTransfers_Old(List<DataCounter> dataCounters) {
//		List<Transfer> result = new ArrayList<>();
//		Instant createdAt = Instant.now(); // 데이터 수신 시간
//		dataCounters.forEach(data -> {
//			String zoneId = LocationUtils.getZoneIdByTerminalCode(data.getTerminalId());
//			Instant recvTime = DateUtils2.toInstant(data.getReadTime(), DatePattern.yyyy_MM_dd_HH_mm_ss_SSS, Zone.GMT);
//			String rt = DateUtils2.format(recvTime, DatePattern.yyyyMMddHHmmss, zoneId);
//			// TODO
////			String bs = isSensorStatusOk(dataCounter.getBatteryStatus(), BAT) ? "H" : "L";
//
//			List<String> temps = new ArrayList<>();
//			if (!ObjectUtils.isEmpty(data.getTemperature())) {
//				String tempStr = data.getTemperature();
//				for (int i = 0; i < tempStr.length(); i += 4) {
//					String tempVal = tempStr.substring(i, i + 4);
//					if (!ValueUtils.isNumber(tempVal)) {
//						continue;
//					}
//					temps.add(tempVal);
//				}
//			}
//
//			Map<String, List<CycleTime>> shotsByShotEndTimeMap = new LinkedHashMap<>();
//			Map<String, String> tempsByShotEndTimeMap = new LinkedHashMap<>();
//			if (ObjectUtils.isEmpty(data.getCycleTimes()) || data.getCycleTimes().size() == 1) {
//				String shotEndTime = DateUtils2.toOtherZone(data.getShotEndTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT, zoneId);
//				shotsByShotEndTimeMap.put(shotEndTime, data.getCycleTimes());
//				tempsByShotEndTimeMap.put(shotEndTime, toTempStr(temps, shotEndTime));
//			} else {
//				// When ShotStartTime is empty
//				if (ObjectUtils.isEmpty(data.getShotStartTime()) && !ObjectUtils.isEmpty(data.getShotEndTime())) {
//					long time = 0L;
//					for (CycleTime cycleTime : data.getCycleTimes()) {
//						double ct = cycleTime == null ? 0d : cycleTime.getCycleTime().doubleValue();
//						time += ValueUtils.toLong(ct * 1000, 0L);
//					}
//					Instant shotEndInst = DateUtils2.toInstant(data.getShotEndTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
//					Instant shotStartInst = shotEndInst.minus(Duration.ofMillis(time));
//					String shotStartTime = DateUtils2.format(shotStartInst, DatePattern.yyyyMMddHHmmss, Zone.GMT);
//					data.setShotStartTime(shotStartTime);
//				}
//				Instant shotStartInst = DateUtils2.toInstant(data.getShotStartTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
//				Instant instant = DateUtils2.toInstant(data.getShotStartTime(), DatePattern.yyyyMMddHHmmss, Zone.GMT);
//				String prevShotEndTime = null;
//				List<CycleTime> shotByHourList = new ArrayList<>();
//				for (CycleTime shot : data.getCycleTimes()) {
//					double ct = shot == null ? 0d : shot.getCycleTime().doubleValue();
//					Instant prevInstant = instant;
//					if (ct > 0) {
//						instant = instant.plus(Duration.ofMillis(ValueUtils.toLong(ct * 1000, 0L)));
//					}
//
//					String shotEndTime = DateUtils2.format(instant, DatePattern.yyyyMMddHHmmss, zoneId);
//					String hour = shotEndTime.substring(0, 10);
//					// Separate Shot by Hour List
//					if (prevShotEndTime != null && !prevShotEndTime.startsWith(hour)) {
//						shotsByShotEndTimeMap.put(prevShotEndTime, shotByHourList);
//						if (!ObjectUtils.isEmpty(temps)) {
//							List<String> temps2 = new ArrayList<>();
//							if (prevInstant.equals(shotStartInst)) {
//								temps2.add(temps.remove(0));
//							} else {
//								long _10mins = Math.max(1L, (prevInstant.getEpochSecond() - shotStartInst.getEpochSecond()) / 600);
//								for (int i = 0; i < _10mins; i++) {
//									temps2.add(temps.remove(0));
//									if (temps.isEmpty()) {
//										break;
//									}
//								}
//							}
//							tempsByShotEndTimeMap.put(prevShotEndTime, toTempStr(temps2, prevShotEndTime));
//							shotStartInst = instant;
//						}
//						shotByHourList = new ArrayList<>();
//					}
//					shotByHourList.add(shot);
//					prevShotEndTime = shotEndTime;
//				}
//				shotsByShotEndTimeMap.put(prevShotEndTime, shotByHourList);
//				tempsByShotEndTimeMap.put(prevShotEndTime, toTempStr(temps, prevShotEndTime));
//			}
//
//			shotsByShotEndTimeMap.forEach((shotEndTime, cycleTimes) -> {
//				Transfer transfer = new Transfer();
//				transfer.setAt("CDATA");
//				transfer.setTi(data.getTerminalId());
//				transfer.setCi(data.getCounterId());
//				transfer.setSc(data.getShotCount());
//				transfer.setRt(rt);
//				transfer.setCf("N"); // TODO: finding correct corresponding cf value
//				transfer.setSn(data.getDataId().intValue());
//				transfer.setBs(DatColUtils.isBatteryStatusOk(data.getStatus()) ? "H" : "L");
//				transfer.setTff(shotEndTime);
//
//				// Resolve CTT values
//				if (ObjectUtils.isEmpty(cycleTimes)) {
//					Counter counter = BeanUtils.get(CounterRepository.class).findByEquipmentCode(data.getCounterId()).orElse(null);
//					String lst = counter == null || counter.getLastShotAt() == null ? shotEndTime : DateUtils2.format(counter.getLastShotAt(), DatePattern.yyyyMMddHHmmss, zoneId);
//					transfer.setLst(lst);
//					transfer.setCt(0.0);
//					transfer.setCtt("/");
//				} else {
//					transfer.setLst(shotEndTime);
//					Map<Integer, Integer> ctt = new HashMap<>();
//					cycleTimes.forEach(cycleTime -> {
//						int key = cycleTime.getCycleTime().setScale(2, RoundingMode.HALF_UP).multiply(_10).intValue();
//						if (ctt.containsKey(key)) {
//							return;
//						}
//						Long shots = cycleTimes.stream()//
//								.filter(x -> x.getId().equals(cycleTime.getId()))//
//								.count();
//						ctt.put(cycleTime.getCycleTime().setScale(2, RoundingMode.HALF_UP).multiply(_10).intValue(), shots.intValue());
//					});
//					// LinkedHashMap preserve the ordering of elements in which they are inserted
//					LinkedHashMap<Integer, Integer> reverseSortedMap = new LinkedHashMap<>();
//
//					// Use Comparator.reverseOrder() for reverse ordering
//					ctt.entrySet().stream()//
//							.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))//
//							.forEachOrdered(x -> reverseSortedMap.put(x.getKey(), x.getValue()));
//					transfer.setCt(Double.valueOf(reverseSortedMap.keySet().stream().findFirst().get()));
//					final String[] cttString = { "" };
//					reverseSortedMap.forEach((ct, sc) -> {
//						cttString[0] += ct + "/" + sc + "/";
//					});
//					cttString[0] = cttString[0].substring(0, cttString[0].length() - 1);
//					transfer.setCtt(cttString[0]);
//				}
//
//				Map<String, Object> map = new HashMap<>();
//				try {
//					map.put("ctt", transfer.getCtt());
//					map.put("ct", ValueUtils.toInteger(transfer.getCt(), 0).toString());
//					TrsColUtils.parseCtt(map);
//					if (map.containsKey("ulct")) {
//						transfer.setUlct(Double.valueOf(map.get("ulct").toString()));
//						transfer.setLlct(Double.valueOf(map.get("llct").toString()));
//					}
//				} catch (Exception e) {
//					log.error(e.getMessage(), e);
//				}
//
//				String tempStr = tempsByShotEndTimeMap.get(shotEndTime);
//				try {
//					bindTemp(map, tempStr); // 멀티 밸류값 중 첫번째만.
//					if (!map.isEmpty()) {
//						transfer.setTlo((Integer) map.get("tlo"));
//						transfer.setThi((Integer) map.get("thi"));
//						transfer.setTav((Integer) map.get("tav"));
//						transfer.setTnw(ValueUtils.toInteger(map.get("tnw"), 0));
//					}
//				} catch (Exception e) {
//					log.warn(e.getMessage(), e);
//					LogUtils.saveErrorQuietly(ErrorType.LOGIC, "TEMPERATURE_PARSE_FAIL", HttpStatus.NOT_IMPLEMENTED, "Temperature Parsing Failed", e);
//				}
//				transfer.setTemp(tempStr);
//				transfer.setCreatedAt(createdAt);
//
//				result.add(transfer);
//			});
//		});
//		return result;
//	}
//
//	@Deprecated
//	private static String toTempStr(List<String> temps, String tff) {
//		if (ObjectUtils.isEmpty(temps) || ObjectUtils.isEmpty(tff)) {
//			return "";
//		}
//		StringBuilder buf = new StringBuilder();
//		String lastStr = null;
//		for (String str : temps) {
//			buf.append(str).append("/");
//			lastStr = str;
//		}
//		buf.append(tff).append("/");
//		buf.append(lastStr);
//		return buf.toString();
//	}
//
//	@Deprecated
//	private static void bindTemp(Map<String, Object> map, String value) {
//		if (ObjectUtils.isEmpty(value)) {
//			return;
//		}
//
//		String[] values = StringUtils.delimitedListToStringArray(value, "/");
//
//		int length = values.length;
//		if (length < 3) {
//			return;
//		}
//
//		// [CDATA] temp = 10분단위 온도1 / 온도2 / 온도3 / 온도4 / 온도5 / 온도1 시간 / 현재온도(tnw) / rtr / rtf / rtl / rat
//		map.put("temp", value);
//
//		String tff = null;
//		String current = null;
//		// TODO remove try~ catch~
//		try {
//			tff = values[length - 2];
//			current = values[length - 1];
//
//			Integer tlo = null;
//			Integer thi = null;
//			Integer tav = null;
//			Long tempCount = Arrays.stream(values).filter(s -> isTemp(s)).count();
//			if (tempCount > 0) {
//				for (String str : values) {
//					if (!isTemp(str)) {
//						continue;
//					}
//					int temp = Integer.parseInt(str);
//					if (tlo == null) {
//						tlo = temp;
//					} else {
//						tlo = Math.min(tlo, temp);
//					}
//					if (thi == null) {
//						thi = temp;
//					} else {
//						thi = Math.max(thi, temp);
//					}
//					if (tav == null) {
//						tav = temp;
//					} else {
//						tav += temp;
//					}
//				}
//				tav = (int) Math.round(tav / ValueUtils.toDouble(tempCount, 0d));
//			}
//			if (tlo != null) {
//				map.put("tlo", tlo);
//			}
//			if (thi != null) {
//				map.put("thi", thi);
//			}
//			if (tav != null) {
//				map.put("tav", tav);
//			}
//		} catch (Exception e) {
//			log.warn(e.getMessage(), e);
//		}
//		if (!ObjectUtils.isEmpty(tff)) {
//			map.put("tff", tff);
//		}
//		if (!ObjectUtils.isEmpty(current)) {
//			map.put("tnw", ObjectUtils.isEmpty(current) ? null : current);
//		}
//	}
//
//	@Deprecated
//	private static boolean isTemp(String str) {
//		return !ObjectUtils.isEmpty(str) && str.length() < 5 && ValueUtils.isNumber(str);
//	}
//
//}
