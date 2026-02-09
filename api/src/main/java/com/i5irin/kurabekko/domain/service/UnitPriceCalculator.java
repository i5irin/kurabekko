package com.i5irin.kurabekko.domain.service;

import org.springframework.stereotype.Service;

@Service
public class UnitPriceCalculator {

  public Result calc(double price, double amount, String unitRaw) {
    boolean unitOmitted = (unitRaw == null);

    if (unitOmitted) {
      return new Result(price / amount, "pcs");
    }
    String unit = normalize(unitRaw);

    return switch (unit) {
      case "g" -> new Result(price / amount, "g");
      case "kg" -> new Result(price / (amount * 1000.0), "g");
      case "ml" -> new Result(price / amount, "ml");
      case "l" -> new Result(price / (amount * 1000.0), "ml");
      case "pcs" -> new Result(price / amount, "pcs");
      default -> throw new IllegalArgumentException("Unsupported unit: " + unitRaw);
    };
  }

  private String normalize(String u) {
    String x = u.trim().toLowerCase();
    return switch (x) {
      case "g", "gram", "grams" -> "g";
      case "kg", "kilogram", "kilograms" -> "kg";
      case "ml" -> "ml";
      case "l", "lt", "liter", "litre" -> "l";
      case "pc", "pcs", "piece", "pieces" -> "pcs";
      default -> x;
    };
  }

  public record Result(double unitPricePerBase, String baseUnit) {}
}
