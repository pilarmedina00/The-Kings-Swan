package com.example.data.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

// Update the import path to the actual package of Customer, for example:
import com.example.data.model.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

}