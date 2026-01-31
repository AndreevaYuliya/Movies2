package com.movies2.profile;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @GetMapping
    public Map<String, Object> me(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return new LinkedHashMap<>();
        }

        Map<String, Object> claims = new LinkedHashMap<>();
        jwt.getClaims().forEach(claims::put);
        return claims;
    }
}
