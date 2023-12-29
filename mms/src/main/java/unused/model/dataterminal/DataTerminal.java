package unused.model.dataterminal;

//@Deprecated
//@Getter
//@Setter
//@NoArgsConstructor
//@ToString
//@Entity
//@EqualsAndHashCode
//@EntityListeners(AuditingEntityListener.class)
//@Table(indexes = { @Index(name = "IDX_TERMINAL_ID", columnList = "terminalId"), })
//public class DataTerminal {
//	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
//	private Long id;
//
//	@Column(length = 20)
//	private String terminalId;
//
//	@Column(length = 20)
//	private String version;
//
//	@Column(length = 20)
//	private String networkType;
//
//	@Column(length = 20)
//	private String ip;
//
//	@CreatedDate
//	private Instant createdDate;
//
//	@LastModifiedDate
//	private Instant updatedDate;
//
//	@Column(columnDefinition = "text")
//	@Convert(converter = SensorDataConverter.class)
//	private List<Sensor> sensors = new ArrayList<>();
//
//	@Builder
//	public DataTerminal(String terminalId, String version, String networkType, String ip) {
//		this.terminalId = terminalId;
//		this.version = version;
//		this.networkType = networkType;
//		this.ip = ip;
//	}
//
//	public String getCreatedDateTime() {
//		String str = DateUtils2.format(createdDate, DatePattern.yyyy_MM_dd_HH_mm_ss, Zone.GMT);
//		return str;
//	}
//
//	public void addSensor(Sensor sensor) {
//		this.sensors.add(sensor);
//	}
//
//	@Converter(autoApply = true)
//	public static class SensorDataConverter implements AttributeConverter<List<Sensor>, String> {
//		private ObjectMapper objectMapper = new ObjectMapper();
//
//		@Override
//		public String convertToDatabaseColumn(List<Sensor> attribute) {
//			try {
//				return objectMapper.writeValueAsString(attribute);
//			} catch (JsonProcessingException e) {
//				return "[]";
//			}
//		}
//
//		@Override
//		public List<Sensor> convertToEntityAttribute(String dbData) {
//			try {
//				return objectMapper.registerModule(new JavaTimeModule()).readValue(dbData, new TypeReference<List<Sensor>>() {
//				});
//			} catch (Exception e) {
//				return new ArrayList<>();
//			}
//		}
//	}
//}
