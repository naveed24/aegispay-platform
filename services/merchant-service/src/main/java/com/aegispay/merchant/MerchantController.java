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
    public Merchant create(@Valid @RequestBody CreateMerchantRequest request) {
        byte[] bytes = new byte[24];
        random.nextBytes(bytes);
        return repository.save(new Merchant(
                UUID.randomUUID(),
                request.name(),
                "agp_" + HexFormat.of().formatHex(bytes),
                request.webhookUrl()));
    }

    @GetMapping
    public List<Merchant> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Merchant get(@PathVariable UUID id) {
        return repository.findById(id).orElseThrow(() -> new MerchantNotFoundException(id));
    }

    @GetMapping("/{id}/webhook")
    public WebhookConfig webhook(@PathVariable UUID id) {
        Merchant merchant = get(id);
        return new WebhookConfig(merchant.getId().toString(), merchant.getWebhookUrl(), merchant.isActive());
    }

    public record CreateMerchantRequest(@NotBlank String name, String webhookUrl) {}
    public record WebhookConfig(String merchantId, String webhookUrl, boolean active) {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class MerchantNotFoundException extends RuntimeException {
        MerchantNotFoundException(UUID id) { super("Merchant not found: " + id); }
    }
}
