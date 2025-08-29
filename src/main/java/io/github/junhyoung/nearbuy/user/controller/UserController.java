package io.github.junhyoung.nearbuy.user.controller;

import io.github.junhyoung.nearbuy.user.dto.request.UserDeleteRequestDto;
import io.github.junhyoung.nearbuy.user.dto.request.UserExistRequestDto;
import io.github.junhyoung.nearbuy.user.dto.request.UserJoinRequestDto;
import io.github.junhyoung.nearbuy.user.dto.request.UserUpdateRequestDto;
import io.github.junhyoung.nearbuy.user.dto.response.UserResponseDto;
import io.github.junhyoung.nearbuy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    // 자체 로그인 유저 존재 확인
    @PostMapping("/exist")
    public ResponseEntity<Boolean> existUserApi(@Validated @RequestBody UserExistRequestDto userExistRequestDto) {
        return ResponseEntity.ok(userService.isExistUser(userExistRequestDto));
    }

    // 회원가입
    @PostMapping("/join")
    public ResponseEntity<Map<String, Long>> joinApi(@RequestBody @Validated UserJoinRequestDto userJoinRequestDto) {
        Long id = userService.join(userJoinRequestDto);
        Map<String, Long> responseBody = Collections.singletonMap("userId", id); // Json 형식으로 반환
        return ResponseEntity.status(201).body(responseBody);
    }

    // 유저 정보
    @GetMapping
    public UserResponseDto userMeApi() {
        return userService.readUser();
    }

    // 유저 수정 (자체 로그인 유저만)
    @PutMapping
    public ResponseEntity<Long> updateUserApi(@Validated @RequestBody UserUpdateRequestDto dto) throws AccessDeniedException {
        return ResponseEntity.status(200).body(userService.updateUser(dto));
    }

    // 유저 제거 (자체/소셜)
    @DeleteMapping
    public ResponseEntity<Boolean> deleteUserApi(@Validated @RequestBody UserDeleteRequestDto dto) throws AccessDeniedException {
        userService.deleteUser(dto);
        return ResponseEntity.status(200).body(true);
    }

}
