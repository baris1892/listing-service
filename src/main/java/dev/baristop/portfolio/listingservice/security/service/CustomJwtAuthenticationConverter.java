package dev.baristop.portfolio.listingservice.security.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.baristop.portfolio.listingservice.security.dto.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Converts a Jwt token into a Spring Security Authentication object.
 * <p>
 * This converter extracts the user's email, subject (sub), and roles from the JWT.
 * <p>
 * Key points:
 * <ul>
 * <li>Roles are taken from the <code>realm_access</code> claim.</li>
 * <li>The default realm role "default-roles-portfolio" is ignored.</li>
 * <li>ROLE_ prefix is automatically added if missing to comply with Spring Security conventions.</li>
 * <li>The resulting <code>UsernamePasswordAuthenticationToken</code> contains a UserPrincipal with ID, email, and granted authorities.</li>
 * </ul>
 * <p>
 * This is suitable for our project, where we rely on Keycloak's realm roles for authorization.
 */
@Component
@Slf4j
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        // logJwt(jwt);

        String email = Optional.ofNullable(jwt.getClaimAsString("email"))
            .orElseThrow(() -> new IllegalArgumentException("User JWT missing 'email' claim"));
        String id = Optional.ofNullable(jwt.getClaimAsString("sub"))
            .orElseThrow(() -> new IllegalArgumentException("User JWT missing 'sub' claim"));

        // extract realm_access roles
        @SuppressWarnings("unchecked")
        List<String> roles = Optional.ofNullable(jwt.getClaimAsMap("realm_access"))
            .map(map -> (List<String>) map.get("roles"))
            .orElse(List.of());

        // Optional: ignore "default-roles-portfolio"
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .filter(role -> !"default-roles-portfolio".equals(role))
            .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

        UserPrincipal principal = new UserPrincipal(id, email, authorities);
        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }

    @SuppressWarnings("unused")
    private void logJwt(Jwt jwt) {
        try {
            ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            log.info("JWT Claims:\n{}", objectMapper.writeValueAsString(jwt.getClaims()));
        } catch (Exception e) {
            log.error("Failed to serialize JWT claims", e);
        }
    }
}
