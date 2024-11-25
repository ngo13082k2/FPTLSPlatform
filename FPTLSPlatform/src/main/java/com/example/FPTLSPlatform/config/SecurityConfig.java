package com.example.FPTLSPlatform.config;


import com.example.FPTLSPlatform.filter.JwtRequestFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                        .requestMatchers("/auth/login", "/applications/**", "/auth/register-student", "/auth/register-teacher", "/forgotpassword/**", "api/**", "/auth/confirm-otp","/feedback/comments","/auth/forgot-password","/auth/confirm-otpForgot","/auth/reset-password").permitAll() // Publicly accessible endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories", "/categories/{id}").permitAll()
                        .requestMatchers("/staff/**").hasAuthority("STAFF")
                        .requestMatchers("/teacher/**").hasAuthority("TEACHER")
                        .requestMatchers("/student/**").hasAuthority("STUDENT")
                        .requestMatchers("/applications/staff").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.GET, "/courses").hasAnyAuthority("TEACHER", "STAFF", "STUDENT")
                        .requestMatchers(HttpMethod.POST, "/courses").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/courses/{courseCode}").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/courses/{courseCode}").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.POST, "/categories").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/categories").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.POST, "/categories").hasAuthority("STAFF")
                        .requestMatchers("/classes/byCourse/{courseCode}").hasAnyAuthority("STUDENT", "TEACHER")
                        .requestMatchers("/classes/StatusCompleted").hasAnyAuthority("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/classes/getByClassId/{classId}").hasAnyAuthority("STUDENT", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/classes").hasAnyAuthority("STUDENT", "ADMIN")
                        .requestMatchers("/classes/teacher/{teacherName}").hasAuthority("STUDENT")
                        .requestMatchers("/classes/{classId}/students").hasAnyAuthority("STAFF", "STUDENT", "TEACHER")
                        .requestMatchers(HttpMethod.PUT, "/classes/{classId}").hasAuthority("TEACHER")
                        .requestMatchers("/classes/my-classes").hasAuthority("TEACHER")
                        .requestMatchers(HttpMethod.POST, "/classes").hasAuthority("TEACHER")
                        .requestMatchers("classes/confirm-classes/").hasAuthority("TEACHER")
                        .requestMatchers("orders/**").hasAnyAuthority("STAFF", "STUDENT", "ADMIN")
                        .requestMatchers("feedback/order/{orderId}/submit").hasAuthority("STUDENT")
                        .requestMatchers("feedback/class/{classId}/summary").hasAuthority("STAFF")
                        .requestMatchers("/api/feedback-question").hasAuthority("STAFF")
                        .requestMatchers("/api/feedback-category").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.POST, "/slots").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.PUT, "/slots/{slotId}").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.DELETE, "/slots/{slotId}").hasAuthority("STAFF")
                        .requestMatchers(HttpMethod.GET, "/slots").hasAnyAuthority("STAFF", "STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/slots/{slotId}").hasAnyAuthority("STAFF", "STUDENT", "TEACHER")
                        .requestMatchers("/admin/**").hasAuthority("ADMIN")


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

