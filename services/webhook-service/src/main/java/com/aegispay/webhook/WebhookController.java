package com.aegispay.webhook;

import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/webhooks")
public class WebhookController {
    private final WebhookDeliveryRepository repository;
    public WebhookController(WebhookDeliveryRepository repository) { this.repository = repository; }

    @GetMapping
    public List<WebhookDelivery> list() { return repository.findAll(); }
}
