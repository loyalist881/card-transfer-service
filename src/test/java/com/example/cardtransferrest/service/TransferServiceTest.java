package com.example.cardtransferrest.service;

import com.example.cardtransferrest.domain.BankAccount;
import com.example.cardtransferrest.dto.Amount;
import com.example.cardtransferrest.dto.ConfirmOperationRequest;
import com.example.cardtransferrest.dto.TransferRequest;
import com.example.cardtransferrest.repository.BankAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TransferServiceTest {
    private TransferService transferService;
    private BankAccountRepository bankAccountRepository;
    private TransferRequest validRequest;
    private Amount amount;
    private BankAccount cardFrom;
    private BankAccount cardTo;

    @BeforeEach
    void setUp() {

        bankAccountRepository = Mockito.mock(BankAccountRepository.class);
        transferService = new TransferService(bankAccountRepository);

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
    @DisplayName("Успешная инициализация перевода")
    void successful_scenario_processTransfer() {
        when(bankAccountRepository.findByCardNumber(cardFrom.getCardNumber())).
                thenReturn(Optional.of(cardFrom));
        when(bankAccountRepository.findByCardNumber(cardTo.getCardNumber())).
                thenReturn(Optional.of(cardTo));
        String operationId = transferService.processTransfer(validRequest);
        assertNotNull(operationId, "ID операции не должен быть null");
        assertEquals(36, operationId.length(), "ID операции должен быть в формате UUID");

        verify(bankAccountRepository, times(1)).findByCardNumber(cardFrom.getCardNumber());
        verify(bankAccountRepository, times(1)).findByCardNumber(cardTo.getCardNumber());
    }

    @Test
    @DisplayName("Ошибка отправителя")
    void sender_error() {
        String badCardNumber = "1234123412341234";
        validRequest.setCardFromNumber(badCardNumber);
        when(bankAccountRepository.findByCardNumber(badCardNumber)).
                thenReturn(Optional.empty());
        UnauthorizedBankAccount exception = assertThrows(UnauthorizedBankAccount.class, () -> {
            transferService.processTransfer(validRequest);
        });
        assertEquals("Карта отправителя не найдена", exception.getMessage());
        verify(bankAccountRepository, times(1)).findByCardNumber(badCardNumber);
    }

    @Test
    @DisplayName("Ошибка получателя")
    void recipient_error() {
        String badCardNumber = "1234123412341234";
        validRequest.setCardToNumber(badCardNumber);
        when(bankAccountRepository.findByCardNumber(cardFrom.getCardNumber())).
                thenReturn(Optional.of(cardFrom));
        when(bankAccountRepository.findByCardNumber(badCardNumber)).
                thenReturn(Optional.empty());
        UnauthorizedBankAccount exception = assertThrows(UnauthorizedBankAccount.class, () -> {
            transferService.processTransfer(validRequest);
        });
        assertEquals("Карта получателя не найдена", exception.getMessage());
        verify(bankAccountRepository, times(1)).findByCardNumber(badCardNumber);
    }

    @Test
    @DisplayName("Ошибка авторизации отправителя")
    void sender_authorization_error() {
        validRequest.setCardFromCVV("CVV");
        validRequest.setCardFromValidTill("07/26");
        when(bankAccountRepository.findByCardNumber(cardFrom.getCardNumber())).
                thenReturn(Optional.of(cardFrom));
        InvalidCredentials exception = assertThrows(InvalidCredentials.class, () -> {
            transferService.processTransfer(validRequest);
        });
        assertEquals("Неверные данные карты отправителя", exception.getMessage());
        verify(bankAccountRepository, times(1)).findByCardNumber(cardFrom.getCardNumber());
        verify(bankAccountRepository, never()).findByCardNumber(cardTo.getCardNumber());
    }

    @Test
    @DisplayName("Ошибка баланса")
    void balance_error() {
        amount.setValue(100000000);
        when(bankAccountRepository.findByCardNumber(cardFrom.getCardNumber())).
                thenReturn(Optional.of(cardFrom));
        when(bankAccountRepository.findByCardNumber(cardTo.getCardNumber())).
                thenReturn(Optional.of(cardTo));
        InvalidCredentials exception = assertThrows(InvalidCredentials.class, () -> {
            transferService.processTransfer(validRequest);
        });
        assertEquals("Недостаточно средств на счете", exception.getMessage());
        verify(bankAccountRepository, times(1)).findByCardNumber(cardFrom.getCardNumber());
        verify(bankAccountRepository, times(1)).findByCardNumber(cardTo.getCardNumber());
    }

    @Test
    @DisplayName("Успешное подтверждение перевода")
    void successful_scenario_confirmOperation() {
        when(bankAccountRepository.findByCardNumber(cardFrom.getCardNumber())).
                thenReturn(Optional.of(cardFrom));
        when(bankAccountRepository.findByCardNumber(cardTo.getCardNumber())).
                thenReturn(Optional.of(cardTo));
        String operationId = transferService.processTransfer(validRequest);
        ConfirmOperationRequest confirmRequest = new ConfirmOperationRequest();
        confirmRequest.setOperationId(operationId);
        confirmRequest.setCode("0000");

        long initialBalanceFrom = cardFrom.getBalance();
        long initialBalanceTo = cardTo.getBalance();
        long amountToTransfer = validRequest.getAmount().getValue();
        long commission = amountToTransfer / 100;

        String confirmedId = transferService.confirmOperation(confirmRequest);

        assertEquals(operationId, confirmedId, "ID операции должен совпадать");

        assertEquals(initialBalanceFrom - amountToTransfer - commission, cardFrom.getBalance(),
                "Баланс отправителя должен уменьшиться на сумму перевода и комиссию");
        assertEquals(initialBalanceTo + amountToTransfer, cardTo.getBalance(),
                "Баланс получателя должен увеличиться на сумму перевода");

        verify(bankAccountRepository, times(2)).findByCardNumber(cardFrom.getCardNumber());
        verify(bankAccountRepository, times(2)).findByCardNumber(cardTo.getCardNumber());
    }

    @Test
    @DisplayName("Ошибка поиска")
    void search_error() {
        ConfirmOperationRequest confirmRequest = new ConfirmOperationRequest();
        confirmRequest.setOperationId("fake-id-123");
        confirmRequest.setCode("0000");
        InvalidCredentials exception = assertThrows(InvalidCredentials.class, () -> {
            transferService.confirmOperation(confirmRequest);
        });
        assertEquals("Операция не найден или уже завершена", exception.getMessage());
    }

    @Test
    @DisplayName("Неверный код")
    void code_error() {
        when(bankAccountRepository.findByCardNumber(cardFrom.getCardNumber())).
                thenReturn(Optional.of(cardFrom));
        when(bankAccountRepository.findByCardNumber(cardTo.getCardNumber())).
                thenReturn(Optional.of(cardTo));
        String operationId = transferService.processTransfer(validRequest);
        ConfirmOperationRequest confirmRequest = new ConfirmOperationRequest();
        confirmRequest.setOperationId(operationId);
        confirmRequest.setCode("9999");
        InvalidCredentials exception = assertThrows(InvalidCredentials.class, () -> {
            transferService.confirmOperation(confirmRequest);
        });
        assertEquals("Неверный код подтверждения", exception.getMessage());
    }
}
