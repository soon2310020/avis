package saleson.mms.company;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import saleson.common.enumeration.mapper.Code;
import saleson.common.enumeration.mapper.CodeMapper;


@Controller
@RequestMapping("/admin/companies")
public class AdminCompanyController {
	@Autowired
	CodeMapper codeMapper;

	/**
	 * 목록
	 * @return
	 */
	@GetMapping
	public String list() {
		return "admin/companies/list";
	}


	/**
	 * 등록
	 * @param model
	 * @return
	 */
	@GetMapping("/new")
	public String create(Model model) {

		model.addAttribute("companyTypes", codeMapper.get(Code.COMPANY_TYPE));
		return "admin/companies/form";
	}

	/**
	 * 수정
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}/edit")
	public String edit(@PathVariable("id") Long id, Model model) {

		model.addAttribute("companyTypes", codeMapper.get(Code.COMPANY_TYPE));
		return "admin/companies/form";
	}


	/**
	 * 상세
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public String details(@PathVariable("id") Long id, Model model) {

		model.addAttribute("companyTypes", codeMapper.get(Code.COMPANY_TYPE));
		return "admin/companies/details";
	}

}
