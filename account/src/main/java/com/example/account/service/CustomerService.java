package com.example.account.service;

import com.example.account.dto.CustomerDto;
import com.example.account.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final Logger log = LoggerFactory.getLogger(CustomerService.class);

    @Value("${dataservice.baseUrl}")
    private String dataServiceBaseUrl;

    public CustomerDto findByEmail(String email) {
        String url = dataServiceBaseUrl + "/customers?email=" + email;
        ResponseEntity<CustomerDto> resp;
        try {
            resp = restTemplate.getForEntity(url, CustomerDto.class);
        } catch (Exception e) {
            log.error("Error calling Data Service findByEmail {} -> {}", url, e.toString(), e);
            return null;
        }
        if (resp.getStatusCode() == HttpStatus.OK) {
            return resp.getBody();
        }
        return null;
    }

    public CustomerDto createCustomer(RegisterRequest req) {
        String url = dataServiceBaseUrl + "/customers";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> entity = new HttpEntity<>(req, headers);
        try {
            ResponseEntity<CustomerDto> resp = restTemplate.postForEntity(url, entity, CustomerDto.class);
            if (resp.getStatusCode() == HttpStatus.CREATED || resp.getStatusCode() == HttpStatus.OK) {
                return resp.getBody();
            }
        } catch (Exception e) {
            log.error("Error creating customer via Data Service {} -> {}", url, e.toString(), e);
            return null;
        }
        return null;
    }
}
