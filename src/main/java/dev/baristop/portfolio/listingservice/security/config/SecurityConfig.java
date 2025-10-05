package dev.baristop.portfolio.listingservice.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.baristop.portfolio.listingservice.response.ErrorResponse;
import dev.baristop.portfolio.listingservice.security.util.Role;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * Configures application security using Spring Security.
 *
 * <p>1. HTTP request authorization rules:
 * <ul>
 *   <li><code>/actuator/health</code> is public</li>
 *   <li><code>/actuator/**</code> requires ADMIN role</li>
 *   <li>all other requests are permitted</li>
 * </ul>
 *
 * <p>2. OAuth2 Resource Server configuration:
 * <ul>
 *   <li>Validates JWTs</li>
 *   <li>Uses {@link dev.baristop.portfolio.listingservice.security.service.CustomJwtAuthenticationConverter}
 *       to convert JWT claims into Spring AuthenticationPrincipal</li>
 * </ul>
 *
 * <p>3. Exception handling:
 * <ul>
 *   <li>Returns JSON {@link dev.baristop.portfolio.listingservice.response.ErrorResponse}
 *       for 401 (Unauthorized) and 403 (Forbidden)</li>
 * </ul>
 *
 * <p>4. Enables method-level security annotations with {@code @EnableMethodSecurity}.
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * @see dev.baristop.portfolio.listingservice.security.service.CustomJwtAuthenticationConverter
     */
    private final Converter<Jwt, AbstractAuthenticationToken> customJwtAuthenticationConverter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasAnyAuthority(Role.ADMIN)
                .anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt ->
                    /** JwtDecoder bean is autoconfigured based on application properties and active profile
                     * @see JwtDecoderConfig
                     * */
                    jwt.jwtAuthenticationConverter(customJwtAuthenticationConverter)
                )
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(getAuthenticationEntryPoint())
                .accessDeniedHandler(getAccessDeniedHandler())
            );

        return http.build();
    }

    private AccessDeniedHandler getAccessDeniedHandler() {
        return (request, response, accessDeniedException)
            -> writeErrorResponse(response, HttpStatus.FORBIDDEN, accessDeniedException.getMessage());
    }

    private AuthenticationEntryPoint getAuthenticationEntryPoint() {
        return (request, response, authException)
            -> writeErrorResponse(response, HttpStatus.UNAUTHORIZED, authException.getMessage());
    }

    private void writeErrorResponse(
        HttpServletResponse response,
        HttpStatus status,
        String message
    ) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");

        ErrorResponse errorResponse = ErrorResponse.of(status, message);
        String jsonResponse = new ObjectMapper().writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }
}
