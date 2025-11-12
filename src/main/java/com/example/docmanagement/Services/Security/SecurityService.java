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

    /**
     * Preia detaliile utilizatorului logat din contextul de securitate Spring.
     */
    private static Optional<UserDetails> getSpringUserDetails() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        // Verificăm dacă cineva este logat
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        return Optional.of((UserDetails) authentication.getPrincipal());
    }

    /**
     * Preia entitatea NOASTRĂ de 'User' (din baza de date)
     * pentru utilizatorul care este logat în prezent.
     */
    public static Optional<User> getAuthenticatedUser() {
        Optional<UserDetails> userDetails = getSpringUserDetails();
        if (userDetails.isEmpty()) {
            return Optional.empty();
        }

        // Folosim email-ul (care e username-ul în Spring) pentru a căuta în BD
        return userRepository.findByEmail(userDetails.get().getUsername());
    }

    /**
     * O metodă ajutătoare pentru a verifica rapid rolul 'PROJECT_MANAGER'.
     */
    public static boolean isCurrentUserProjectManager() {
        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return false;
        }

        // Verificăm numele rolului
        return "PROJECT_MANAGER".equals(userOpt.get().getRole().getRoleName());
    }

    /**
     * O metodă ajutătoare pentru a verifica rolul 'ADMIN'.
     */
    public boolean isCurrentUserAdmin() {
        Optional<User> userOpt = getAuthenticatedUser();
        if (userOpt.isEmpty()) {
            return false;
        }
        return "ADMIN".equals(userOpt.get().getRole().getRoleName());
    }
}