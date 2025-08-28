package io.github.junhyoung.nearbuy.user.service;

import io.github.junhyoung.nearbuy.user.domain.User;
import io.github.junhyoung.nearbuy.user.domain.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.dto.UserRequestDto;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 자체 로그인 회원 존재 여부 확인 - 프론트단에서의 검증
     */
    public Boolean isExistUser(UserRequestDto userRequestDto) {
        return userRepository.existsByUsername(userRequestDto.getUsername());
    }

    /**
     * 자체 로그인 회원 가입
     */

    @Transactional
    public Long join(UserRequestDto userRequestDto) {

        // 자체 로그인 회원 존재 여부 확인 - 백엔드단에서 검증
        if (userRepository.existsByUsername(userRequestDto.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }

        User user = User.builder()
                .username(userRequestDto.getUsername())
                .password(passwordEncoder.encode(userRequestDto.getPassword()))
                .isLock(false)
                .isSocial(false)
                .roleType(UserRoleType.USER)
                .nickname(userRequestDto.getNickname())
                .email(userRequestDto.getEmail())
                .build();

        return userRepository.save(user).getId();
    }


    // 자체 로그인


    /**
     * 자체 로그인 회원 정보 수정
     * ㄴ 자체 로그인 여부 확인 (소셜 로그인인지 아닌지 확인)
     * ㄴ 사용자 잠김 여부 확인
      */
    @Transactional
    public Long updateUser(UserRequestDto userRequestDto) throws AccessDeniedException {

        // 본인 계정에 대한 수정인지 검증
        String sessionUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!sessionUsername.equals(userRequestDto.getUsername())) {
            throw new AccessDeniedException("본인 계정만 수정 가능합니다.");
        }

        // 조회
        User findUser = userRepository.findByUsernameAndIsLockAndIsSocial(userRequestDto.getUsername(), false, false)
                .orElseThrow(() -> new UsernameNotFoundException(userRequestDto.getUsername()));

        findUser.updateUser(userRequestDto);

        return userRepository.save(findUser).getId();
    }

    // 자체/소셜 로그인 회원 탈퇴


    // 소셜 로그인 (매 로그인시 : 신규 = 가입, 기존 = 업데이트)


    // 자체/소셜 유저 정보 조회


}
