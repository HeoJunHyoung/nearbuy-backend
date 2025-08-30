package io.github.junhyoung.nearbuy.user.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;

import io.github.junhyoung.nearbuy.auth.web.dto.CustomOAuth2User;
import io.github.junhyoung.nearbuy.auth.web.dto.UserPrincipal;
import io.github.junhyoung.nearbuy.user.dto.request.UserUpdateRequestDto;
import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.entity.enumerate.SocialProviderType;
import io.github.junhyoung.nearbuy.user.entity.enumerate.UserRoleType;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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
@Transactional
public class SocialLoginService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    /**
     * 소셜 로그인(OAuth2)을 위한 유저 정보를 로드하거나 업데이트
     */
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
}