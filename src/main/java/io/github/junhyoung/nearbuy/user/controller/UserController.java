package io.github.junhyoung.nearbuy.user.controller;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.global.common.ApiResponse;
import io.github.junhyoung.nearbuy.user.dto.request.*;
import io.github.junhyoung.nearbuy.user.dto.response.UserResponseDto;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/exist")
    public ResponseEntity<ApiResponse<Boolean>> existUserApi(@Validated @RequestBody UserExistRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(userService.isExistUser(dto)));
    }

    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Map<String, Long>>> joinApi(@RequestBody @Validated UserJoinRequestDto dto) {
        Long id = userService.join(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(Collections.singletonMap("userId", id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserApi(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(ApiResponse.success(userService.readUserById(userPrincipal.id())));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<Long>> updateLocalUserApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                @Validated @RequestBody UserUpdateRequestDto dto) {
        return ResponseEntity.ok(ApiResponse.success(userService.updateLocalUser(userPrincipal.id(), dto)));
    }

    @PostMapping("/password")
    public ResponseEntity<ApiResponse<Void>> updateUserPasswordApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                                   @Validated @RequestBody UserUpdatePasswordRequestDto dto) {
        userService.updateUserPassword(userPrincipal.id(), dto);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteUserApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                           @Validated @RequestBody UserDeleteRequestDto dto) {
        UserRoleType role = UserRoleType.fromRoleName(userPrincipal.getRole());
        userService.deleteUser(userPrincipal.id(), dto.getTargetId(), role);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
