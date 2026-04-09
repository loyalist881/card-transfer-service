package com.example.cardtransferrest.advice;

import com.example.cardtransferrest.service.InvalidCredentials;
import com.example.cardtransferrest.service.UnauthorizedBankAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@Slf4j
@CrossOrigin(origins = "*")
@RestControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(InvalidCredentials.class)
    public ResponseEntity<Map<String, String>> handleInvalidCredentials(InvalidCredentials e) {
        log.error("Ошибка валидации: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(UnauthorizedBankAccount.class)
    public ResponseEntity<String> handleUnauthorized(UnauthorizedBankAccount e) {
        log.error("Ошибка карты: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAll(Exception e) {
        log.error("Неизвестная ошибка сервера: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
}
