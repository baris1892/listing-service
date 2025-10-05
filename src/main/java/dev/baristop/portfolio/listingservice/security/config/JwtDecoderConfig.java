package dev.baristop.portfolio.listingservice.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuration for decoding JWTs in different environments.
 * <p>
 * Provides two beans:
 * <ol>
 * <li>{@code testJwtDecoder} – used in 'test' profile, decodes JWTs using a shared secret.</li>
 * <li>{@code realJwtDecoder} – used in 'dev' and 'prod' profiles, decodes JWTs using the
 * issuer's JSON Web Key Set (JWKS) endpoint (Keycloak in our case).
 * </li>
 * </ol>
 * <p>
 * The appropriate decoder is automatically selected based on the active Spring profile.
 * <p>
 * Usage:
 * <ul>
 * <li>In tests, provide <code>app.security.jwt.secret</code> for HMAC decoding.</li>
 * <li>In dev/prod, provide <code>spring.security.oauth2.resourceserver.jwt.issuer-uri</code> pointing to the Keycloak realm.</li>
 * </ul>
 */
@Configuration
@Slf4j
public class JwtDecoderConfig {

    @Bean
    @Profile({"test"})
    public JwtDecoder testJwtDecoder(@Value("${app.security.jwt.secret}") String secret) {
        log.info("JwtDecoderConfig: using testJwtDecoder for test");

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    @Profile({"dev", "prod"})
    public JwtDecoder realJwtDecoder(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer) {
        log.info("JwtDecoderConfig: using realJwtDecoder");

        // Use Spring helper to fetch JWKS from issuer
        return JwtDecoders.fromIssuerLocation(issuer);
    }
}
