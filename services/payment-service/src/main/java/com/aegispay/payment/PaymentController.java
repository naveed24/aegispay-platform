package com.aegispay.payment;

import com.aegispay.payment.PaymentOrchestrator.CreatePaymentCommand;
import com.aegispay.payment.PaymentOrchestrator.IdempotencyConflictException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentOrchestrator orchestrator;
    private final PaymentRepository repository;

    public PaymentController(PaymentOrchestrator orchestrator, PaymentRepository repository) {
        this.orchestrator = orchestrator;
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Payment create(
            @RequestHeader("Idempotency-Key") @NotBlank String idempotencyKey,
            @RequestHeader("X-API-Key") @NotBlank String apiKey,
            @Valid @RequestBody CreatePaymentRequest request) {
        return orchestrator.create(
                new CreatePaymentCommand(request.merchantId(), request.amount(), request.currency()),
                idempotencyKey,
                apiKey);
    }

    @GetMapping
    public List<Payment> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Payment get(@PathVariable UUID id) {
        return repository.findById(id).orElseThrow(NotFoundException::new);
    }

    public record CreatePaymentRequest(
            @NotBlank String merchantId,
            @NotNull @DecimalMin("0.01") BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency) {}

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(IdempotencyConflictException.class)
    void idempotencyConflict() {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class NotFoundException extends RuntimeException {}
}
