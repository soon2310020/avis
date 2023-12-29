package unused.resource.data;

//@Deprecated
//public class DataMapperOld {
//
//	/**
//	 * RawData
//	 * @param terminalData
//	 * @return
//	 */
//	@Deprecated
//	public List<Data> toListFrom(DataDto terminalData) {
//		if (terminalData == null || terminalData.getRawdata() == null || terminalData.getRawdata().length == 0) {
//			return Collections.emptyList();
//		}
//		String terminalId = terminalData.getTerminal_id();
//		Instant now = DateUtils2.newInstant();
//		return Arrays.stream(terminalData.getRawdata()).map(r -> new Data(terminalId, r, now)).collect(Collectors.toList());
//	}
//
//	@Deprecated
//	public DataTerminal toDataTerminal(HeartbeatDto heartbeatDto) {
//		DataTerminal dataTerminal = DataTerminal.builder()//
//				.terminalId(heartbeatDto.getTerminal().getId())//
//				.version(heartbeatDto.getTerminal().getSw_ver())//
//				.networkType(heartbeatDto.getNetwork().getType())//
//				.ip(heartbeatDto.getNetwork().getIp())//
//				.build();
//
//		Arrays.stream(heartbeatDto.getSensor().getList()).forEach(s -> {
//			dataTerminal.addSensor(new Sensor(s.getId(), s.getLast_read()));
//		});
//
//		return dataTerminal;
//	}
//
//}
