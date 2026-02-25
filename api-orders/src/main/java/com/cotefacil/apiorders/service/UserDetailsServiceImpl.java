package com.cotefacil.apiorders.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Como não temos usuários locais, apenas criamos um UserDetails básico para validação do token
        // O token já foi validado, então podemos retornar um usuário dummy
        return new User(username, "", new ArrayList<>());
    }
}
