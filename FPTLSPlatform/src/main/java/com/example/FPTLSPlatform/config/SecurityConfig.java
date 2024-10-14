package com.example.FPTLSPlatform.config;


import com.example.FPTLSPlatform.filter.JwtRequestFilter;
import com.twilio.http.HttpMethod;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login", "/applications/**", "/auth/register-student", "/auth/register-teacher", "/forgotpassword/**", "api/**").permitAll() // Publicly accessible endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers("/staff/**", "/courses/**").hasAuthority("STAFF")
                        .requestMatchers("/teacher/**").hasAuthority("TEACHER")
                        .requestMatchers("/student/**").hasAuthority("STUDENT")
                        .requestMatchers("/courses/**", "/applications/staff").hasAuthority("STAFF")
                        .requestMatchers("/categories/**").hasAuthority("STAFF")
                        .requestMatchers("/classes/byCourse/{courseCode}").hasAuthority("STUDENT")
                        .requestMatchers("/classes/{classId}").hasAuthority("STUDENT")
                        .requestMatchers("/classes").hasAuthority("STUDENT")
                        .requestMatchers("/classes/teacher/{teacherName}").hasAuthority("STUDENT")

                        .requestMatchers("/classes/**").hasAuthority("TEACHER")
                        .requestMatchers("classes/confirm-classes/").hasAuthority("TEACHER")
                        .requestMatchers("orders/**").hasAuthority("STUDENT")
                        .requestMatchers("feedback/order/{orderId}/submit").hasAuthority("STUDENT")
                        .requestMatchers("feedback/class/{classId}/summary").hasAuthority("STAFF")
                        .requestMatchers("/api/feedback-question").hasAuthority("STAFF")
                        .requestMatchers("/api/feedback-category").hasAuthority("STAFF")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

