package com.aegispay.payment;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RequestHasherTest {
    @Test
    void canonicalizesEquivalentAmountsAndCurrencyCase() {
        assertEquals(
                RequestHasher.hash("m-1", new BigDecimal("100.00"), "inr"),
                RequestHasher.hash("m-1", new BigDecimal("100"), "INR"));
    }

    @Test
    void changesWhenPaymentIntentChanges() {
        assertNotEquals(
                RequestHasher.hash("m-1", new BigDecimal("100"), "INR"),
                RequestHasher.hash("m-1", new BigDecimal("101"), "INR"));
    }
}
