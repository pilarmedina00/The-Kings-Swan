package com.example.account.controller;

import com.example.account.service.CustomerService;
import com.example.account.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecureEndpointTest {

    private MockMvc mockMvc;
    private CustomerService customerService;
    private JwtUtil jwtUtil;

    @BeforeEach
    public void setup() {
        customerService = Mockito.mock(CustomerService.class);
        jwtUtil = new JwtUtil();
    AccountController controller = new AccountController(customerService, jwtUtil);
    // include the jwt filter so Authorization header is processed
    com.example.account.security.JwtAuthenticationFilter filter = new com.example.account.security.JwtAuthenticationFilter(jwtUtil);
    mockMvc = MockMvcBuilders.standaloneSetup(controller).addFilters(filter).build();
    }

    @Test
    public void secureEndpoint_withValidToken_returnsOk() throws Exception {
        String token = jwtUtil.generateToken("user@example.com", null);

        mockMvc.perform(get("/secure").header("Authorization", "Bearer " + token).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("hello user@example.com")));
    }

    @Test
    public void secureEndpoint_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/secure").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
