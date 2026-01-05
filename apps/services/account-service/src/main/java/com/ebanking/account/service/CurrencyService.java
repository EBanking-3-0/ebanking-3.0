package com.ebanking.account.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class CurrencyService {

    private static final BigDecimal EUR_TO_MAD = new BigDecimal("10.42");
    private static final BigDecimal USD_TO_MAD = new BigDecimal("9.92");
    private static final BigDecimal EUR_TO_USD = new BigDecimal("1.05");

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return amount;
        }

        // Convert to pivot (MAD) then to target? Or just direct cases since we have
        // few.
        // Let's implement direct cases and pivot via EUR or MAD if needed.
        // Request: 1 EUR = 10.42 MAD, 1 USD = 9.92 MAD, 1 EUR = 1.05 USD

        // EUR -> MAD
        if (fromCurrency.equalsIgnoreCase("EUR") && toCurrency.equalsIgnoreCase("MAD")) {
            return amount.multiply(EUR_TO_MAD);
        }
        // MAD -> EUR
        if (fromCurrency.equalsIgnoreCase("MAD") && toCurrency.equalsIgnoreCase("EUR")) {
            return amount.divide(EUR_TO_MAD, 2, RoundingMode.HALF_UP);
        }

        // USD -> MAD
        if (fromCurrency.equalsIgnoreCase("USD") && toCurrency.equalsIgnoreCase("MAD")) {
            return amount.multiply(USD_TO_MAD);
        }
        // MAD -> USD
        if (fromCurrency.equalsIgnoreCase("MAD") && toCurrency.equalsIgnoreCase("USD")) {
            return amount.divide(USD_TO_MAD, 2, RoundingMode.HALF_UP);
        }

        // EUR -> USD
        if (fromCurrency.equalsIgnoreCase("EUR") && toCurrency.equalsIgnoreCase("USD")) {
            return amount.multiply(EUR_TO_USD);
        }
        // USD -> EUR
        if (fromCurrency.equalsIgnoreCase("USD") && toCurrency.equalsIgnoreCase("EUR")) {
            return amount.divide(EUR_TO_USD, 2, RoundingMode.HALF_UP);
        }

        throw new IllegalArgumentException("Unsupported conversion: " + fromCurrency + " to " + toCurrency);
    }
}
