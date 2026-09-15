package com.tyt.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/**
 * 커밋된 docs/openapi.yaml이 코드에서 생성한 스펙과 같은지 검사한다.
 * API를 바꿨다면 OPENAPI_UPDATE=true ./gradlew test --tests '*OpenApiSpecTest'로 파일을 갱신하고 함께 커밋한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSpecTest {

	private static final Path SPEC_PATH = Path.of("..", "docs", "openapi.yaml");
	private static final String UPDATE_GUIDE =
		"OPENAPI_UPDATE=true ./gradlew test --tests '*OpenApiSpecTest'로 갱신하고 함께 커밋하세요.";

	@Autowired
	private MockMvc mockMvc;

	@DisplayName("docs/openapi.yaml이 코드에서 생성한 스펙과 같다")
	@Test
	void specIsUpToDate() throws Exception {
		String generated = normalize(mockMvc.perform(get("/v3/api-docs.yaml"))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(StandardCharsets.UTF_8));

		if ("true".equals(System.getenv("OPENAPI_UPDATE"))) {
			Files.createDirectories(SPEC_PATH.getParent());
			Files.writeString(SPEC_PATH, generated, StandardCharsets.UTF_8);
		}

		assertThat(SPEC_PATH).as("docs/openapi.yaml이 없습니다. " + UPDATE_GUIDE).exists();
		assertThat(normalize(Files.readString(SPEC_PATH, StandardCharsets.UTF_8)))
			.as("docs/openapi.yaml이 코드와 다릅니다. " + UPDATE_GUIDE)
			.isEqualTo(generated);
	}

	/**
	 * git autocrlf로 체크아웃하면 CRLF가 되므로 줄바꿈을 맞춰 비교한다.
	 */
	private static String normalize(String yaml) {
		return yaml.replace("\r\n", "\n").stripTrailing() + "\n";
	}
}
