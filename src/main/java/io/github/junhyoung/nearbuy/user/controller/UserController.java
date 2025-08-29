package io.github.junhyoung.nearbuy.user.controller;

import io.github.junhyoung.nearbuy.user.dto.UserExistRequestDto;
import io.github.junhyoung.nearbuy.user.dto.UserJoinRequestDto;
import io.github.junhyoung.nearbuy.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 유저 수정 (자체 로그인 유저만)

    // 유저 제거 (자체/소셜)

}
