package com.safevoting.elecciones.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/votaciones/*/completar").hasRole("SERVICE_AUDITORIA")
                        .pathMatchers(HttpMethod.GET, "/api/v1/votaciones/*/tarjeton").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/participaciones/mis-votos").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/votaciones/*/votos").hasRole("VOTANTE")
                        .pathMatchers("/api/v1/candidaturas/**").hasRole("GESTOR_ELECTORAL")
                        .pathMatchers("/api/v1/votaciones/**").hasRole("GESTOR_ELECTORAL")
                        .pathMatchers(HttpMethod.POST, "/api/v1/partidos/**").hasAnyRole("ADMIN", "GESTOR_CANDIDATURAS")
                        .pathMatchers(HttpMethod.GET, "/api/v1/partidos/**").hasAnyRole("ADMIN", "GESTOR_CANDIDATURAS")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/partidos/**").hasAnyRole("ADMIN", "GESTOR_CANDIDATURAS")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/partidos/**").hasAnyRole("ADMIN", "GESTOR_CANDIDATURAS")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }
}
