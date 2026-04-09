package com.example.cardtransferrest.controller;

import com.example.cardtransferrest.dto.ConfirmOperationRequest;
import com.example.cardtransferrest.dto.OperationIdResponse;
import com.example.cardtransferrest.dto.TransferRequest;
import com.example.cardtransferrest.service.TransferService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TransferController {
    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping("/transfer")
    public ResponseEntity<OperationIdResponse> processTransfer(@Valid @RequestBody TransferRequest request) {
        log.info("Мы получили запрос на перевод денег: {}", request);
        String operationId = service.processTransfer(request);
        return ResponseEntity.ok(new OperationIdResponse(operationId));
    }

    @PostMapping("/confirmOperation")
    public ResponseEntity<OperationIdResponse> confirmOperation(@Valid @RequestBody ConfirmOperationRequest request) {
        log.info("Запрос получил подтверждение на перевод: {}", request);
        String operationId = service.confirmOperation(request);
        return ResponseEntity.ok(new OperationIdResponse(operationId));
    }
}
