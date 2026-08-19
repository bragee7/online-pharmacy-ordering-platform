package com.medicare.pharmacy.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class OrderNumberGenerator {

    private OrderNumberGenerator() {
    }

    public static String generate() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 6);
        return "MC-" + date + "-" + random;
    }

    public static String trackingNumber() {
        String random = UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 10);
        return "TRK" + random;
    }

    public static String transactionRef() {
        String random = UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 4);
        return "TXN" + System.currentTimeMillis() + random;
    }
}
