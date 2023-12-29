package saleson.restdocs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import saleson.api.mold.MoldParam;
import saleson.common.ApiControllerTest;
import saleson.enums.UserType;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApisControllerTest  extends ApiControllerTest {

	@Test
	@DisplayName("Tooling - list")
	void getMolds() throws Exception {

		MoldParam param = MoldParam.builder()
				.query("PK USA")
				.status("all")
				.sort("id,desc")
				.page(1)
				.build();

		mockMvc.perform(get("/apis/molds")
				.header(HttpHeaders.AUTHORIZATION, getBearerToken(UserType.HQ))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(param))
		)
				.andDo(print())
				.andExpect(status().isOk())


				.andDo(document("tooling-list",
						requestHeaders(
								headerWithName("Authorization").description("Bearer accessToken")
						),

						relaxedRequestFields(
								fieldWithPath("query").type(JsonFieldType.STRING).optional().description("keyword"),
								fieldWithPath("operatingStatus").type(JsonFieldType.STRING).optional().description("Operating Status"),
								fieldWithPath("equipmentStatus").type(JsonFieldType.STRING).optional().description("Equipment Status"),
								fieldWithPath("page").type(JsonFieldType.NUMBER).optional().description("Page number"),
								fieldWithPath("query").type(JsonFieldType.STRING).optional().description("keyword"),
								fieldWithPath("status").type(JsonFieldType.STRING).optional().description("TAB Status (all, )"),
								fieldWithPath("sort").type(JsonFieldType.STRING).optional().description("Data Sort. (id,desc Default)"),
								fieldWithPath("extraStatus").type(JsonFieldType.STRING).optional().description("Default : 'op-status-is-not-null'")

						),

						relaxedResponseFields(
								fieldWithPath("content.[].id").type(JsonFieldType.NUMBER).description("Tooling ID"),
								fieldWithPath("content.[].equipmentCode").type(JsonFieldType.STRING).description("equipmentCode"),
								fieldWithPath("content.[].equipmentStatus").type(JsonFieldType.STRING).description("equipmentStatus"),
								fieldWithPath("content.[].name").type(JsonFieldType.STRING).description("Tooling name"),
								fieldWithPath("content.[].runnerType").type(JsonFieldType.STRING).description("runnerType"),
								fieldWithPath("content.[].size").type(JsonFieldType.STRING).description("size"),
								fieldWithPath("content.[].sizeUnit").type(JsonFieldType.STRING).description("sizeUnit"),
								fieldWithPath("content.[].counterCode").type(JsonFieldType.STRING).description("counterCode"),
								fieldWithPath("content.[].contractedCycleTimeSeconds").type(JsonFieldType.STRING).description("Approval Cycle Time (Sec)"),
								//fieldWithPath("content.[].locationTitle").type(JsonFieldType.STRING).description("locationTitle"),
								fieldWithPath("content.[].locationCode").type(JsonFieldType.STRING).description("locationCode"),
								//fieldWithPath("content.[].locationCity").type(JsonFieldType.STRING).description("locationCity"),
								//fieldWithPath("content.[].lastShot").type(JsonFieldType.NUMBER).description("lastShot"),
								fieldWithPath("content.[].lastShotDateTime").type(JsonFieldType.STRING).description("lastShotDateTime"),
								//fieldWithPath("content[].operatingStatus").type(JsonFieldType.STRING).description("operatingStatus"),
								fieldWithPath("content.[].operatedDateTime").type(JsonFieldType.STRING).description("operatedDateTime"),
								fieldWithPath("content.[].companyName").type(JsonFieldType.STRING).description("companyName"),
								fieldWithPath("content.[].toolMakerCompanyName").type(JsonFieldType.STRING).description("toolMakerCompanyName"),
								fieldWithPath("content.[].createdDateTime").type(JsonFieldType.STRING).description("createdDateTime"),


								// Paging
								fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("First Page. "),
								fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("Last Page. "),
								fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("total Pages "),
								fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("Total Elements"),
								fieldWithPath("size").type(JsonFieldType.NUMBER).description("size"),
								fieldWithPath("number").type(JsonFieldType.NUMBER).description("page number.")
						)



				))


		;
	}



	@Test
	@DisplayName("Tooling - get")
	void getMold() throws Exception {


		mockMvc.perform(get("/apis/molds/6298")
				.header(HttpHeaders.AUTHORIZATION, getBearerToken(UserType.HQ))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk())


				.andDo(document("tooling-get",
						requestHeaders(
								headerWithName("Authorization").description("Bearer accessToken")
						),

						relaxedResponseFields(
								fieldWithPath("id").type(JsonFieldType.NUMBER).description("Tooling ID"),
								fieldWithPath("equipmentCode").type(JsonFieldType.STRING).description("equipmentCode"),
								fieldWithPath("equipmentStatus").type(JsonFieldType.STRING).description("equipmentStatus"),
								fieldWithPath("name").type(JsonFieldType.STRING).description("Tooling name"),
								fieldWithPath("runnerType").type(JsonFieldType.STRING).description("runnerType"),
								fieldWithPath("size").type(JsonFieldType.STRING).description("size"),
								fieldWithPath("sizeUnit").type(JsonFieldType.STRING).description("sizeUnit"),
								fieldWithPath("counterCode").type(JsonFieldType.STRING).description("counterCode"),
								fieldWithPath("contractedCycleTimeSeconds").type(JsonFieldType.STRING).description("Approval Cycle Time (Sec)"),
								//fieldWithPath("locationTitle").type(JsonFieldType.STRING).description("locationTitle"),
								fieldWithPath("locationCode").type(JsonFieldType.STRING).description("locationCode"),
								//fieldWithPath("locationCity").type(JsonFieldType.STRING).description("locationCity"),
								//fieldWithPath("lastShot").type(JsonFieldType.NUMBER).description("lastShot"),
								fieldWithPath("lastShotDateTime").type(JsonFieldType.STRING).description("lastShotDateTime"),
								//fieldWithPath("operatingStatus").type(JsonFieldType.STRING).description("operatingStatus"),
								fieldWithPath("operatedDateTime").type(JsonFieldType.STRING).description("operatedDateTime"),
								fieldWithPath("companyName").type(JsonFieldType.STRING).description("companyName"),
								fieldWithPath("toolMakerCompanyName").type(JsonFieldType.STRING).description("toolMakerCompanyName"),
								fieldWithPath("createdDateTime").type(JsonFieldType.STRING).description("createdDateTime")
						)

				))
		;
	}


	@Test
	@DisplayName("Tooling - report")
	void getMoldReport() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		payload.put("moldId", 24470);
		payload.put("year", "2018");
		payload.put("chartDataType", "QUANTITY");
		payload.put("dateViewType", "DAY");



		mockMvc.perform(get("/apis/molds/report")
				.header(HttpHeaders.AUTHORIZATION, getBearerToken(UserType.HQ))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(payload))
		)
				.andDo(print())
				.andExpect(status().isOk())


				.andDo(document("tooling-report",
						requestHeaders(
								headerWithName("Authorization").description("Bearer accessToken")
						),

						relaxedRequestFields(
								fieldWithPath("moldId").type(JsonFieldType.NUMBER).description("Tooling ID"),
								fieldWithPath("year").type(JsonFieldType.STRING).description("Year"),
								fieldWithPath("chartDataType").type(JsonFieldType.STRING).description("Data type : QUANTITY, CYCLE_TIME, UPTIME"),
								fieldWithPath("dateViewType").type(JsonFieldType.STRING).description("Data view: DAY, WEEK, MONTH")
								),
						relaxedResponseFields(
								fieldWithPath("[].title").type(JsonFieldType.STRING).description("Date"),
								fieldWithPath("[].data").type(JsonFieldType.NUMBER).description("Quantity data"),
								fieldWithPath("[].cycleTime").type(JsonFieldType.NUMBER).description("cycleTime"),
								fieldWithPath("[].maxCycleTime").type(JsonFieldType.NUMBER).description("maxCycleTime"),
								fieldWithPath("[].minCycleTime").type(JsonFieldType.NUMBER).description("minCycleTime"),
								fieldWithPath("[].contractedCycleTime").type(JsonFieldType.NUMBER).description("contractedCycleTime"),
								fieldWithPath("[].cycleTimeMinusL1").type(JsonFieldType.NUMBER).description("cycleTimeMinusL1"),
								fieldWithPath("[].cycleTimeMinusL2").type(JsonFieldType.NUMBER).description("cycleTimeMinusL2"),
								fieldWithPath("[].cycleTimePlusL1").type(JsonFieldType.NUMBER).description("cycleTimePlusL1"),
								fieldWithPath("[].cycleTimePlusL2").type(JsonFieldType.NUMBER).description("cycleTimePlusL2"),
								fieldWithPath("[].cycleTimeWithin").type(JsonFieldType.NUMBER).description("cycleTimeWithin"),
								fieldWithPath("[].cycleTimeL1").type(JsonFieldType.NUMBER).description("cycleTimeL1"),
								fieldWithPath("[].cycleTimeL2").type(JsonFieldType.NUMBER).description("cycleTimeL2"),
								fieldWithPath("[].uptime").type(JsonFieldType.NUMBER).description("uptime"),
								fieldWithPath("[].uptimeMinute").type(JsonFieldType.NUMBER).description("uptimeMinute"),
								fieldWithPath("[].uptimeHour").type(JsonFieldType.NUMBER).description("uptimeHour")


						)

				))
		;
	}


	@Test
	@DisplayName("Parts - list")
	void getParts() throws Exception {

		MoldParam param = MoldParam.builder()
				.query("CASE_TYPE")
				.status("active")
				.sort("id,desc")
				.extraStatus("op-status-is-not-null")
				.page(1)
				.build();

		mockMvc.perform(get("/apis/parts")
				.header(HttpHeaders.AUTHORIZATION, getBearerToken(UserType.HQ))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(param))
		)
				.andDo(print())
				.andExpect(status().isOk())


				.andDo(document("parts-list",
						requestHeaders(
								headerWithName("Authorization").description("Bearer accessToken")
						),

						relaxedRequestFields(
								fieldWithPath("query").type(JsonFieldType.STRING).optional().description("keyword"),
								fieldWithPath("operatingStatus").type(JsonFieldType.STRING).optional().description("Operating Status"),
								fieldWithPath("equipmentStatus").type(JsonFieldType.STRING).optional().description("Equipment Status"),
								fieldWithPath("page").type(JsonFieldType.NUMBER).optional().description("Page number"),
								fieldWithPath("query").type(JsonFieldType.STRING).optional().description("keyword"),
								fieldWithPath("status").type(JsonFieldType.STRING).optional().description("TAB Status (all, )"),
								fieldWithPath("sort").type(JsonFieldType.STRING).optional().description("Data Sort. (id,desc Default)"),
								fieldWithPath("extraStatus").type(JsonFieldType.STRING).optional().description("Default : 'op-status-is-not-null'")

						),

						relaxedResponseFields(
								fieldWithPath("content.[].id").type(JsonFieldType.NUMBER).description("Part ID"),
								fieldWithPath("content.[].name").type(JsonFieldType.STRING).description("Part name"),
								fieldWithPath("content.[].partCode").type(JsonFieldType.STRING).description("Part Code"),
								fieldWithPath("content.[].categoryName").type(JsonFieldType.STRING).description("categoryName"),
								fieldWithPath("content.[].projectName").type(JsonFieldType.STRING).description("projectName"),
								fieldWithPath("content.[].partSize").type(JsonFieldType.STRING).description("Part size"),
								fieldWithPath("content.[].partWeight").type(JsonFieldType.STRING).description("Part Weight"),
								fieldWithPath("content.[].totalMolds").type(JsonFieldType.NUMBER).description("totalMolds"),
								fieldWithPath("content.[].activeMolds").type(JsonFieldType.NUMBER).description("activeMolds"),
								fieldWithPath("content.[].idleMolds").type(JsonFieldType.NUMBER).description("idleMolds"),
								fieldWithPath("content.[].inactiveMolds").type(JsonFieldType.NUMBER).description("inactiveMolds"),
								fieldWithPath("content.[].disconnectedMolds").type(JsonFieldType.NUMBER).description("disconnectedMolds"),
								fieldWithPath("content.[].quantityProduced").type(JsonFieldType.NUMBER).description("quantityProduced"),
								fieldWithPath("content.[].createdDateTime").type(JsonFieldType.STRING).description("createdDateTime"),


								// Paging
								fieldWithPath("first").type(JsonFieldType.BOOLEAN).description("First Page. "),
								fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("Last Page. "),
								fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("total Pages "),
								fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("Total Elements"),
								fieldWithPath("size").type(JsonFieldType.NUMBER).description("size"),
								fieldWithPath("number").type(JsonFieldType.NUMBER).description("page number.")
						)



				))


		;
	}



	@Test
	@DisplayName("Parts - get")
	void getPart() throws Exception {

		MoldParam param = new MoldParam();
		param.setQuery("");
		/*MoldParam param = MoldParam.builder()
				.query("")
				.status("active")
				.sort("id,desc")
				.extraStatus("op-status-is-not-null")
				.page(1)
				.build();*/

		mockMvc.perform(get("/apis/parts/6334")
				.header(HttpHeaders.AUTHORIZATION, getBearerToken(UserType.HQ))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
		)
				.andDo(print())
				.andExpect(status().isOk())


				.andDo(document("parts-get",
						requestHeaders(
								headerWithName("Authorization").description("Bearer accessToken")
						),

						relaxedResponseFields(
								fieldWithPath("id").type(JsonFieldType.NUMBER).description("Part ID"),
								fieldWithPath("name").type(JsonFieldType.STRING).description("Part name"),
								fieldWithPath("partCode").type(JsonFieldType.STRING).description("Part Code"),
								fieldWithPath("categoryName").type(JsonFieldType.STRING).description("categoryName"),
								fieldWithPath("projectName").type(JsonFieldType.STRING).description("projectName"),
								fieldWithPath("partSize").type(JsonFieldType.STRING).description("Part size"),
								fieldWithPath("partWeight").type(JsonFieldType.STRING).description("Part Weight"),
								fieldWithPath("totalMolds").type(JsonFieldType.NUMBER).description("totalMolds"),
								fieldWithPath("activeMolds").type(JsonFieldType.NUMBER).description("activeMolds"),
								fieldWithPath("idleMolds").type(JsonFieldType.NUMBER).description("idleMolds"),
								fieldWithPath("inactiveMolds").type(JsonFieldType.NUMBER).description("inactiveMolds"),
								fieldWithPath("disconnectedMolds").type(JsonFieldType.NUMBER).description("disconnectedMolds"),
								fieldWithPath("quantityProduced").type(JsonFieldType.NUMBER).description("quantityProduced"),
								fieldWithPath("createdDateTime").type(JsonFieldType.STRING).description("createdDateTime")

						)



				))


		;
	}


	@Test
	@DisplayName("Parts - report")
	void getPartReport() throws Exception {
		Map<String, Object> payload = new HashMap<>();
		payload.put("partId", 45010);
		payload.put("year", "2018");
		payload.put("chartDataType", "QUANTITY");
		payload.put("dateViewType", "DAY");



		mockMvc.perform(get("/apis/parts/report")
				.header(HttpHeaders.AUTHORIZATION, getBearerToken(UserType.HQ))
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(payload))
		)
				.andDo(print())
				.andExpect(status().isOk())


				.andDo(document("parts-report",
						requestHeaders(
								headerWithName("Authorization").description("Bearer accessToken")
						),

						relaxedRequestFields(
								fieldWithPath("partId").type(JsonFieldType.NUMBER).description("Part ID"),
								fieldWithPath("year").type(JsonFieldType.STRING).description("Year"),
								fieldWithPath("chartDataType").type(JsonFieldType.STRING).description("Data type : QUANTITY"),
								fieldWithPath("dateViewType").type(JsonFieldType.STRING).description("Data view: DAY, WEEK, MONTH")
						),
						relaxedResponseFields(
								fieldWithPath("[].title").type(JsonFieldType.STRING).description("Date"),
								fieldWithPath("[].data").type(JsonFieldType.NUMBER).description("Quantity data"),
								fieldWithPath("[].moldCount").type(JsonFieldType.NUMBER).description("cycleTime")
						)

				))
		;
	}
}
