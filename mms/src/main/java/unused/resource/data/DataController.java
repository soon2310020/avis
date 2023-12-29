package unused.resource.data;

//@Deprecated
//@Slf4j
//@RestController
//@RequestMapping("/mms/data")
//@RequiredArgsConstructor
//public class DataController {
//	private final DataService dataService;
//
//	@Deprecated
//	@PostMapping
//	public ResponseEntity<String> index(@RequestBody DataDto dataDto) {
//
//		log.info("dataDto: {}", dataDto);
//
//		BeanUtils.get(DataServiceOld.class).save(dataDto);
//
//		try {
//			dataService.refine();
//		} catch (Exception e) {
//			log.error("Refine 오류 : ", e);
//		}
//		return ResponseEntity.ok("OK");
//	}
//
//	@Deprecated
//	@PostMapping("/heartbeat")
//	public ResponseEntity<String> heartbeat(@RequestBody HeartbeatDto heartbeatDto) {
//
//		log.info("heartbeatDto: {}", heartbeatDto);
//
//		BeanUtils.get(DataServiceOld.class).save(heartbeatDto);
//
//		return ResponseEntity.ok("OK");
//	}
//
//	@Deprecated
//	@GetMapping("/refine")
//	public ResponseEntity<String> refine() {
//		dataService.refine();
//		return ResponseEntity.ok("OK");
//	}
//
//}
