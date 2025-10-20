package com.example.account.controller;

import com.example.account.dto.CustomerDto;
import com.example.account.dto.LoginRequest;
import com.example.account.service.CustomerService;
import com.example.account.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AccountControllerTokenFlowTest {

    private MockMvc mockMvc;
    private CustomerService customerService;
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        customerService = Mockito.mock(CustomerService.class);
        jwtUtil = new JwtUtil();
        AccountController controller = new AccountController(customerService, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void token_validCredentials_returnsToken() throws Exception {
        CustomerDto customer = new CustomerDto();
        customer.setEmail("user@example.com");
        customer.setName("User");
        // set passwordHash to bcrypt of 'secret' so BCryptPasswordEncoder.matches passes
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        customer.setPasswordHash(enc.encode("secret"));

        when(customerService.findByEmail(anyString())).thenReturn(customer);

        String body = "{\"username\": \"user@example.com\", \"password\": \"secret\"}";

        mockMvc.perform(post("/token").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    public void token_debugTrue_returnsDiagnosticMap() throws Exception {
        CustomerDto customer = new CustomerDto();
        customer.setEmail("debug@example.com");
        customer.setName("Debug");
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        customer.setPasswordHash(enc.encode("pw"));

        when(customerService.findByEmail(anyString())).thenReturn(customer);

        String body = "{\"username\": \"debug@example.com\", \"password\": \"pw\"}";

        mockMvc.perform(post("/token?debug=true").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.passwordMatch").value(true));
    }

}
