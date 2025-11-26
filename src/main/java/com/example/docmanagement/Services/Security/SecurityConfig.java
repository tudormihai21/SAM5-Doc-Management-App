package com.example.docmanagement.Services.Security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends VaadinWebSecurity {

    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //Configure Authentication Provider
        http.authenticationProvider(authenticationProvider());

        // ALLOW REST API BEFORE VAADIN LOCKS THE DOOR
        // We define specific exceptions here. We do NOT add .anyRequest()
        // because super.configure(http) will do that automatically later.
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
        );

        // Disable CSRF for REST API endpoints
        http.csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
        );

        // Configure Custom Logout
        http.logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        // CALL SUPER LAST
        // This applies Vaadin's security configuration and adds the "catch-all"
        // rule (anyRequest().authenticated()) for the UI views.
        super.configure(http);

        setLoginView(http, "/login");
    }
}