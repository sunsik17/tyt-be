package com.tyt.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyt.auth.application.AuthCommandService;
import com.tyt.auth.application.dto.result.TokenResult;
import com.tyt.auth.presentation.dto.request.KakaoLoginRequest;
import com.tyt.auth.presentation.dto.request.ReissueTokenRequest;
import com.tyt.auth.presentation.dto.response.TokenResponse;
import com.tyt.auth.presentation.dto.response.constants.AuthSuccessCode;
import com.tyt.common.web.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "카카오 로그인과 토큰 재발급. 토큰 없이 호출한다.")
@SecurityRequirements
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthCommandService authCommandService;

	@Operation(
		summary = "카카오 로그인",
		description = """
			앱이 카카오 SDK 로그인으로 받은 카카오 액세스 토큰으로 로그인한다. 처음 보는 카카오 계정이면 가입까지 한다.
			응답의 accessToken은 이후 요청의 `Authorization: Bearer` 헤더에, refreshToken은 재발급에 쓴다. 두 토큰을 모두 저장한다."""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "`LOGIN_SUCCESS`"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "`INVALID_REQUEST`: accessToken이 비어 있다",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"INVALID_REQUEST","message":"accessToken: 공백일 수 없습니다","data":null}"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "`INVALID_SOCIAL_TOKEN`: 카카오 토큰이 만료됐거나 유효하지 않거나 다른 앱에서 발급됐다. 카카오 로그인부터 다시 한다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"INVALID_SOCIAL_TOKEN","message":"소셜 로그인 토큰이 유효하지 않습니다.","data":null}""")))
	})
	@PostMapping("/kakao/tokens")
	public ResponseEntity<ApiResponse<TokenResponse>> loginWithKakao(@Valid @RequestBody KakaoLoginRequest request) {
		TokenResult result = authCommandService.loginWithKakao(request.toCommand());

		return ResponseEntity.ok(ApiResponse.success(AuthSuccessCode.LOGIN_SUCCESS, TokenResponse.from(result)));
	}

	@Operation(
		summary = "토큰 재발급",
		description = """
			refresh 토큰으로 access와 refresh를 모두 새로 발급한다. 쓴 refresh 토큰은 바로 무효가 되므로 응답의 두 토큰으로 교체해 저장한다.
			같은 refresh 토큰으로 동시에 두 번 호출하면 하나만 성공하고 나머지는 401이다. 재발급 요청은 한 곳에서 한 번에 하나만 보낸다."""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "`TOKEN_REISSUED`"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "`INVALID_REQUEST`: refreshToken이 비어 있다",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"INVALID_REQUEST","message":"refreshToken: 공백일 수 없습니다","data":null}"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "`INVALID_REFRESH_TOKEN`: refresh 토큰이 만료·무효이거나, 이미 교체됐거나, 다른 기기에서 로그인해 무효가 됐다. 저장된 토큰을 지우고 로그인 화면으로 보낸다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"INVALID_REFRESH_TOKEN","message":"refresh 토큰이 유효하지 않습니다. 다시 로그인해야 합니다.","data":null}""")))
	})
	@PostMapping("/tokens")
	public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueTokenRequest request) {
		TokenResult result = authCommandService.reissue(request.toCommand());

		return ResponseEntity.ok(ApiResponse.success(AuthSuccessCode.TOKEN_REISSUED, TokenResponse.from(result)));
	}
}
