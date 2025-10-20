package com.example.account.controller;

import com.example.account.dto.*;
import com.example.account.service.CustomerService;
import com.example.account.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@RestController
@RequestMapping("/")
public class AccountController {

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Logger log = LoggerFactory.getLogger(AccountController.class);

    public AccountController(CustomerService customerService, JwtUtil jwtUtil) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @GetMapping
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Account service is up and running");
    }

    @PostMapping("token")
    public ResponseEntity<?> token(@RequestBody LoginRequest login, HttpServletRequest servletRequest) {
        if (login == null || !StringUtils.hasText(login.getUsername()) || !StringUtils.hasText(login.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("username and password required");
        }
        log.info("Login attempt for user={}", login.getUsername());
        CustomerDto customer = customerService.findByEmail(login.getUsername());
        log.info("Customer lookup result for user={}: {}", login.getUsername(), customer != null ? "FOUND" : "NOT_FOUND");
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        // Verify password against bcrypt hash stored in data service
        boolean passMatch = customer.getPasswordHash() != null && passwordEncoder.matches(login.getPassword(), customer.getPasswordHash());
        log.info("Password match for user={}: {}", login.getUsername(), passMatch);

        // If debug=true query parameter is present, return diagnostic info (no passwords)
        String debug = servletRequest.getParameter("debug");
        if ("true".equalsIgnoreCase(debug)) {
            Map<String,Object> diag = new HashMap<>();
            diag.put("usernameReceived", login.getUsername());
            diag.put("customerFound", customer != null);
            diag.put("passwordMatch", passMatch);
            if (passMatch) {
                String token = jwtUtil.generateToken(customer.getEmail(), new HashMap<>());
                diag.put("token", token);
                return ResponseEntity.ok(diag);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(diag);
            }
        }

        if (!passMatch) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid credentials");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", customer.getEmail());
        claims.put("name", customer.getName());

        String token = jwtUtil.generateToken(customer.getEmail(), claims);

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (req == null || !StringUtils.hasText(req.getEmail()) || !StringUtils.hasText(req.getPassword()) || !StringUtils.hasText(req.getName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("name, email and password required");
        }

        // create via Data Service
        CustomerDto created = customerService.createCustomer(req);
        if (created == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("could not create customer");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("secure")
    public ResponseEntity<?> secure(java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("unauthorized");
        }
        return ResponseEntity.ok("hello " + principal.getName());
    }
}
