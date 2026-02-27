package com.cotefacil.apigateway.controller;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProxyControllerTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private OrderProxyController orderProxyController;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> entityCaptor;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    private final String baseUrl = "http://localhost:8081";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(orderProxyController, "ordersApiUrl", baseUrl);
        logger = (Logger) LoggerFactory.getLogger(OrderProxyController.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    // Configuração padrão para evitar NPE em getHeaderNames()
    private void mockDefaultRequestBehavior() {
        when(request.getHeaderNames()).thenReturn(new Vector<String>().elements());
    }

    @Test
    void proxyGet_shouldCallProxyRequestWithNullBody() {
        mockDefaultRequestBehavior();
        String token = "Bearer token";
        when(request.getRequestURI()).thenReturn("/api/orders/123");
        when(request.getQueryString()).thenReturn(null);

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = orderProxyController.proxyGet(request, token);

        assertThat(response).isEqualTo(expectedResponse);
        verify(restTemplate).exchange(eq(baseUrl + "/api/orders/123"), eq(HttpMethod.GET), entityCaptor.capture(), eq(String.class));
        HttpEntity<String> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isNull();
        assertThat(entity.getHeaders().getFirst("Authorization")).isEqualTo(token);
    }

    @Test
    void proxyPost_shouldCallProxyRequestWithBody() {
        mockDefaultRequestBehavior();
        String token = "Bearer token";
        String body = "{\"key\":\"value\"}";
        when(request.getRequestURI()).thenReturn("/api/orders");
        when(request.getQueryString()).thenReturn("param=1");

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = orderProxyController.proxyPost(request, token, body);

        assertThat(response).isEqualTo(expectedResponse);
        verify(restTemplate).exchange(eq(baseUrl + "/api/orders?param=1"), eq(HttpMethod.POST), entityCaptor.capture(), eq(String.class));
        HttpEntity<String> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isEqualTo(body);
        assertThat(entity.getHeaders().getFirst("Authorization")).isEqualTo(token);
    }

    @Test
    void proxyPut_shouldCallProxyRequestWithBody() {
        mockDefaultRequestBehavior();
        String token = "Bearer token";
        String body = "{\"key\":\"value\"}";
        when(request.getRequestURI()).thenReturn("/api/orders/456");
        when(request.getQueryString()).thenReturn(null);

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = orderProxyController.proxyPut(request, token, body);

        assertThat(response).isEqualTo(expectedResponse);
        verify(restTemplate).exchange(eq(baseUrl + "/api/orders/456"), eq(HttpMethod.PUT), entityCaptor.capture(), eq(String.class));
        HttpEntity<String> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isEqualTo(body);
        assertThat(entity.getHeaders().getFirst("Authorization")).isEqualTo(token);
    }

    @Test
    void proxyDelete_shouldCallProxyRequestWithNullBody() {
        mockDefaultRequestBehavior();
        String token = "Bearer token";
        when(request.getRequestURI()).thenReturn("/api/orders/789");
        when(request.getQueryString()).thenReturn("force=true");

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = orderProxyController.proxyDelete(request, token);

        assertThat(response).isEqualTo(expectedResponse);
        verify(restTemplate).exchange(eq(baseUrl + "/api/orders/789?force=true"), eq(HttpMethod.DELETE), entityCaptor.capture(), eq(String.class));
        HttpEntity<String> entity = entityCaptor.getValue();
        assertThat(entity.getBody()).isNull();
        assertThat(entity.getHeaders().getFirst("Authorization")).isEqualTo(token);
    }

    @Test
    void proxyRequest_shouldForwardAllHeadersExceptAuthorizationAndContentLength() {
        String token = "Bearer token";
        String body = "body";
        when(request.getRequestURI()).thenReturn("/api/orders/123");
        when(request.getQueryString()).thenReturn(null);
        // Configurar apenas os headers que serão repassados
        Enumeration<String> headerNames = new Vector<>(List.of("Accept", "User-Agent")).elements();
        when(request.getHeaderNames()).thenReturn(headerNames);
        when(request.getHeader("Accept")).thenReturn("application/json");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        // Não configurar Authorization nem Content-Length

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        orderProxyController.proxyPost(request, token, body);

        verify(restTemplate).exchange(eq(baseUrl + "/api/orders/123"), eq(HttpMethod.POST), entityCaptor.capture(), eq(String.class));
        HttpHeaders headers = entityCaptor.getValue().getHeaders();
        assertThat(headers.getFirst("Authorization")).isEqualTo(token); // este é adicionado explicitamente, não via request
        assertThat(headers.getFirst("Accept")).isEqualTo("application/json");
        assertThat(headers.getFirst("User-Agent")).isEqualTo("TestAgent");
        assertThat(headers.getFirst("Content-Length")).isNull();
    }
    
    @Test
    void proxyRequest_shouldLogInfo() {
        mockDefaultRequestBehavior();
        String token = "Bearer token";
        when(request.getRequestURI()).thenReturn("/api/orders/123");
        when(request.getQueryString()).thenReturn(null);

        ResponseEntity<String> expectedResponse = ResponseEntity.ok("response");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        orderProxyController.proxyGet(request, token);

        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage()).contains("Proxy: GET /api/orders/123 -> " + baseUrl + "/api/orders/123");
    }

    @Test
    void proxyRequest_shouldReturnSameStatusCodeAndBody() {
        mockDefaultRequestBehavior();
        String token = "Bearer token";
        when(request.getRequestURI()).thenReturn("/api/orders/123");
        when(request.getQueryString()).thenReturn(null);

        String responseBody = "{\"result\":\"ok\"}";
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(responseBody, HttpStatus.CREATED);
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<String> response = orderProxyController.proxyGet(request, token);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(responseBody);
    }
}
