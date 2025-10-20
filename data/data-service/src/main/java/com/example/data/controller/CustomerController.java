package com.example.data.controller;


import com.example.data.model.Customer;
import com.example.data.repository.CustomerRepository;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerRepository repo;  // <-- here

    public CustomerController(CustomerRepository repo) {  // <-- here
        this.repo = repo;
    }

    @GetMapping("/")
    public String health() { return "service is up"; }

    @GetMapping
    public List<Customer> getAll() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getOne(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Customer c) {
        Optional<Customer> existing = repo.findByEmail(c.getEmail());
        if (existing.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"error\":\"EMAIL_TAKEN\",\"message\":\"Email already exists\"}");
        }
        Customer saved = repo.save(c);
        return ResponseEntity.created(URI.create("/api/customers/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> update(@PathVariable Long id, @RequestBody Customer in) {
        return repo.findById(id).map(c -> {
            c.setName(in.getName());
            c.setEmail(in.getEmail());
            c.setPasswordHash(in.getPasswordHash());
            repo.save(c);
            return ResponseEntity.ok().body((Object) c);
        }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("{\"error\":\"NOT_FOUND\",\"message\":\"Customer not found\"}"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("{\"error\":\"NOT_FOUND\",\"message\":\"Customer not found\"}");
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}