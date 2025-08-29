package io.github.junhyoung.nearbuy.user.service;

import io.github.junhyoung.nearbuy.user.dto.UserExistRequestDto;
import io.github.junhyoung.nearbuy.user.entity.User;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.dto.UserJoinRequestDto;
import io.github.junhyoung.nearbuy.user.dto.UserUpdateRequestDto;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 자체 로그인 회원 존재 여부 확인 - 프론트단에서의 검증
     */
    public Boolean isExistUser(UserExistRequestDto userExistRequestDto) {
        return userRepository.existsByUsername(userExistRequestDto.getUsername());
    }

    /**
     * 자체 로그인 회원 가입
     */
    @Transactional
    public Long join(UserJoinRequestDto userJoinRequestDto) {

        // 자체 로그인 회원 존재 여부 확인 - 백엔드단에서 검증
        validateDuplicateUser(userJoinRequestDto.getUsername());

        // 1차 비밀번호와 2차 비밀번호 검증
        if (!userJoinRequestDto.getOriginPassword().equals(userJoinRequestDto.getConfirmPassword())) {
            throw new IllegalArgumentException("1차 비밀번호와 2차 비밀번호가 서로 일치하지 않습니다.");
        }

        User user = User.builder()
                .username(userJoinRequestDto.getUsername())
                .password(passwordEncoder.encode(userJoinRequestDto.getOriginPassword()))
                .isLock(false)
                .isSocial(false)
                .roleType(UserRoleType.USER)
                .nickname(userJoinRequestDto.getNickname())
                .email(userJoinRequestDto.getEmail())
                .build();

        return userRepository.save(user).getId();
    }


    /**
     * 자체 로그인
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }


    /**
     * 자체 로그인 회원 정보 수정
     * ㄴ 자체 로그인 여부 확인 (소셜 로그인인지 아닌지 확인)
     * ㄴ 사용자 잠김 여부 확인
      */
    @Transactional
    public Long updateUser(UserUpdateRequestDto userUpdateRequestDto) throws AccessDeniedException {

        // 본인 계정에 대한 수정인지 검증
        String sessionUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!sessionUsername.equals(userUpdateRequestDto.getUsername())) {
            throw new AccessDeniedException("본인 계정만 수정 가능합니다.");
        }

        // 조회
        User findUser = userRepository.findByUsernameAndIsLockAndIsSocial(userUpdateRequestDto.getUsername(), false, false)
                .orElseThrow(() -> new UsernameNotFoundException(userUpdateRequestDto.getUsername()));

        findUser.updateUser(userUpdateRequestDto);
        return userRepository.save(findUser).getId();
    }

    // 자체/소셜 로그인 회원 탈퇴


    // 소셜 로그인 (매 로그인시 : 신규 = 가입, 기존 = 업데이트)


    // 자체/소셜 유저 정보 조회


    //== 내부 메서드 ==//
    /**
     * 중복 회원 검증
     */
    private void validateDuplicateUser(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }
    }


}
