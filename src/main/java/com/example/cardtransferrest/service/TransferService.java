package com.example.cardtransferrest.service;

import com.example.cardtransferrest.domain.BankAccount;
import com.example.cardtransferrest.dto.ConfirmOperationRequest;
import com.example.cardtransferrest.dto.TransferRequest;
import com.example.cardtransferrest.repository.BankAccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class TransferService {
    private final BankAccountRepository repository;
    private final Map<String, TransferRequest> pendingOperations = new ConcurrentHashMap<>();

    public TransferService(BankAccountRepository repository) {
        this.repository = repository;
    }

    public String processTransfer(TransferRequest request) {
        Optional<BankAccount> optionalCardFrom = repository.findByCardNumber(request.getCardFromNumber());
        if (!optionalCardFrom.isPresent()) {
            throw new UnauthorizedBankAccount("Карта отправителя не найдена");
        }
        BankAccount cardFrom = optionalCardFrom.get();

        if (!cardFrom.getCvv().equals(request.getCardFromCVV()) ||
                !cardFrom.getValidTill().equals(request.getCardFromValidTill())) {
            throw new InvalidCredentials("Неверные данные карты отправителя");
        }

        Optional<BankAccount> optionalCardTo = repository.findByCardNumber(request.getCardToNumber());
        if (!optionalCardTo.isPresent()) {
            throw new UnauthorizedBankAccount("Карта получателя не найдена");
        }


        if (cardFrom.getBalance() < request.getAmount().getValue()) {
            throw new InvalidCredentials("Недостаточно средств на счете");
        }

        String operationId = UUID.randomUUID().toString();
        pendingOperations.put(operationId, request);
        return operationId;
    }

    public String confirmOperation(ConfirmOperationRequest request) {
        TransferRequest transferInfo = pendingOperations.get(request.getOperationId());

        if (transferInfo == null) {
            throw new InvalidCredentials("Операция не найден или уже завершена");
        }

        if (!"0000".equals(request.getCode())) {
            throw new InvalidCredentials("Неверный код подтверждения");
        }

        BankAccount cardFrom = repository.findByCardNumber(transferInfo.getCardFromNumber()).get();
        BankAccount cardTo = repository.findByCardNumber(transferInfo.getCardToNumber()).get();

        BankAccount firstLock = cardFrom.getCardNumber().compareTo(cardTo.getCardNumber()) < 0 ? cardFrom : cardTo;
        BankAccount secondLock = cardFrom.getCardNumber().compareTo(cardTo.getCardNumber()) < 0 ? cardTo : cardFrom;

        firstLock.getLock().lock();
        try {
            secondLock.getLock().lock();
            try {
                long amount = transferInfo.getAmount().getValue();
                long commission = amount / 100;
                long totalDeduction = amount + commission;
                if (cardFrom.getBalance() < totalDeduction) {
                    throw new InvalidCredentials("Недостаточно средств на счете");
                }
                cardFrom.withdraw(totalDeduction);
                cardTo.deposit(amount);
                log.info("Перевод успешно выполнен. Карта списания: {}, Карта зачисления: {}, Сумма: {}, Комиссия: {}",
                        cardFrom.getCardNumber(),
                        cardTo.getCardNumber(),
                        amount,
                        commission);
            } finally {
                secondLock.getLock().unlock();
            }
        } finally {
            firstLock.getLock().unlock();
        }
        pendingOperations.remove(request.getOperationId());

        return request.getOperationId();
    }
}
