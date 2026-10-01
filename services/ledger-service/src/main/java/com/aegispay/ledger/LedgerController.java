package com.aegispay.ledger;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/ledger")
public class LedgerController {
    private final LedgerTransactionRepository transactions;

    public LedgerController(LedgerTransactionRepository transactions) {
        this.transactions = transactions;
    }

    @GetMapping("/transactions")
    public List<LedgerTransaction> transactions() {
        return transactions.findAll();
    }
}
