package io.github.junhyoung.nearbuy.user.service;

import io.github.junhyoung.nearbuy.jwt.service.JwtService;
import io.github.junhyoung.nearbuy.auth.dto.CustomOAuth2User;
import io.github.junhyoung.nearbuy.user.dto.request.UserDeleteRequestDto;
import io.github.junhyoung.nearbuy.user.dto.request.UserExistRequestDto;
import io.github.junhyoung.nearbuy.user.dto.response.UserResponseDto;
import io.github.junhyoung.nearbuy.user.entity.User;
import io.github.junhyoung.nearbuy.user.entity.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.dto.request.UserJoinRequestDto;
import io.github.junhyoung.nearbuy.user.dto.request.UserUpdateRequestDto;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.security.access.AccessDeniedException;

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
            throw new org.springframework.security.access.AccessDeniedException("본인 계정만 수정 가능합니다.");
        }

        // 조회
        User findUser = userRepository.findByUsernameAndIsLockAndIsSocial(userUpdateRequestDto.getUsername(), false, false)
                .orElseThrow(() -> new UsernameNotFoundException(userUpdateRequestDto.getUsername()));

        findUser.updateUser(userUpdateRequestDto);
        return userRepository.save(findUser).getId();
    }


    /**
     * 소셜 로그인 (매 로그인시 : 신규 = 가입, 기존 = 업데이트)
     */
    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        // 부모 메소드 호출
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 데이터
        Map<String, Object> attributes;
        List<GrantedAuthority> authorities;

        String username;
        String role = UserRoleType.USER.name();
        String email;
        String nickname;

        // provider 제공자별 데이터 획득 (GOOGLE / NAVER 구분)
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        if (registrationId.equals(SocialProviderType.NAVER.name())) {

            attributes = (Map<String, Object>) oAuth2User.getAttributes().get("response");
            username = registrationId + "_" + attributes.get("id");
            email = attributes.get("email").toString();
            nickname = attributes.get("nickname").toString();

        } else if (registrationId.equals(SocialProviderType.GOOGLE.name())) {

            attributes = (Map<String, Object>) oAuth2User.getAttributes();
            username = registrationId + "_" + attributes.get("sub");
            email = attributes.get("email").toString();
            nickname = attributes.get("name").toString();

        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        // 데이터베이스 조회 -> 존재하면 업데이트, 없으면 신규 가입
        Optional<User> entity = userRepository.findByUsernameAndIsLockAndIsSocial(username, true, true);

        if (entity.isPresent()) {
            // role 조회
            role = entity.get().getRoleType().name();

            // 기존 유저 업데이트
            UserUpdateRequestDto dto = new UserUpdateRequestDto();
            dto.setNickname(nickname);
            dto.setEmail(email);
            entity.get().updateUser(dto);

            userRepository.save(entity.get());
        } else {
            // 신규 유저 추가
            User newUserEntity = User.builder()
                    .username(username)
                    .password("")
                    .isLock(false)
                    .isSocial(true)
                    .socialProviderType(SocialProviderType.valueOf(registrationId))
                    .roleType(UserRoleType.USER)
                    .nickname(nickname)
                    .email(email)
                    .build();

            userRepository.save(newUserEntity);
        }

        authorities = List.of(new SimpleGrantedAuthority(role));

        return new CustomOAuth2User(attributes, authorities, username);
    }


    /**
     * 자체/소셜 유저 정보 조회
     */
    @Transactional(readOnly = true)
    public UserResponseDto readUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        User entity = userRepository.findByUsernameAndIsLock(username, false)
                .orElseThrow(() -> new UsernameNotFoundException("해당 유저를 찾을 수 없습니다: " + username));

        return new UserResponseDto(username, entity.getIsSocial(), entity.getNickname(), entity.getEmail());
    }

    /**
     * 자체/소셜 로그인 회원 탈퇴
     */
    @Transactional
    public void deleteUser(UserDeleteRequestDto dto) throws AccessDeniedException {

        // 본인 및 어드민만 삭제 가능 검증
        SecurityContext context = SecurityContextHolder.getContext();
        String sessionUsername = context.getAuthentication().getName();
        String sessionRole = context.getAuthentication().getAuthorities().iterator().next().getAuthority();

        boolean isOwner = sessionUsername.equals(dto.getUsername());
        boolean isAdmin = sessionRole.equals("ROLE_"+UserRoleType.ADMIN.name());

        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("본인 혹은 관리자만 삭제할 수 있습니다.");
        }

        // 유저 제거
        userRepository.deleteByUsername(dto.getUsername());

        // Refresh 토큰 제거
        jwtService.removeRefreshUser(dto.getUsername());
    }


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
