package io.github.junhyoung.nearbuy.user.service;

import io.github.junhyoung.nearbuy.auth.token.service.JwtService;
import io.github.junhyoung.nearbuy.auth.web.dto.CustomOAuth2User;
import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.user.dto.request.*;
import io.github.junhyoung.nearbuy.user.dto.response.UserResponseDto;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.entity.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService extends DefaultOAuth2UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // =================================================================
    // Public API Methods
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
    public UserResponseDto readUser(String username) {
        UserEntity entity = findUserByUsername(username);
        return new UserResponseDto(entity.getUsername(), entity.getIsSocial(), entity.getNickname(), entity.getEmail());
    }

    /**
     * 유저를 탈퇴 처리
     */
    @Transactional
    public void deleteUser(UserPrincipal userPrincipal, UserDeleteRequestDto dto) throws AccessDeniedException {
        validateDeleteAuthorization(userPrincipal, dto.getUsername());

        userRepository.deleteByUsername(dto.getUsername());
        jwtService.removeRefreshUser(dto.getUsername());
    }

    // =================================================================
    // Spring Security Overridden Methods
    // =================================================================

    /**
     * 자체 로그인을 위한 유저 정보를 로드
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없거나 잠긴 계정, 혹은 소셜 계정입니다: " + username));

        return User.builder()
                .username(entity.getUsername())
                .password(entity.getPassword())
                .roles(entity.getRoleType().name())
                .accountLocked(entity.getIsLock())
                .build();
    }

    /**
     * 소셜 로그인(OAuth2)을 위한 유저 정보를 로드하거나 업데이트
     */
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 1. 소셜 서비스로부터 받은 정보를 표준화된 형태로 파싱
        SocialUserAttributes attributes = parseSocialUserAttributes(userRequest, oAuth2User);

        // 2. DB에 유저가 있는지 확인하고, 없으면 생성, 있으면 정보를 업데이트
        UserEntity user = processSocialUser(attributes);

        // 3. 인증된 세션을 위한 Principal 객체를 생성
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRoleType().name()));
        UserPrincipal userPrincipal = new UserPrincipal(user.getId(), user.getUsername(), authorities);

        return new CustomOAuth2User(userPrincipal, attributes.attributes());
    }


    // =================================================================
    // Private Helper Methods
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
     * 소셜 유저 정보를 임시로 담기 위한 record 클래스.
     */
    private record SocialUserAttributes(String username, String email, String nickname, String registrationId, Map<String, Object> attributes) {}

    /**
     * 소셜 서비스 제공자(Naver, Google)에 따라 유저 정보를 파싱
     */
    private SocialUserAttributes parseSocialUserAttributes(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        return switch (SocialProviderType.valueOf(registrationId)) {
            case NAVER -> {
                Map<String, Object> response = (Map<String, Object>) attributes.get("response");
                String username = registrationId + "_" + response.get("id");
                yield new SocialUserAttributes(username, (String) response.get("email"), (String) response.get("nickname"), registrationId, response);
            }
            case GOOGLE -> {
                String username = registrationId + "_" + attributes.get("sub");
                yield new SocialUserAttributes(username, (String) attributes.get("email"), (String) attributes.get("name"), registrationId, attributes);
            }
            default -> throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인 제공자입니다: " + registrationId);
        };
    }

    /**
     * 파싱된 소셜 유저 정보를 바탕으로 DB 작업을 처리
     */
    private UserEntity processSocialUser(SocialUserAttributes attributes) {
        Optional<UserEntity> userOptional = userRepository.findByUsernameAndIsSocial(attributes.username(), true);

        if (userOptional.isPresent()) {
            // 기존 유저가 존재하면, 최신 소셜 정보로 업데이트
            UserEntity existingUser = userOptional.get();
            UserUpdateRequestDto updateDto = new UserUpdateRequestDto();
            updateDto.setEmail(attributes.email());
            updateDto.setNickname(attributes.nickname());
            existingUser.updateUser(updateDto);
            return userRepository.save(existingUser);
        } else {
            // 신규 유저이면, 새로 생성
            UserEntity newUser = UserEntity.builder()
                    .username(attributes.username())
                    .password("") // 소셜 유저는 비밀번호가 없음
                    .isLock(false)
                    .isSocial(true)
                    .socialProviderType(SocialProviderType.valueOf(attributes.registrationId()))
                    .roleType(UserRoleType.USER)
                    .nickname(attributes.nickname())
                    .email(attributes.email())
                    .build();
            return userRepository.save(newUser);
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