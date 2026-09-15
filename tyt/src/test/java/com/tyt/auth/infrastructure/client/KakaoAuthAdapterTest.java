package com.tyt.auth.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tyt.auth.domain.exception.AuthErrorCode;
import com.tyt.auth.infrastructure.config.KakaoProperties;
import com.tyt.common.exception.BusinessException;

class KakaoAuthAdapterTest {

	private static final String TOKEN_INFO_URL = "https://kapi.kakao.com/v1/user/access_token_info";
	private static final Long OUR_APP_ID = 1L;
	private static final String ADMIN_KEY = "test-admin-key";
	private static final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

	private MockRestServiceServer server;
	private KakaoAuthAdapter kakaoAuthAdapter;

	@BeforeEach
	void setUp() {
		RestClient.Builder builder = RestClient.builder().baseUrl("https://kapi.kakao.com");
		server = MockRestServiceServer.bindTo(builder).build();
		kakaoAuthAdapter = new KakaoAuthAdapter(builder.build(), new KakaoProperties(OUR_APP_ID, ADMIN_KEY));
	}

	@DisplayName("우리 앱에서 발급된 토큰이면 카카오 회원번호를 돌려준다")
	@Test
	void getKakaoId() {
		server.expect(requestTo(TOKEN_INFO_URL))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer kakao-token"))
			.andRespond(withSuccess("{\"id\":12345,\"expires_in\":7199,\"app_id\":1}", MediaType.APPLICATION_JSON));

		assertThat(kakaoAuthAdapter.getKakaoId("kakao-token")).isEqualTo("12345");
		server.verify();
	}

	@DisplayName("다른 앱에서 발급된 토큰이면 INVALID_SOCIAL_TOKEN")
	@Test
	void getKakaoIdFromOtherApp() {
		server.expect(requestTo(TOKEN_INFO_URL))
			.andRespond(withSuccess("{\"id\":12345,\"expires_in\":7199,\"app_id\":999}", MediaType.APPLICATION_JSON));

		assertThatThrownBy(() -> kakaoAuthAdapter.getKakaoId("kakao-token"))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);
	}

	@DisplayName("카카오가 401을 주면 INVALID_SOCIAL_TOKEN")
	@Test
	void getKakaoIdWithInvalidToken() {
		server.expect(requestTo(TOKEN_INFO_URL))
			.andRespond(withStatus(HttpStatus.UNAUTHORIZED)
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"msg\":\"this access token does not exist\",\"code\":-401}"));

		assertThatThrownBy(() -> kakaoAuthAdapter.getKakaoId("kakao-token"))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.INVALID_SOCIAL_TOKEN);
	}

	@DisplayName("어드민 키와 회원번호로 카카오 연결을 끊는다")
	@Test
	void unlinkKakao() {
		server.expect(requestTo(UNLINK_URL))
			.andExpect(method(HttpMethod.POST))
			.andExpect(header(HttpHeaders.AUTHORIZATION, "KakaoAK " + ADMIN_KEY))
			.andExpect(content().formDataContains(Map.of("target_id_type", "user_id", "target_id", "12345")))
			.andRespond(withSuccess("{\"id\":12345}", MediaType.APPLICATION_JSON));

		assertThatCode(() -> kakaoAuthAdapter.unlinkKakao("12345")).doesNotThrowAnyException();
		server.verify();
	}

	@DisplayName("이미 연결이 끊긴 사용자(-101)면 성공으로 본다")
	@Test
	void unlinkAlreadyUnlinkedUser() {
		server.expect(requestTo(UNLINK_URL))
			.andRespond(withStatus(HttpStatus.BAD_REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"msg\":\"NotRegisteredUserException\",\"code\":-101}"));

		assertThatCode(() -> kakaoAuthAdapter.unlinkKakao("12345")).doesNotThrowAnyException();
	}

	@DisplayName("어드민 키가 틀리는 등 다른 에러면 SOCIAL_UNLINK_FAILED")
	@Test
	void unlinkWithOtherClientError() {
		server.expect(requestTo(UNLINK_URL))
			.andRespond(withStatus(HttpStatus.UNAUTHORIZED)
				.contentType(MediaType.APPLICATION_JSON)
				.body("{\"msg\":\"wrong appKey\",\"code\":-401}"));

		assertThatThrownBy(() -> kakaoAuthAdapter.unlinkKakao("12345"))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.SOCIAL_UNLINK_FAILED);
	}

	@DisplayName("카카오 서버 오류면 SOCIAL_UNLINK_FAILED")
	@Test
	void unlinkWithServerError() {
		server.expect(requestTo(UNLINK_URL))
			.andRespond(withServerError());

		assertThatThrownBy(() -> kakaoAuthAdapter.unlinkKakao("12345"))
			.isInstanceOf(BusinessException.class)
			.extracting(e -> ((BusinessException)e).getErrorCode())
			.isEqualTo(AuthErrorCode.SOCIAL_UNLINK_FAILED);
	}
}
