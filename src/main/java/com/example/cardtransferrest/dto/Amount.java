package com.example.cardtransferrest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class Amount {
    @NotNull
    @Positive(message = "Сумма должна быть больше нуля")
    private Integer value;

    @NotBlank
    private String currency;
}
