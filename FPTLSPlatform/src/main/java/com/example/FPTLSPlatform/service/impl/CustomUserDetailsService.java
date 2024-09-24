package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TeacherRepository teacherRepository;

    public CustomUserDetailsService(UserRepository userRepository, TeacherRepository teacherRepository) {
        this.userRepository = userRepository;
        this.teacherRepository = teacherRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Tìm kiếm User trước
        User user = userRepository.findByUserName(username).orElse(null);

        if (user != null) {
            // Nếu tìm thấy User, trả về UserDetails
            Set<SimpleGrantedAuthority> authorities = new HashSet<>();
            authorities.add(new SimpleGrantedAuthority(user.getRole().name()));

            return new org.springframework.security.core.userdetails.User(
                    user.getUserName(),
                    user.getPassword(),
                    authorities
            );
        }

        // Nếu không tìm thấy User, tìm kiếm Teacher
        Teacher teacher = teacherRepository.findByTeacherName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User or Teacher not found"));

        // Nếu tìm thấy Teacher, trả về UserDetails cho Teacher
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(teacher.getRole().name()));

        return new org.springframework.security.core.userdetails.User(
                teacher.getTeacherName(),
                teacher.getPassword(),
                authorities
        );
    }
}
