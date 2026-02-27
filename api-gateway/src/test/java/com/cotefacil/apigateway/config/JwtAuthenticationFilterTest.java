package com.cotefacil.apigateway.config;

import com.cotefacil.apigateway.service.JwtService;
import com.cotefacil.apigateway.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String TOKEN = "valid.jwt.token";
    private static final String USERNAME = "testuser";
    private static final String BEARER_TOKEN = "Bearer " + TOKEN;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenAuthHeaderMissing_shouldContinueChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void whenAuthHeaderDoesNotStartWithBearer_shouldContinueChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void whenUsernameExtractedIsNull_shouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(TOKEN);
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void whenAuthenticationAlreadyExists_shouldNotAuthenticate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(USERNAME);

        // Simular autenticação existente
        UsernamePasswordAuthenticationToken existingAuth = mock(UsernamePasswordAuthenticationToken.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(TOKEN);
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existingAuth);
    }

    @Test
    void whenTokenIsInvalid_shouldNotAuthenticate() throws ServletException, IOException {
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(TOKEN, userDetails)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(TOKEN);
        verify(userDetailsService).loadUserByUsername(USERNAME);
        verify(jwtService).isTokenValid(TOKEN, userDetails);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void whenTokenIsValid_shouldAuthenticateAndSetSecurityContext() throws ServletException, IOException {
        UserDetails userDetails = mock(UserDetails.class);

        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenReturn(userDetails);
        when(jwtService.isTokenValid(TOKEN, userDetails)).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verificar se a autenticação foi configurada corretamente no contexto
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.getCredentials()).isNull();
        assertThat(authentication.getAuthorities()).isEqualTo(userDetails.getAuthorities());
        assertThat(authentication.getDetails()).isNotNull(); // O WebAuthenticationDetails foi criado

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void whenLoadUserThrowsException_shouldPropagate() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(BEARER_TOKEN);
        when(jwtService.extractUsername(TOKEN)).thenReturn(USERNAME);
        when(userDetailsService.loadUserByUsername(USERNAME)).thenThrow(new RuntimeException("User not found"));

        assertThrows(RuntimeException.class, () ->
                jwtAuthenticationFilter.doFilterInternal(request, response, filterChain)
        );

        verify(filterChain, never()).doFilter(any(), any());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
