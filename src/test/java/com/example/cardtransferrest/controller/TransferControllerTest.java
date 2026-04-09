package com.example.cardtransferrest.controller;

import com.example.cardtransferrest.domain.BankAccount;
import com.example.cardtransferrest.dto.Amount;
import com.example.cardtransferrest.dto.ConfirmOperationRequest;
import com.example.cardtransferrest.dto.OperationIdResponse;
import com.example.cardtransferrest.dto.TransferRequest;
import com.example.cardtransferrest.service.TransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class TransferControllerTest {
    private TransferController transferController;
    private TransferService transferService;
    private TransferRequest validRequest;
    private BankAccount cardFrom;
    private BankAccount cardTo;
    private Amount amount;

    @BeforeEach
    void setUp() {
        transferService = Mockito.mock(TransferService.class);
        transferController = new TransferController(transferService);

        cardFrom = new BankAccount("1111222233334444", "12/26", "123", 10000000L);
        cardTo = new BankAccount("5555666677778888", "10/26", "456", 50000L);

        validRequest = new TransferRequest();
        amount = new Amount();
        validRequest.setCardFromNumber(cardFrom.getCardNumber());
        validRequest.setCardFromValidTill(cardFrom.getValidTill());
        validRequest.setCardFromCVV(cardFrom.getCvv());
        validRequest.setCardToNumber(cardTo.getCardNumber());
        amount.setValue(1000000);
        amount.setCurrency("RUB");
        validRequest.setAmount(amount);
    }

    @Test
    @DisplayName("Успешный перевод")
    void controller_ok() {
        String expectedId = "test-uuid-12345";
        when(transferService.processTransfer(validRequest)).
                thenReturn(expectedId);
        ResponseEntity<OperationIdResponse> response = transferController.processTransfer(validRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedId, response.getBody().getOperationId());
        verify(transferService, times(1)).processTransfer(validRequest);
    }

    @Test
    @DisplayName("Успешное подтверждение оплаты")
    void controller_ok_Copi() {
        String expectedId = "test-uuid-12345";
        ConfirmOperationRequest confirmRequest = new ConfirmOperationRequest();
        confirmRequest.setOperationId(expectedId);
        confirmRequest.setCode("0000");
        when(transferService.confirmOperation(confirmRequest)).
                thenReturn(expectedId);
        ResponseEntity<OperationIdResponse> response = transferController.confirmOperation(confirmRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedId, response.getBody().getOperationId());
        verify(transferService, times(1)).confirmOperation(confirmRequest);
    }
}
