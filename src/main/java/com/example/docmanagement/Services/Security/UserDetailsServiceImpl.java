package com.example.docmanagement.Services.Security;

import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        System.out.println("=== ATTEMPTING TO LOAD USER: " + email + " ===");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("=== USER NOT FOUND: " + email + " ===");
                    return new UsernameNotFoundException("User not found with email: " + email);
                });

        System.out.println("=== USER FOUND: " + user.getEmail() + " ===");
        System.out.println("=== USER PASSWORD LENGTH: " + (user.getPassword() != null ? user.getPassword().length() : "NULL") + " ===");
        System.out.println("=== USER ROLE: " + user.getRole().getRoleName() + " ===");

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName().toUpperCase());

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(authority)
        );
    }
}