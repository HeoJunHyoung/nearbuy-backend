package io.github.junhyoung.nearbuy.user.service;

import io.github.junhyoung.nearbuy.auth.token.service.JwtService;
import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.global.exception.business.InvalidPasswordException;
import io.github.junhyoung.nearbuy.global.exception.business.UserAlreadyExistException;
import io.github.junhyoung.nearbuy.global.exception.business.UserNotFoundException;
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
     * 현재 로그인한 유저의 정보를 조회
     */
    public UserResponseDto readUserById(Long userId) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

        return UserResponseDto.createUserResponseDto(entity);
    }

    /**
     * 기존 유저의 정보를 수정
     */
    @Transactional
    public Long updateLocalUser(Long userId, UserUpdateRequestDto dto) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

        entity.updateUser(dto.getNickname(), dto.getEmail());
        return entity.getId();
    }

    @Transactional
    public void updateUserPassword(Long userId, UserUpdatePasswordRequestDto dto) {
        UserEntity entity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 회원입니다."));

        // 엔티티의 비즈니스 메서드 호출
        entity.updateUserPassword(
                dto.getOriginPassword(),
                dto.getNewPassword(),
                dto.getNewConfirmPassword(),
                passwordEncoder
        );
    }

    /**
     * 유저 탈퇴 처리
     */
    @Transactional
    public void deleteUser(Long currentUserId, Long targetId, UserRoleType currentUserRole){
        UserEntity userToDelete = userRepository.findById(targetId)
                .orElseThrow(() -> new UserNotFoundException("삭제할 사용자를 찾을 수 없습니다."));

        validateDeleteAuthorization(currentUserId, targetId, currentUserRole);

        userRepository.delete(userToDelete);
        jwtService.removeRefreshUser(userToDelete.getId()); // userId 전달
    }




    // =================================================================
    // Private Helper Methods (비즈니스 로직 관련 헬퍼)
    // =================================================================

    /**
     * 회원가입 요청의 유효성을 검증
     */
    private void validateJoinRequest(UserJoinRequestDto dto) {
        if (!dto.getOriginPassword().equals(dto.getConfirmPassword())) {
            throw new InvalidPasswordException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new UserAlreadyExistException("이미 존재하는 아이디입니다.");
        }
    }

    /**
     * 유저 삭제 권한을 검증
     */
    private void validateDeleteAuthorization(Long currentUserId, Long targetUserId, UserRoleType currentUserRole) {
        boolean isOwner = currentUserId.equals(targetUserId);
        boolean isAdmin = currentUserRole == UserRoleType.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("계정을 삭제할 권한이 없습니다.");
        }
    }
}