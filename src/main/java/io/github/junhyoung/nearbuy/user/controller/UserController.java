package io.github.junhyoung.nearbuy.user.controller;

import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.user.dto.request.*;
import io.github.junhyoung.nearbuy.user.dto.response.UserResponseDto;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.service.UserService;
import lombok.RequiredArgsConstructor;
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

    // 회원가입 시 username 검증 (프론트엔드에서 실시간으로 요청)
    @PostMapping("/exist")
    public ResponseEntity<Boolean> existUserApi(@Validated @RequestBody UserExistRequestDto userExistRequestDto) {
        return ResponseEntity.ok(userService.isExistUser(userExistRequestDto));
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, Long>> joinApi(@RequestBody @Validated UserJoinRequestDto userJoinRequestDto) {
        Long id = userService.join(userJoinRequestDto);
        Map<String, Long> responseBody = Collections.singletonMap("userId", id);
        return ResponseEntity.status(201).body(responseBody);
    }

    // 유저 정보 조회
    @GetMapping
    public UserResponseDto getUserApi(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return userService.readUserById(userPrincipal.id());
    }

    // 유저 정보 수정 (자체 로그인 유저만 가능)
    @PatchMapping
    public ResponseEntity<Long> updateLocalUserApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                              @Validated @RequestBody UserUpdateRequestDto dto) {
        return ResponseEntity.status(200).body(userService.updateLocalUser(userPrincipal.id(), dto));
    }

    @PostMapping("/password")
    public ResponseEntity<String> updateUserPasswordApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                        @Validated @RequestBody UserUpdatePasswordRequestDto dto) {
        userService.updateUserPassword(userPrincipal.id(), dto);
        return ResponseEntity.status(200).body("비밀번호 변경이 완료되었습니다.");
    }

    // 유저 제거 (자체/소셜)
    @DeleteMapping
    public ResponseEntity<Boolean> deleteUserApi(@AuthenticationPrincipal UserPrincipal userPrincipal,
                                                 @Validated @RequestBody UserDeleteRequestDto dto) {
        UserRoleType currentUserRole = UserRoleType.fromRoleName(userPrincipal.getRole());
        userService.deleteUser(userPrincipal.id(), dto.getTargetId(), currentUserRole);
        return ResponseEntity.status(200).body(true);
    }

}
