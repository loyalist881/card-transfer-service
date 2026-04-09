package com.example.cardtransferrest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.ToString;

@Data
public class TransferRequest {
    @NotBlank
    @Size(min = 16, max = 16)
    private String cardFromNumber;

    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{2}$")
    private String cardFromValidTill;

    @NotBlank
    @Size(min = 3, max = 3)
    @ToString.Exclude
    private String cardFromCVV;

    @NotBlank
    @Size(min = 16, max = 16)
    private String cardToNumber;

    @Valid
    @NotNull
    private Amount amount;
}
