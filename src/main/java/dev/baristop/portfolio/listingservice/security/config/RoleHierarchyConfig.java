package dev.baristop.portfolio.listingservice.security.config;

import dev.baristop.portfolio.listingservice.security.util.Role;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;

/**
 * Configures the Role Hierarchy for Spring Security.
 * <p>
 * In Spring Security, a role hierarchy allows roles to inherit authorities from other roles.
 * For example, if ROLE_ADMIN > ROLE_USER, any user with ROLE_ADMIN automatically has all privileges of ROLE_USER.
 * <p>
 * This is useful to avoid repetitive role checks and to simplify method-level security.
 * <p>
 * The ROLE_ prefix is automatically added to all roles to comply with Spring Security conventions.
 */
@Configuration
public class RoleHierarchyConfig {

    @Bean
    public RoleHierarchy roleHierarchy() {
        // @formatter:off
        // Automatically prepend "ROLE_" to each role in the hierarchy
        String hierarchy = String.join("\n",
            addRolePrefix(Role.ADMIN)   + " > " + addRolePrefix(Role.USER)
        );
        // @formatter:on

        return RoleHierarchyImpl.fromHierarchy(hierarchy);
    }

    // Helper method to add ROLE_ prefix
    private static String addRolePrefix(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);

        return handler;
    }
}
