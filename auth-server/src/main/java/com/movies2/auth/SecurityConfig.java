package com.movies2.auth;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.core.annotation.Order;

import java.time.Duration;
import java.util.UUID;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    SecurityFilterChain authSecurity(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                new OAuth2AuthorizationServerConfigurer();
        authorizationServerConfigurer.oidc(Customizer.withDefaults());

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http.securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/authorization/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new LoginUrlAuthenticationEntryPoint("/login/oauth2/authorization/google")))
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint.baseUri("/login/oauth2/authorization")))
                .oauth2ResourceServer(o -> o.jwt(Customizer.withDefaults()))
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .apply(authorizationServerConfigurer);
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/oauth2/authorization/**", "/login/**", "/error")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint.baseUri("/login/oauth2/authorization")))
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    RegisteredClientRepository registeredClients(
            @Value("${GATEWAY_REDIRECT_URI:http://localhost:8080/login/oauth2/code/auth}") String redirectUri) {
        RegisteredClient gateway = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("gateway")
                .clientSecret("{noop}gateway-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(redirectUri)
                .scope(OidcScopes.OPENID).scope(OidcScopes.PROFILE).scope(OidcScopes.EMAIL)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .tokenSettings(TokenSettings.builder().accessTokenTimeToLive(Duration.ofHours(1)).build())
                .build();
        return new InMemoryRegisteredClientRepository(gateway);
    }

    @Bean
    JWKSource<SecurityContext> jwkSource(
            @Value("${AUTH_RSA_PUBLIC:}") String publicPem,
            @Value("${AUTH_RSA_PRIVATE:}") String privatePem) {
        RSAKey rsa;
        try {
            rsa = (!publicPem.isBlank() && !privatePem.isBlank())
                    ? Jwks.fromPem(publicPem, privatePem)
                    : Jwks.generateRsa();
        } catch (Exception ex) {
            // fallback to generated key if provided PEM is invalid
            rsa = Jwks.generateRsa();
        }
        return new ImmutableJWKSet<>(new JWKSet(rsa));
    }

    @Bean
    AuthorizationServerSettings authSettings(
            @Value("${AUTH_SERVER_ISSUER:http://auth-server:9000}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> accessTokenCustomizer() {
        return context -> {
            if (!"access_token".equals(context.getTokenType().getValue())) {
                return;
            }

            if (context.getPrincipal() instanceof OAuth2AuthenticationToken authToken) {
                Object principal = authToken.getPrincipal();
                if (principal instanceof OidcUser oidcUser) {
                    String name = oidcUser.getFullName();
                    String email = oidcUser.getEmail();
                    String picture = oidcUser.getPicture();

                    if (name != null) {
                        context.getClaims().claim("name", name);
                    }
                    if (email != null) {
                        context.getClaims().claim("email", email);
                    }
                    if (picture != null) {
                        context.getClaims().claim("picture", picture);
                    }
                }
            }
        };
    }

    @Bean
    ClientRegistrationRepository socialClients(
            @Value("${google.client-id}") String id,
            @Value("${google.client-secret}") String secret) {
        ClientRegistration google = CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(id).clientSecret(secret)
                .scope("openid", "profile", "email")
                .redirectUri("{baseUrl}/login/oauth2/code/google")
                .build();
        return new InMemoryClientRegistrationRepository(google);
    }
}
