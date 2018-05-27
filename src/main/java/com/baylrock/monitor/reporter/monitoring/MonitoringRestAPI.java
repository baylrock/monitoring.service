package com.baylrock.monitor.reporter.monitoring;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MonitoringRestAPI {

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Autowired
    public MonitoringRestAPI(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        restTemplate = new RestTemplate();
    }

    public StatusResponse getServiceStatus(URI uri) {
        ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
        try {
            return objectMapper.readValue(response.getBody(), StatusResponse.class);
        } catch (IOException e) {
            // Accept any parse exceptions as response about "offline status" (from API doc: This call returns a status of READY if the API is available.)
            return new StatusResponse();
        }
    }

    public boolean legalServiceUri(URI uri) {
        try {
            restTemplate.getForEntity(uri, String.class);
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }

}
