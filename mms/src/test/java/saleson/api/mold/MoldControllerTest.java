package saleson.api.mold;


import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import saleson.common.ApiControllerTest;
import saleson.enums.UserType;

import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MoldControllerTest extends ApiControllerTest {

	//@Test
	@DisplayName("")
	void getMolds() throws Exception {

		MoldParam param = MoldParam.builder()
				.query("")
				.status("all")
				.sort("id,desc")
				.page(1)
				.build();

		mockMvc.perform(get("/api/molds")
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

				requestFields(
						fieldWithPath("query").type(JsonFieldType.STRING).optional().description("keyword"),
						fieldWithPath("operatingStatus").type(JsonFieldType.STRING).optional().description("Operating Status"),
						fieldWithPath("equipmentStatus").type(JsonFieldType.STRING).optional().description("Equipment Status"),
						fieldWithPath("page").type(JsonFieldType.NUMBER).optional().description("Page number"),
						fieldWithPath("query").type(JsonFieldType.STRING).optional().description("keyword"),
						fieldWithPath("status").type(JsonFieldType.STRING).optional().description("TAB Status (all, )"),
						fieldWithPath("sort").type(JsonFieldType.STRING).optional().description("Data Sort. (id,desc Default)")

				),

				relaxedResponseFields(
						fieldWithPath("content.[]id").type(JsonFieldType.NUMBER).description("Tooling ID"),


						// Paging
						fieldWithPath("last").type(JsonFieldType.BOOLEAN).description("Last Page. "),
						fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("total Pages "),
						fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("Ltotal Elements"),
						fieldWithPath("size").type(JsonFieldType.NUMBER).description("size"),
						fieldWithPath("number").type(JsonFieldType.NUMBER).description("page number.")
				)



				))


				;
	}

}
