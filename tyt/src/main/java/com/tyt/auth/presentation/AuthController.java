package com.tyt.auth.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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

@Tag(name = "Auth", description = "카카오 로그인, 토큰 재발급, 로그아웃, 탈퇴. 로그인과 재발급만 토큰 없이 호출한다.")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthCommandService authCommandService;

	@Operation(
		summary = "카카오 로그인",
		description = """
			앱이 카카오 SDK 로그인으로 받은 카카오 액세스 토큰으로 로그인한다. 처음 보는 카카오 계정이면 가입까지 한다.
			앱은 서버와 같은 카카오 앱의 네이티브 앱 키를 써야 한다. 다른 앱에서 발급된 토큰은 거절한다.
			응답의 accessToken은 이후 요청의 `Authorization: Bearer` 헤더에, refreshToken은 재발급에 쓴다. 두 토큰을 모두 저장한다."""
	)
	@SecurityRequirements
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
			description = "`INVALID_SOCIAL_TOKEN`: 카카오 토큰이 만료됐거나 유효하지 않거나 다른 카카오 앱에서 발급됐다. 카카오 로그인부터 다시 한다.",
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
	@SecurityRequirements
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
			description = "`INVALID_REFRESH_TOKEN`: refresh 토큰이 만료·무효이거나, 이미 교체됐거나, 로그아웃·다른 기기 로그인으로 무효가 됐다. 저장된 토큰을 지우고 로그인 화면으로 보낸다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"INVALID_REFRESH_TOKEN","message":"refresh 토큰이 유효하지 않습니다. 다시 로그인해야 합니다.","data":null}""")))
	})
	@PostMapping("/tokens")
	public ResponseEntity<ApiResponse<TokenResponse>> reissue(@Valid @RequestBody ReissueTokenRequest request) {
		TokenResult result = authCommandService.reissue(request.toCommand());

		return ResponseEntity.ok(ApiResponse.success(AuthSuccessCode.TOKEN_REISSUED, TokenResponse.from(result)));
	}

	@Operation(
		summary = "로그아웃",
		description = """
			서버에 저장된 refresh 토큰을 지워 더 이상 재발급받을 수 없게 한다. access 토큰은 만료(최대 30분)까지 서명상 유효하므로 기기에서 두 토큰을 모두 지운다.
			응답과 상관없이 기기의 토큰을 지우고 로그인 화면으로 보낸다."""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "`LOGGED_OUT`"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "`UNAUTHENTICATED`: access 토큰이 없거나 만료·무효다. 재발급 후 다시 호출하거나, 기기의 토큰만 지우고 로그아웃을 마친다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"UNAUTHENTICATED","message":"인증이 필요합니다.","data":null}""")))
	})
	@DeleteMapping("/tokens")
	public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
		authCommandService.logout(userId);

		return ResponseEntity.ok(ApiResponse.success(AuthSuccessCode.LOGGED_OUT));
	}

	@Operation(
		summary = "탈퇴",
		description = """
			카카오 연결을 끊고 사용자와 모든 데이터를 완전히 삭제한다. 되돌릴 수 없으니 호출 전에 사용자 확인을 받는다.
			성공하면 기기의 토큰을 지우고 로그인 화면으로 보낸다. 같은 카카오 계정으로 다시 로그인하면 새 계정으로 가입된다."""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "`ACCOUNT_DELETED`"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "`UNAUTHENTICATED`: access 토큰이 없거나 만료·무효다. 재발급 후 다시 호출한다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"UNAUTHENTICATED","message":"인증이 필요합니다.","data":null}"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "500",
			description = "`SOCIAL_UNLINK_FAILED`: 카카오 연결 끊기에 실패해 아무것도 지우지 않았다. 잠시 후 다시 시도하게 한다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"SOCIAL_UNLINK_FAILED","message":"카카오 연결 끊기에 실패했습니다. 잠시 후 다시 시도해주세요.","data":null}""")))
	})
	@DeleteMapping("/accounts/me")
	public ResponseEntity<ApiResponse<Void>> withdraw(@AuthenticationPrincipal Long userId) {
		authCommandService.withdraw(userId);

		return ResponseEntity.ok(ApiResponse.success(AuthSuccessCode.ACCOUNT_DELETED));
	}
}
