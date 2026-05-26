package PickitPickit.global.security;

import PickitPickit.global.response.ErrorStatus;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {

    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final JwtProperties jwtProperties;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/kakao/login",
                                "/api/auth/kakao/authorize",
                                "/api/auth/kakao/callback",
                                "/api/auth/token/reissue",
                                "/api/auth/logout"
                        ).permitAll()

                        .requestMatchers(
                                "/api/stores/**"
                        ).permitAll()

                        .requestMatchers(
                                "/api/auth/me",
                                "/api/onboarding/**",
                                "/api/reviews/**",
                                "/api/search-logs/**"
                        ).authenticated()

                        .requestMatchers(
                                "/api/admin/**",
                                "/api/owner/**"
                        ).authenticated()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, ErrorStatus.UNAUTHORIZED)
                        )
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, ErrorStatus.FORBIDDEN)
                        )
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
                )
                .build();
    }

    /**
     * 중요:
     * NimbusJwtEncoder에는 SecretKey가 아니라 byte[] 기반 ImmutableSecret을 쓰는 게 안전하다.
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        byte[] secretBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return new NimbusJwtEncoder(new ImmutableSecret<>(secretBytes));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = createJwtDecoder();

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer()),
                accessTokenValidator()
        ));

        return jwtDecoder;
    }

    @Bean
    public JwtDecoder refreshTokenJwtDecoder() {
        NimbusJwtDecoder jwtDecoder = createJwtDecoder();

        jwtDecoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(jwtProperties.getIssuer())
        );

        return jwtDecoder;
    }

    private NimbusJwtDecoder createJwtDecoder() {
        return NimbusJwtDecoder.withSecretKey(secretKey())
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    private OAuth2TokenValidator<Jwt> accessTokenValidator() {
        return jwt -> {
            String tokenType = jwt.getClaimAsString(TOKEN_TYPE_CLAIM);

            if (ACCESS_TOKEN_TYPE.equals(tokenType)) {
                return OAuth2TokenValidatorResult.success();
            }

            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                            "invalid_token",
                            "Access token is required for protected APIs.",
                            null
                    )
            );
        };
    }

    private SecretKey secretKey() {
        return new SecretKeySpec(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
    }

    private void writeError(HttpServletResponse response, ErrorStatus errorStatus) throws java.io.IOException {
        response.setStatus(errorStatus.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        String body = """
                {
                  "code": "%s",
                  "message": "%s",
                  "data": null
                }
                """.formatted(
                escapeJson(errorStatus.getCode()),
                escapeJson(errorStatus.getMessage())
        );

        response.getWriter().write(body);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}