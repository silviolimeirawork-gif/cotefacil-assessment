package com.cotefacil.apigateway.exception;

import com.cotefacil.apigateway.dto.ErrorResponse;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private HttpServletRequest request;

    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();

        // Capturar logs
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(listAppender);
    }

    @Test
    void handleBadCredentials_shouldReturnUnauthorized() {
        // Given
        String errorMessage = "Bad credentials";
        BadCredentialsException ex = new BadCredentialsException(errorMessage);
        when(request.getRequestURI()).thenReturn("/auth/login");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBadCredentials(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(401);
        assertThat(body.getError()).isEqualTo("Unauthorized");
        assertThat(body.getMessage()).isEqualTo("Invalid username or password");
        assertThat(body.getPath()).isEqualTo("/auth/login");
        assertThat(body.getTimestamp()).isNotNull();

        // Verificar log
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(logEvent.getFormattedMessage()).contains("Erro de autenticação: " + errorMessage);
    }

    @Test
    void handleValidation_shouldReturnBadRequest() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be blank");
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(request.getRequestURI()).thenReturn("/api/orders");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(400);
        assertThat(body.getError()).isEqualTo("Bad Request");
        assertThat(body.getMessage()).isEqualTo("field: must not be blank");
        assertThat(body.getPath()).isEqualTo("/api/orders");
    }

    @Test
    void handleValidation_whenNoFieldErrors_shouldUseDefaultMessage() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of()); // lista vazia
        when(request.getRequestURI()).thenReturn("/api/orders");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleValidation(ex, request);

        // Then
        assertThat(response.getBody().getMessage()).isEqualTo("Validation error");
    }

    @Test
    void handleGeneric_shouldReturnInternalServerError() {
        // Given
        String errorMessage = "Null pointer";
        Exception ex = new NullPointerException(errorMessage);
        when(request.getRequestURI()).thenReturn("/api/test");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGeneric(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(500);
        assertThat(body.getError()).isEqualTo("Internal Server Error");
        assertThat(body.getMessage()).isEqualTo(errorMessage);
        assertThat(body.getPath()).isEqualTo("/api/test");

        // Verificar log com stack trace
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR);
        assertThat(logEvent.getFormattedMessage()).contains("Erro interno no servidor: " + errorMessage);
    }

    @Test
    void handleMethodNotSupported_shouldReturnMethodNotAllowed() {
        // Given
        String errorMessage = "Request method 'POST' is not supported";
        HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("POST");
        when(request.getRequestURI()).thenReturn("/api/orders/123");

        // When
        ResponseEntity<ErrorResponse> response = exceptionHandler.handleMethodNotSupported(ex, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        ErrorResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getStatus()).isEqualTo(405);
        assertThat(body.getError()).isEqualTo("Method Not Allowed");
        assertThat(body.getMessage()).isEqualTo(errorMessage); // ou .contains("not supported")
        assertThat(body.getPath()).isEqualTo("/api/orders/123");
    }
    
}
