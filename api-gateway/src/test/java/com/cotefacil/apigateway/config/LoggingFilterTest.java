package com.cotefacil.apigateway.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;



@ExtendWith(MockitoExtension.class)
class LoggingFilterTest {

    private LoggingFilter loggingFilter;

    @Mock
    private HttpServletRequest request;

    private MockHttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
        response = new MockHttpServletResponse();

        logger = (Logger) LoggerFactory.getLogger(LoggingFilter.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
        SecurityContextHolder.clearContext();
    }

    @Test
    void whenAuthenticatedUserAndSmallBodies_shouldLogInfoWithDetails() throws ServletException, IOException {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("joao");
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        String requestBody = "{\"id\":1}";
        String responseBody = "{\"status\":\"ok\"}";

        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(requestBody.getBytes())));
        response.setStatus(200);

        doAnswer(invocation -> {
            ContentCachingRequestWrapper reqWrapper = invocation.getArgument(0);
            ContentCachingResponseWrapper resWrapper = invocation.getArgument(1);

            reqWrapper.getInputStream().readAllBytes();
            resWrapper.getOutputStream().write(responseBody.getBytes());
            resWrapper.flushBuffer();

            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));

        loggingFilter.doFilterInternal(request, response, filterChain);

        // Verificar se o corpo da resposta foi copiado corretamente
        assertThat(response.getContentAsString()).isEqualTo(responseBody);

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        String logMessage = logs.get(0).getFormattedMessage();
        assertThat(logMessage)
                .contains("AUDIT:", "POST", "/api/orders", "User: joao", "Status: 200")
                .contains("Request: " + requestBody)
                .contains("Response: " + responseBody);
    }

    @Test
    void whenAnonymousUserAndLargeBodies_shouldTruncateLogs() throws ServletException, IOException {
        SecurityContextHolder.setContext(mock(SecurityContext.class));

        String largeRequestBody = "a".repeat(150);
        String largeResponseBody = "b".repeat(150);

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/orders/123");
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream(largeRequestBody.getBytes())));
        response.setStatus(201);

        doAnswer(invocation -> {
            ContentCachingRequestWrapper reqWrapper = invocation.getArgument(0);
            ContentCachingResponseWrapper resWrapper = invocation.getArgument(1);

            reqWrapper.getInputStream().readAllBytes();
            resWrapper.getOutputStream().write(largeResponseBody.getBytes());
            resWrapper.flushBuffer();

            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));

        loggingFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentAsString()).isEqualTo(largeResponseBody);

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        String logMessage = logs.get(0).getFormattedMessage();

        assertThat(logMessage)
                .contains("User: anonymous")
                .contains("Request: " + largeRequestBody.substring(0, 100) + "...")
                .contains("Response: " + largeResponseBody.substring(0, 100) + "...");
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void whenFilterChainThrowsException_shouldStillLog() throws ServletException, IOException {
        SecurityContextHolder.setContext(mock(SecurityContext.class));

        when(request.getMethod()).thenReturn("DELETE");
        when(request.getRequestURI()).thenReturn("/api/orders/999");
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream("".getBytes())));
        response.setStatus(200);

        doThrow(new IOException("Falha na rede"))
                .when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));

        assertThrows(IOException.class, () ->
                loggingFilter.doFilterInternal(request, response, filterChain)
        );

        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs).hasSize(1);
        String logMessage = logs.get(0).getFormattedMessage();
        assertThat(logMessage)
                .contains("DELETE", "/api/orders/999", "User: anonymous", "Status: 200");
    }
    
    @Test
    void whenResponseStatusIsSet_shouldLogCorrectStatus() throws ServletException, IOException {
        SecurityContextHolder.setContext(mock(SecurityContext.class));

        when(request.getMethod()).thenReturn("PUT");
        when(request.getRequestURI()).thenReturn("/api/orders/456");
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream("{\"qty\":2}".getBytes())));
        response.setStatus(400);

        doAnswer(invocation -> {
            ContentCachingRequestWrapper reqWrapper = invocation.getArgument(0);
            ContentCachingResponseWrapper resWrapper = invocation.getArgument(1);

            reqWrapper.getInputStream().readAllBytes();
            resWrapper.getOutputStream().write("{\"error\":\"bad\"}".getBytes());
            resWrapper.flushBuffer();

            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));

        loggingFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentAsString()).isEqualTo("{\"error\":\"bad\"}");
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs.get(0).getFormattedMessage()).contains("Status: 400");
    }

    @Test
    void whenNoAuthentication_shouldUseAnonymous() throws ServletException, IOException {
        SecurityContextHolder.setContext(mock(SecurityContext.class));

        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getInputStream()).thenReturn(new DelegatingServletInputStream(new ByteArrayInputStream("".getBytes())));
        response.setStatus(200);

        doAnswer(invocation -> {
            ContentCachingRequestWrapper reqWrapper = invocation.getArgument(0);
            ContentCachingResponseWrapper resWrapper = invocation.getArgument(1);

            reqWrapper.getInputStream().readAllBytes();
            resWrapper.getOutputStream().write("[]".getBytes());
            resWrapper.flushBuffer();

            return null;
        }).when(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));

        loggingFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getContentAsString()).isEqualTo("[]");
        List<ILoggingEvent> logs = listAppender.list;
        assertThat(logs.get(0).getFormattedMessage()).contains("User: anonymous");
    }
}
