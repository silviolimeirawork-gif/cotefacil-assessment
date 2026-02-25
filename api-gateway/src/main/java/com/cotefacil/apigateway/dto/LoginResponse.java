package com.cotefacil.apigateway.dto;

public class LoginResponse {
    private String token;

    // Construtor padrão (obrigatório para desserialização JSON)
    public LoginResponse() {}
    
    public LoginResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
