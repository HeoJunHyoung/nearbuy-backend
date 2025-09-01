package io.github.junhyoung.nearbuy.user.service;

import io.github.junhyoung.nearbuy.auth.token.service.JwtService;
import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.user.dto.request.*;
import io.github.junhyoung.nearbuy.user.dto.response.UserResponseDto;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // =================================================================
    // Public API Methods (회원 비즈니스 로직)
    // =================================================================

    /**
     * 주어진 아이디의 유저가 존재하는지 확인
     */
    public Boolean isExistUser(UserExistRequestDto userExistRequestDto) {
        return userRepository.existsByUsername(userExistRequestDto.getUsername());
    }

    /**
     * 자체 신규 회원을 가입
     */
    @Transactional
    public Long join(UserJoinRequestDto userJoinRequestDto) {
        validateJoinRequest(userJoinRequestDto);

        UserEntity userEntity = UserEntity.builder()
                .username(userJoinRequestDto.getUsername())
                .password(passwordEncoder.encode(userJoinRequestDto.getOriginPassword()))
                .isLock(false)
                .isSocial(false)
                .roleType(UserRoleType.USER)
                .nickname(userJoinRequestDto.getNickname())
                .email(userJoinRequestDto.getEmail())
                .build();

        return userRepository.save(userEntity).getId();
    }

    /**
     * 기존 유저의 정보를 수정
     */
    @Transactional
    public Long updateUser(String username, UserUpdateRequestDto userUpdateRequestDto) {
        UserEntity userEntity = findUserByUsernameForUpdate(username);
        userEntity.updateUser(userUpdateRequestDto);
        return userEntity.getId();
    }

    @Transactional
    public void updateUserPassword(String username, UserUpdatePasswordRequestDto dto) {
        UserEntity findUser = userRepository.findByUsernameAndIsLock(username, false).orElseThrow(() -> new UsernameNotFoundException("회원이 존재하지 않습니다."));
        // 기존 비밀번호 검증
        validateOriginPassword(dto.getOriginPassword(), findUser.getPassword());
        // 새 비밀번호 확인 일치 검증
        validatePasswordConfirmation(dto.getNewPassword(), dto.getNewConfirmPassword());

        findUser.updateUserPassword(passwordEncoder.encode(dto.getNewPassword()));
    }

    /**
     * 현재 로그인한 유저의 정보를 조회
     */
    public UserResponseDto readUserByUsername(String username) {
        UserEntity entity = findUserByUsername(username);
        return new UserResponseDto(entity.getUsername(), entity.getIsSocial(), entity.getNickname(), entity.getEmail());
    }

    /**
     * 유저 탈퇴 처리
     */
    @Transactional
    public void deleteUser(UserPrincipal userPrincipal, UserDeleteRequestDto dto) throws AccessDeniedException {
        validateDeleteAuthorization(userPrincipal, dto.getUsername());

        userRepository.deleteByUsername(dto.getUsername());
        jwtService.removeRefreshUser(dto.getUsername());
    }


    // =================================================================
    // Private Helper Methods (비즈니스 로직 관련 헬퍼)
    // =================================================================

    private void validateOriginPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new IllegalArgumentException("기존 비밀번호가 일치하지 않습니다.");
        }
    }

    private void validatePasswordConfirmation(String newPassword, String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호가 서로 일치하지 않습니다.");
        }
    }

    /**
     * 회원가입 요청의 유효성을 검증
     */
    private void validateJoinRequest(UserJoinRequestDto dto) {
        if (!dto.getOriginPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }
    }

    /**
     * 유저 삭제 권한을 검증
     */
    private void validateDeleteAuthorization(UserPrincipal principal, String targetUsername) {
        boolean isOwner = principal.username().equals(targetUsername);
        boolean isAdmin = ("ROLE_" + UserRoleType.ADMIN.name()).equals(principal.getRole());

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("계정을 삭제할 권한이 없습니다.");
        }
    }

    /**
     * 아이디로 유저를 조회
     */
    private UserEntity findUserByUsername(String username) {
        return userRepository.findByUsernameAndIsLock(username, false)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));
    }

    /**
     * 아이디로 유저를 조회 (수정 전용)
     */
    private UserEntity findUserByUsernameForUpdate(String username) {
        return userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException("수정할 사용자를 찾을 수 없습니다: " + username));
    }
}