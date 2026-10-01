package com.aegispay.payment;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class RequestHasher {
    private RequestHasher() {}

    public static String hash(String merchantId, BigDecimal amount, String currency) {
        try {
            String canonical = merchantId + "|" + amount.stripTrailingZeros().toPlainString() + "|" + currency.toUpperCase();
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
