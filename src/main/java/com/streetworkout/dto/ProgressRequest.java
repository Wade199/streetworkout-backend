package com.streetworkout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProgressRequest {

    @NotNull(message = "La date est obligatoire")
    private LocalDate progressDate;

    private BigDecimal weight;
    private String notes;
}
