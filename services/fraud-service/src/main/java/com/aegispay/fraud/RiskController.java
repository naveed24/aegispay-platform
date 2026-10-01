package com.aegispay.fraud;

import com.aegispay.fraud.RiskEngine.RiskRequest;
import com.aegispay.fraud.RiskEngine.RiskResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/risk")
public class RiskController {
    private final RiskEngine riskEngine;

    public RiskController(RiskEngine riskEngine) {
        this.riskEngine = riskEngine;
    }

    @PostMapping("/score")
    public RiskResult score(@Valid @RequestBody RiskRequest request) {
        return riskEngine.score(request);
    }
}
