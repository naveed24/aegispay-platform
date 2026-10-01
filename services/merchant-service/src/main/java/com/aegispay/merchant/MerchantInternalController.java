package com.aegispay.merchant;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/merchants")
public class MerchantInternalController {
    private final MerchantRepository repository;

    public MerchantInternalController(MerchantRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/validate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void validate(
            @RequestParam UUID merchantId,
            @RequestHeader("X-API-Key") String apiKey) {
        Merchant merchant = repository.findById(merchantId)
                .filter(Merchant::isActive)
                .filter(m -> m.getApiKey().equals(apiKey))
                .orElseThrow(UnauthorizedMerchantException::new);
    }

    @GetMapping("/{id}/webhook")
    public WebhookConfig webhook(@PathVariable UUID id) {
        Merchant merchant = repository.findById(id)
                .orElseThrow(() -> new MerchantController.MerchantNotFoundException(id));
        return new WebhookConfig(merchant.getId().toString(), merchant.getWebhookUrl(), merchant.isActive());
    }

    public record WebhookConfig(String merchantId, String webhookUrl, boolean active) {}

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    static class UnauthorizedMerchantException extends RuntimeException {}
}
