package com.movies2.gateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import java.net.URI;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;


@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    private static final String FRONTEND_BASE_URL = "http://localhost:3050/theGame";
    private static final String FRONTEND_ORIGIN = "http://localhost:3050";

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.addAllowedOrigin(FRONTEND_ORIGIN);
        cors.addAllowedHeader("*");
        cors.addAllowedMethod("*");
        cors.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cors);
        return source;
    }

    @Bean
    public SecurityWebFilterChain springSecurity(ServerHttpSecurity http) {
        ServerAuthenticationSuccessHandler successHandler =
                new RedirectServerAuthenticationSuccessHandler(FRONTEND_BASE_URL);
        HttpStatusServerEntryPoint entryPoint =
                new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED);
        RedirectServerLogoutSuccessHandler logoutSuccessHandler =
                new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create(FRONTEND_BASE_URL));

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(Customizer.withDefaults())
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .requestCache(cache -> cache.requestCache(NoOpServerRequestCache.getInstance()))
                .exceptionHandling(handling -> handling.authenticationEntryPoint(entryPoint))
                .authorizeExchange(ex -> ex.pathMatchers("/actuator/**", "/logout").permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(successHandler))
                .oauth2Client(Customizer.withDefaults())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .logout(logout -> logout
                        .requiresLogout(ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/logout"))
                        .logoutSuccessHandler(logoutSuccessHandler))
                .build();
    }
}
