package com.example.account.controller;

import com.example.account.dto.*;
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

public class AccountControllerTest {

    private MockMvc mockMvc;
    private CustomerService customerService;
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        customerService = Mockito.mock(CustomerService.class);
        jwtUtil = new JwtUtil();
        // set properties via reflection if needed; JwtUtil falls back to defaults so ok
        AccountController controller = new AccountController(customerService, jwtUtil);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void token_invalidBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/token").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void token_userNotFound_returnsUnauthorized() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("noone@example.com");
        login.setPassword("pass");

        when(customerService.findByEmail(anyString())).thenReturn(null);

        String body = "{\"username\": \"noone@example.com\", \"password\": \"pass\"}";

        mockMvc.perform(post("/token").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void register_missingFields_returnsBadRequest() throws Exception {
        String body = "{\"email\": \"test@example.com\"}";
        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

}
