package com.cotefacil.apiorders.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String username = SecurityContextHolder.getContext().getAuthentication() != null ?
                    SecurityContextHolder.getContext().getAuthentication().getName() : "anonymous";

            String requestBody = new String(requestWrapper.getContentAsByteArray());
            String responseBody = new String(responseWrapper.getContentAsByteArray());

            log.info("AUDIT: {} - {} {} - User: {} - Status: {} - Duration: {}ms - Request: {} - Response: {}",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    request.getMethod(),
                    request.getRequestURI(),
                    username,
                    response.getStatus(),
                    duration,
                    requestBody.length() > 100 ? requestBody.substring(0,100) + "..." : requestBody,
                    responseBody.length() > 100 ? responseBody.substring(0,100) + "..." : responseBody
            );

            responseWrapper.copyBodyToResponse(); // importante para não corromper a resposta
        }
    }
}