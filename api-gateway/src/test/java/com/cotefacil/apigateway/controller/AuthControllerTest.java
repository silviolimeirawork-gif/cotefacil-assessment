package com.cotefacil.apigateway.controller;

import com.cotefacil.apigateway.dto.LoginRequest;
import com.cotefacil.apigateway.dto.LoginResponse;
import com.cotefacil.apigateway.service.JwtService;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthController authController;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(AuthController.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("pass");
        UserDetails userDetails = mock(UserDetails.class);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // When
        ResponseEntity<LoginResponse> response = authController.login(request);

        // Then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isEqualTo("jwt-token");

        // Verificar logs
        assertThat(listAppender.list).hasSize(2);
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .contains("Tentativa de login para usuário: user");
        assertThat(listAppender.list.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(listAppender.list.get(1).getFormattedMessage())
                .contains("Login bem-sucedido para usuário: user");
        assertThat(listAppender.list.get(1).getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    void login_withInvalidCredentials_shouldThrowBadCredentialsException() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When / Then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () ->
                authController.login(request)
        );

        // Verificar logs: primeiro INFO (tentativa), depois WARN (falha)
        assertThat(listAppender.list).hasSize(2);
        assertThat(listAppender.list.get(0).getFormattedMessage())
                .contains("Tentativa de login para usuário: user");
        assertThat(listAppender.list.get(0).getLevel()).isEqualTo(Level.INFO);
        assertThat(listAppender.list.get(1).getFormattedMessage())
                .contains("Falha de login para usuário: user - senha inválida");
        assertThat(listAppender.list.get(1).getLevel()).isEqualTo(Level.WARN);
    }
    
}
