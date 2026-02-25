package com.cotefacil.apigateway;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GeraSenha {

	public static void main(String[] args) {
	
		System.out.println(new BCryptPasswordEncoder().encode("senha123"));
		
	}
	
}
