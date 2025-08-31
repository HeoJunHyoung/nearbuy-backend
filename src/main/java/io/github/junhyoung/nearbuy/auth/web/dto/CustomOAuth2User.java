package io.github.junhyoung.nearbuy.auth.web.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private final UserPrincipal userPrincipal;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UserPrincipal userPrincipal, Map<String, Object> attributes) {
        this.userPrincipal = userPrincipal;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.userPrincipal.authorities();
    }

    @Override
    public String getName() {
        return this.userPrincipal.username();
    }

    public UserPrincipal getUserPrincipal() {
        return this.userPrincipal;
    }
}