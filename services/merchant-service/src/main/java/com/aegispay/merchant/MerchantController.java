package com.aegispay.merchant;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/merchants")
public class MerchantController {
    private final MerchantRepository repository;
    private final SecureRandom random = new SecureRandom();

    public MerchantController(MerchantRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreatedMerchant create(@Valid @RequestBody CreateMerchantRequest request) {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        Merchant merchant = repository.save(new Merchant(
                UUID.randomUUID(),
                request.name(),
                "agp_" + HexFormat.of().formatHex(bytes),
                request.webhookUrl()));
        return new CreatedMerchant(
                merchant.getId().toString(),
                merchant.getName(),
                merchant.getApiKey(),
                merchant.getWebhookUrl(),
                merchant.isActive());
    }

    @GetMapping
    public List<MerchantSummary> list() {
        return repository.findAll().stream().map(MerchantController::summary).toList();
    }

    @GetMapping("/{id}")
    public MerchantSummary get(@PathVariable UUID id) {
        return summary(repository.findById(id).orElseThrow(() -> new MerchantNotFoundException(id)));
    }

    private static MerchantSummary summary(Merchant merchant) {
        return new MerchantSummary(
                merchant.getId().toString(),
                merchant.getName(),
                merchant.getWebhookUrl(),
                merchant.isActive(),
                merchant.getCreatedAt());
    }

    public record CreateMerchantRequest(@NotBlank String name, String webhookUrl) {}
    public record CreatedMerchant(String id, String name, String apiKey, String webhookUrl, boolean active) {}
    public record MerchantSummary(String id, String name, String webhookUrl, boolean active, java.time.Instant createdAt) {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class MerchantNotFoundException extends RuntimeException {
        MerchantNotFoundException(UUID id) { super("Merchant not found: " + id); }
    }
}
