package com.railway.trainservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Swagger / Actuator (public) ────────────────────────────
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/**"
                ).permitAll()

                // ── Passenger read-only APIs (any authenticated user) ──────
                .requestMatchers(HttpMethod.GET,
                        "/api/trains/{trainId}",
                        "/api/schedules/search",
                        "/api/schedules/{scheduleId}",
                        "/api/schedules/{scheduleId}/availability",
                        "/api/fares"
                ).authenticated()

                // ── Admin APIs ─────────────────────────────────────────────
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                // ── Internal APIs (service-to-service, no public user auth) ─
                // Protected by an internal secret header checked in the controller
                .requestMatchers("/internal/**").permitAll()

                // Deny everything else
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
