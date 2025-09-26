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

// 1단계: 소셜 로그인 시작 (Spring Security의 OAuth2 필터)
// ㄴ 사용자가 프론트엔드에서 "구글 로그인" 버튼을 클릭하면, 브라우저는 GET /oauth2/authorization/google 같은 경로로 요청을 보냅니다.
// ㄴ 이 요청은 LoginFilter가 아닌, Spring Security에 내장된 OAuth2AuthorizationRequestRedirectFilter가 가로채 구글의 로그인 페이지로 사용자를 리다이렉트시킵니다.

// 2단계: 외부 소셜 서비스에서 인증
// ㄴ 사용자는 구글 페이지에서 자신의 아이디와 비밀번호로 로그인하고, 정보 제공에 동의합니다.
// ㄴ 로그인이 성공하면, 구글은 임시 인증 코드(Authorization Code)를 포함한 채, 사전에 등록된 우리 서버의 주소(Redirect URI)로 사용자를 다시 돌려보냅니다.

// 3단계: 사용자 정보 조회 (SocialLoginService)
// ㄴ Spring Security의 OAuth2LoginAuthenticationFilter가 이 콜백 요청을 받아, 구글로부터 받은 인증 코드를 구글 서버에 다시 보내 액세스 토큰(Access Token)으로 교환합니다.
// ㄴ 그 후, 이 액세스 토큰을 이용해 구글의 API 서버에서 사용자의 정보(이메일, 이름 등)를 가져옵니다.
// ㄴ 가져온 사용자 정보를 가지고 SecurityConfig에 등록된 user.service.SocialLoginService의 loadUser() 메서드를 호출합니다. 이 서비스의 역할은 다음과 같습니다.
//  ㄴ 비밀번호 검증이 없습니다. (인증 책임은 구글에 위임했기 때문입니다.)
//  ㄴ DB에서 해당 소셜 ID를 가진 사용자가 있는지 확인합니다.
//  ㄴ 기존 회원이면: 최신 정보(닉네임 등)로 업데이트합니다.
//  ㄴ 신규 회원이면: 비밀번호 없이 새로운 회원 정보를 DB에 저장합니다.
//  ㄴ CustomOAuth2User 객체를 생성하여 반환합니다.
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
            existingUser.updateUser(attributes.nickname(), attributes.email());
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