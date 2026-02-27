package com.cotefacil.apigateway.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    private final String secret = "mySecretKeyWithAtLeast32CharactersForHS256Algorithm";
    private final Long expiration = 3600000L; // 1 hora
    private final String USERNAME = "testuser";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expiration", expiration);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);

        String token = jwtService.generateToken(userDetails);

        assertThat(token).isNotNull();
        assertThat(jwtService.extractUsername(token)).isEqualTo(USERNAME);
        assertThat(jwtService.extractExpiration(token)).isAfter(new Date());
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        String extracted = jwtService.extractUsername(token);

        assertThat(extracted).isEqualTo(USERNAME);
    }

    @Test
    void extractExpiration_shouldReturnFutureDate() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        Date expirationDate = jwtService.extractExpiration(token);

        assertThat(expirationDate).isAfter(new Date());
    }

    @Test
    void isTokenValid_withValidToken_shouldReturnTrue() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isTrue();
    }

    @Test
    void isTokenValid_withWrongUsername_shouldReturnFalse() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String token = jwtService.generateToken(userDetails);

        UserDetails wrongUser = mock(UserDetails.class);
        when(wrongUser.getUsername()).thenReturn("wronguser");

        boolean isValid = jwtService.isTokenValid(token, wrongUser);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);

        ReflectionTestUtils.setField(jwtService, "expiration", -1000L);
        String token = jwtService.generateToken(userDetails);

        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertThat(isValid).isFalse();
    }

    @Test
    void isTokenValid_withTamperedToken_shouldReturnFalse() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(USERNAME);
        String validToken = jwtService.generateToken(userDetails);

        String tamperedToken = validToken.substring(0, validToken.length() - 5) + "xxxxx";

        boolean isValid = jwtService.isTokenValid(tamperedToken, userDetails);

        assertThat(isValid).isFalse();
    }
}
