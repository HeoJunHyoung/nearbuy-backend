package io.github.junhyoung.nearbuy.auth.web.dto;


import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

public record UserPrincipal(Long id, String username, Collection<? extends GrantedAuthority> authorities) {
    public String getRole() {
        if (authorities == null || authorities.isEmpty()) {
            return null;
        }
        return authorities.iterator().next().getAuthority();
    }
}
