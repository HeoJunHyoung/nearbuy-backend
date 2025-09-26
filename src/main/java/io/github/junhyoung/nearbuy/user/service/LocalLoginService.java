package io.github.junhyoung.nearbuy.user.service;

import io.github.junhyoung.nearbuy.user.entity.UserEntity;
import io.github.junhyoung.nearbuy.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalLoginService implements UserDetailsService {

    private final UserRepository userRepository;

    // LoginFilter에서 Provider가 사용자 정보를 가져와 달라고 요청을 보냈는데, 이 요청을 받는것이 UserDetailsService이다.
    // ㄴ 즉, 여기서는 DB로부터 UserData를 가져와서 반환해준다.

    // 반환해주는 객체는 Spring Security가 이해할 수 있는 UserDetails이여 하며,
    // 해당 코드에서는 User(Spring Security가 제공해주는 기본 Security 객체)를 build를 통해 반환하고 있다.

    // 이렇게 UserDetails를 return 하면, Provider(DaoAuthenticationProvider)가 LoginFilter에서 넘겨줬던 평문 비밀번호와 방금 DB로부터 가져온 비밀번호를 비교한다.
    // 만약 비밀번호가 서로 일치한다면, Provider는 [사용자 정보] + [ROLE_USER] 정보를 담은 Authentication 객체를 생성해서,
    // AuthenticationManager를 거쳐서 LoginFilter로 최종 반환한다.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByUsernameAndIsLockAndIsSocial(username, false, false)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없거나 잠긴 계정, 혹은 소셜 계정입니다: " + username));

        return User.builder()   // UserDetails를 상속받는 User 객체 반환 (해당 User 객체는 Spring Security에서 기본적으로 제공하는 클래스 객체)
                .username(entity.getUsername())
                .password(entity.getPassword())
                .roles(entity.getRoleType().name())
                .accountLocked(entity.getIsLock())
                .build();
    }

}
