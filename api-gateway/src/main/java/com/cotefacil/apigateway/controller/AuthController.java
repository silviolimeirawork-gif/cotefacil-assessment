package com.cotefacil.apigateway.controller;

import com.cotefacil.apigateway.dto.LoginRequest;
import com.cotefacil.apigateway.dto.LoginResponse;
import com.cotefacil.apigateway.service.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Tentativa de login para usuário: {}", request.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            UserDetails user = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(user);
            log.info("Login bem-sucedido para usuário: {}", request.getUsername());
            return ResponseEntity.ok(new LoginResponse(token));
        } catch (BadCredentialsException e) {
            log.warn("Falha de login para usuário: {} - senha inválida", request.getUsername());
            throw e; // será tratado pelo GlobalExceptionHandler
        }
    }
}
