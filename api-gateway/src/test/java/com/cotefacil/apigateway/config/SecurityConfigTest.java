package com.cotefacil.apigateway.config;

import com.cotefacil.apigateway.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void shouldAllowAccessToPublicEndpointWithWrongMethod() throws Exception {
        mockMvc.perform(get("/auth/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldDenyAccessToProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAllowAccessWithValidToken() throws Exception {
        // Gera um token real
        String token = jwtService.generateToken(User.withUsername("usuario")
                .password("")
                .authorities("USER")
                .build());

        // Configura o mock do RestTemplate para retornar uma resposta 200
        ResponseEntity<String> responseEntity = new ResponseEntity<>("OK", HttpStatus.OK);
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(responseEntity);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
