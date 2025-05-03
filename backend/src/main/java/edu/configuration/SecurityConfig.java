package edu.configuration;

import edu.security.CustomAuthenticationEntryPoint;
import edu.security.CustomAuthenticationManager;
import edu.security.JwtAuthenticationFilter;
import edu.security.JwtProvider;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@ConfigurationProperties(prefix = "security", ignoreUnknownFields = false)
@Setter
@EnableWebSecurity
public class SecurityConfig {
    private String[] openEndpoints;

    private String[] securedEndpoints;

    private String authKey;

    @Getter
    private Duration expiration;

    @Bean
    public JwtProvider jwtProvider() {
        return new JwtProvider(
                Keys.hmacShaKeyFor(authKey.getBytes()),
                expiration.toMillis()
        );
    }

    @Bean
    public JwtAuthenticationFilter jwtFilter(JwtProvider jwtProvider) {
        return new JwtAuthenticationFilter(jwtProvider);
    }

    @Bean
    public CustomAuthenticationManager customAuthenticationManager() {
        return new CustomAuthenticationManager();
    }

    @Bean
    public CustomAuthenticationEntryPoint customAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            CustomAuthenticationManager authenticationManager,
            CustomAuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(openEndpoints).permitAll()
                        .requestMatchers(securedEndpoints).authenticated()
                        .anyRequest().permitAll()
                )
                .authenticationManager(authenticationManager)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(authenticationEntryPoint)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
