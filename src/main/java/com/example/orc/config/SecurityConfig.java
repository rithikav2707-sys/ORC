package com.example.orc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/login", "/dev/h2-console/**").permitAll()
                    .requestMatchers("/turbines/**", "/telemetry/**", "/products/**", "/accounts/**", "/budgets/**", "/settings",
                        "/api/orc/turbines/**", "/api/orc/telemetry/**", "/api/products/**", "/api/accounts/**")
                    .hasRole("ADMIN")
                        .anyRequest().authenticated())
                    .formLogin(login -> login.loginPage("/login").permitAll())
                    .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                    .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**", "/dev/h2-console/**"))
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new InMemoryUserDetailsManager(
                User.withUsername("admin").password(encoder.encode("orcAdmin123")).roles("ADMIN").build(),
                User.withUsername("accountant").password(encoder.encode("accountant123")).roles("ACCOUNTANT").build());
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
