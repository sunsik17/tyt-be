package com.tyt.user.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyt.common.web.ApiResponse;
import com.tyt.user.application.UserQueryService;
import com.tyt.user.application.dto.result.UserResult;
import com.tyt.user.presentation.dto.response.UserResponse;
import com.tyt.user.presentation.dto.response.constants.UserSuccessCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "사용자")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserQueryService userQueryService;

	@Operation(
		summary = "내 정보 조회",
		description = "access 토큰의 사용자를 조회한다. 앱 시작 시 저장된 토큰이 아직 쓸 수 있는지 확인하는 용도로도 쓴다."
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "`USER_FOUND`"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "`UNAUTHENTICATED`: 토큰이 없거나 만료·무효다. 재발급 후 다시 호출한다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"UNAUTHENTICATED","message":"인증이 필요합니다.","data":null}"""))),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "`USER_NOT_FOUND`: 토큰은 유효하지만 사용자가 없다. 저장된 토큰을 지우고 로그인 화면으로 보낸다.",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiResponse.class),
				examples = @ExampleObject(value = """
					{"code":"USER_NOT_FOUND","message":"사용자를 찾을 수 없습니다.","data":null}""")))
	})
	@GetMapping("/me")
	public ResponseEntity<ApiResponse<UserResponse>> getMe(@AuthenticationPrincipal Long userId) {
		UserResult result = userQueryService.getById(userId);

		return ResponseEntity.ok(ApiResponse.success(UserSuccessCode.USER_FOUND, UserResponse.from(result)));
	}
}
