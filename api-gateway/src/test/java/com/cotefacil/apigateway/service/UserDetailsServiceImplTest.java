package com.cotefacil.apigateway.service;

import com.cotefacil.apigateway.model.User; // Ajuste o import conforme seu pacote
import com.cotefacil.apigateway.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private User mockUser; // Agora mockamos a entidade concreta que implementa UserDetails

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        String username = "joao";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));

        UserDetails result = userDetailsService.loadUserByUsername(username);

        assertThat(result).isEqualTo(mockUser);
    }

    @Test
    void loadUserByUsername_whenUserDoesNotExist_shouldThrowUsernameNotFoundException() {
        String username = "joao";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(username));

        assertThat(exception.getMessage()).isEqualTo("User not found: " + username);
    }
}
