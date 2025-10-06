package dev.baristop.portfolio.listingservice.security;

import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;

@Slf4j
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser mockCustomUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        var authorities = Arrays.stream(mockCustomUser.roles())
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .map(SimpleGrantedAuthority::new)
            .toList();

        UserPrincipal principal = new UserPrincipal(mockCustomUser.id(), mockCustomUser.email(), authorities);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
            principal, "password", authorities
        );

        context.setAuthentication(auth);

        return context;
    }
}
