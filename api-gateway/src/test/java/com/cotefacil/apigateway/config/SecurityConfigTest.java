package com.cotefacil.apigateway.config;

import com.cotefacil.apigateway.service.JwtService;
import com.cotefacil.apigateway.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityConfigTest {

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtService jwtService; // necessário para JwtAuthenticationFilter

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private LoggingFilter loggingFilter;

    @Autowired
    private ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
    }

    @Test
    void securityFilterChainBeanIsCreated() {
        SecurityFilterChain filterChain = context.getBean(SecurityFilterChain.class);
        assertThat(filterChain).isNotNull();
    }

    @Test
    void passwordEncoderBeanIsCreated() {
        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    void authenticationManagerBeanIsCreated() throws Exception {
        AuthenticationManager authManager = context.getBean(AuthenticationManager.class);
        assertThat(authManager).isNotNull();
    }
}