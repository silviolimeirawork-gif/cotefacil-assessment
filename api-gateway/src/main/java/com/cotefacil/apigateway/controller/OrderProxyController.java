package com.cotefacil.apigateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;

@RestController
@RequestMapping("/api/orders")
public class OrderProxyController {

    private static final Logger log = LoggerFactory.getLogger(OrderProxyController.class);

    private final RestTemplate restTemplate;

    @Value("${api.orders.url}")
    private String ordersApiUrl;

    public OrderProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/**")
    public ResponseEntity<String> proxyGet(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        return proxyRequest(request, token, HttpMethod.GET, null);
    }

    @PostMapping("/**")
    public ResponseEntity<String> proxyPost(HttpServletRequest request, @RequestHeader("Authorization") String token, @RequestBody(required = false) String body) {
        return proxyRequest(request, token, HttpMethod.POST, body);
    }

    @PutMapping("/**")
    public ResponseEntity<String> proxyPut(HttpServletRequest request, @RequestHeader("Authorization") String token, @RequestBody(required = false) String body) {
        return proxyRequest(request, token, HttpMethod.PUT, body);
    }

    @DeleteMapping("/**")
    public ResponseEntity<String> proxyDelete(HttpServletRequest request, @RequestHeader("Authorization") String token) {
        return proxyRequest(request, token, HttpMethod.DELETE, null);
    }

    private ResponseEntity<String> proxyRequest(HttpServletRequest request, String token, HttpMethod method, String body) {
        String path = request.getRequestURI().replace("/api/orders", "");
        String query = request.getQueryString();
        String url = ordersApiUrl + "/api/orders" + path + (query != null ? "?" + query : "");

        log.info("Proxy: {} {} -> {}", method, request.getRequestURI(), url);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            if (!header.equalsIgnoreCase("Authorization") && !header.equalsIgnoreCase("content-length")) {
                headers.set(header, request.getHeader(header));
            }
        }

        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
        return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
    }
}
