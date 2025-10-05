package dev.baristop.portfolio.listingservice.security.dto;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents the authenticated user in the system.
 * <p>
 * This record is used as the principal in Spring Security's authentication context.
 * It contains the user's unique identifier, email, and granted authorities (roles/permissions).
 * <p>
 * The {@link #toString()} method is overridden to display authority names instead of full objects,
 * which is useful for logging and debugging.
 */
public record UserPrincipal(String id, String email, Collection<? extends GrantedAuthority> authorities) {

    @Override
    public @NotNull String toString() {
        Set<String> authorityNames = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        return "UserPrincipal{" +
            "id='" + id + '\'' +
            ", email='" + email + '\'' +
            ", authorities=" + authorityNames +
            '}';
    }
}
