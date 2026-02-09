package com.i5irin.kurabekko.app.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CalcRequest(
    @Positive double price,
    @Positive double amount,
    @JsonSetter(nulls = Nulls.FAIL) @Pattern(regexp = "\\S+", message = "unit must not be blank")
        String unit) {}
