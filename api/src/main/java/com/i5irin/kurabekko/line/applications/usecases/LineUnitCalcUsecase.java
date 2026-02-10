package com.i5irin.kurabekko.line.applications.usecases;

import com.i5irin.kurabekko.domain.service.UnitPriceCalculator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LineUnitCalcUsecase {
  private static final String COMMAND_PREFIX = "くらべっこ";

  // Whitespace including full-width space: \s plus \u3000 (full-width space)
  // Number: allow "12" or "12.34" (.5 is not supported)
  private static final Pattern COMMAND_PATTERN =
      Pattern.compile("^" + COMMAND_PREFIX + "(?:[\\s\\u3000]+)(?<pairs>.+)$");

  private static final Pattern PAIR_PATTERN =
      Pattern.compile(
          "^\\s*(?<price>\\d+(?:\\.\\d+)?)"
              + "(?:[\\s\\u3000]+)"
              + "(?<amount>\\d+(?:\\.\\d+)?)\\s*$");

  private final UnitPriceCalculator unitPriceCalculator;

  public LineUnitCalcUsecase(UnitPriceCalculator unitPriceCalculator) {
    this.unitPriceCalculator = unitPriceCalculator;
  }

  public LineUnitCalcUsecaseResult execute(String input) {
    Optional<List<PriceAmount>> parsed = Optional.empty();
    try {
      parsed = parseKurabekko(input);
    } catch (IllegalArgumentException e) {
      System.out.println(e.getMessage());
      return new LineUnitCalcUsecaseResult(
          null, null, false, "正しい形式で入力してください。\n例: " + COMMAND_PREFIX + " 12 6, 15 3");
    }
    if (parsed.isEmpty()) {
      throw new IllegalArgumentException("Not a Kurabekko command");
    }
    List<PriceAmount> items = parsed.get();
    // Create a List of combinations of unit price, price, and amount
    List<PriceAmountWithUnitPrice> unitPrices =
        items.stream()
            .map(
                pa ->
                    new PriceAmountWithUnitPrice(
                        pa.price(),
                        pa.amount(),
                        (unitPriceCalculator.calc(pa.price(), pa.amount(), null))
                            .unitPricePerBase()))
            .toList();
    // Create a text message listing unit prices separated by commas
    String unitPricesText =
        unitPrices.stream()
            .map(up -> String.valueOf(up.unitPrice()))
            .collect(java.util.stream.Collectors.joining(", "));
    String unitPriceMessage = "単価一覧: " + unitPricesText;
    // Create a text message indicating the cheapest unit price along with its price, amount,
    // and index
    String cheapestMessage;
    {
      var cheapest =
          unitPrices.stream()
              .min((a, b) -> Double.compare(a.unitPrice(), b.unitPrice()))
              .orElseThrow();
      int index = unitPrices.indexOf(cheapest) + 1;
      cheapestMessage =
          String.format(
              "最安値は %d 番目\n単価 %.2f （価格: %.2f, 量: %.2f）です。",
              index, cheapest.unitPrice(), cheapest.price(), cheapest.amount());
    }

    return new LineUnitCalcUsecaseResult(unitPriceMessage, cheapestMessage, true, null);
  }

  public record LineUnitCalcUsecaseResult(
      String unitPriceMessage, String cheapestMessage, boolean success, String errorMessage) {}

  public record PriceAmount(double price, double amount) {}

  record PriceAmountWithUnitPrice(double price, double amount, double unitPrice) {}

  /**
   * If the input contains "くらべっこ", returns List<PriceAmount>. If there is any invalid format,
   * throws IllegalArgumentException. If it does not start with "くらべっこ", returns Optional.empty().
   */
  private static Optional<List<PriceAmount>> parseKurabekko(String input) {
    System.out.println("Parsing input: '" + input.trim() + "'");
    if (input.strip().equals(COMMAND_PREFIX)) {
      throw new IllegalArgumentException("No arguments.");
    }
    Matcher m = COMMAND_PATTERN.matcher(input);
    if (!m.matches()) {
      return Optional.empty();
    }

    String pairsPart = m.group("pairs");

    // Comma-separated (allow whitespace before and after comma; trailing whitespace optional)
    String[] segments = pairsPart.split("\\s*,\\s*");

    List<PriceAmount> result = new ArrayList<>();
    for (String seg : segments) {
      if (seg.isBlank()) {
        continue;
      }
      Matcher pm = PAIR_PATTERN.matcher(seg);
      if (!pm.matches()) {
        // e.g., only price "11" or only amount "2" falls here
        throw new IllegalArgumentException("Invalid format: '" + seg);
      }

      double price = Double.parseDouble(pm.group("price"));
      double amount = Double.parseDouble(pm.group("amount"));

      result.add(new PriceAmount(price, amount));
    }

    if (result.isEmpty()) {
      throw new IllegalArgumentException("No price-amount pairs found.");
    }

    return Optional.of(result);
  }
}
