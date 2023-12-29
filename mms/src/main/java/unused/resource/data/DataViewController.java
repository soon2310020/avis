package unused.resource.data;

//@Deprecated
//public class DataViewController {
//
//	@GetMapping("/mms/data/terminal")
//	public String sensor(DataViewCriteria criteria, @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
//		BooleanBuilder predicate = new BooleanBuilder();
//
//		if (criteria.getQuery() != null && !criteria.getQuery().isEmpty()) {
//			predicate.and(QDataTerminal.dataTerminal.terminalId.contains(criteria.getQuery()));
//		}
//
//		Page<DataTerminal> pageContent = BeanUtils.get(DataTerminalRepository.class).findAll(predicate, pageable);
//
//		model.addAttribute("criteria", criteria);
//		model.addAttribute("pageContent", pageContent);
//		model.addAttribute("pagination", new DataViewPagination(pageContent));
//
//		return "data/terminal";
//	}
//
//}
