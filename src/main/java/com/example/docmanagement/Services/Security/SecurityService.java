package com.example.docmanagement.Services.Security;

import com.example.docmanagement.Domain.User.User;
import com.example.docmanagement.Repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class SecurityService {

    private static UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    private static Optional<UserDetails> getSpringUserDetails() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        return Optional.of((UserDetails) authentication.getPrincipal());
    }


    public static Optional<User> getAuthenticatedUser() {
        Optional<UserDetails> userDetails = getSpringUserDetails();
        if (userDetails.isEmpty()) {
            return Optional.empty();
        }

        return userRepository.findByEmail(userDetails.get().getUsername());
    }


    public static boolean isCurrentUserProjectManager() {
        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return false;
        }

        return "PROJECT_MANAGER".equals(userOpt.get().getRole().getRoleName());
    }


    public boolean isCurrentUserAdmin() {
        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return false;
        }
        return "ADMIN".equals(userOpt.get().getRole().getRoleName());
    }
}