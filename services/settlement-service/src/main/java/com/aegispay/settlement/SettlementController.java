package com.aegispay.settlement;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/settlements")
public class SettlementController {
    private final SettlementRepository repository;
    public SettlementController(SettlementRepository repository) { this.repository = repository; }

    @GetMapping
    public List<Settlement> list() { return repository.findAll(); }
}
