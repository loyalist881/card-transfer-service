package com.example.cardtransferrest.containers;

import com.example.cardtransferrest.dto.Amount;
import com.example.cardtransferrest.dto.TransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
public class TestContainers {
    @Container
    private static final GenericContainer<?> myApp = new GenericContainer<>("card-transfer-rest:latest")
            .withExposedPorts(5500);

    private final RestTemplate restTemplate = new RestTemplate();

    @Test
    void transferRequest_ShouldReturnOperationId() {
        TransferRequest request = new TransferRequest();
        request.setCardFromNumber("1111222233334444");
        request.setCardFromValidTill("12/26");
        request.setCardFromCVV("123");
        request.setCardToNumber("5555666677778888");
        Amount amount = new Amount();
        amount.setValue(100000);
        amount.setCurrency("RUB");
        request.setAmount(amount);

        Integer appPort = myApp.getMappedPort(5500);
        String url = "http://localhost:" + appPort + "/transfer";

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        System.out.println("Тест пройден. Ответ от контейнера: " + response.getBody());
    }
}
