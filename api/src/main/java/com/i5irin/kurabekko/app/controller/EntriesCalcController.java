package com.i5irin.kurabekko.app.controller;

import com.i5irin.kurabekko.app.dto.CalcRequest;
import com.i5irin.kurabekko.app.dto.CalcResponse;
import com.i5irin.kurabekko.domain.service.UnitPriceCalculator;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EntriesCalcController {

  private final UnitPriceCalculator calculator;

  public EntriesCalcController(UnitPriceCalculator calculator) {
    this.calculator = calculator;
  }

  @PostMapping("/app/entries/calc")
  public CalcResponse calc(@Valid @RequestBody CalcRequest req) {
    var r = calculator.calc(req.price(), req.amount(), req.unit());
    return new CalcResponse(r.unitPricePerBase(), r.baseUnit());
  }
}
