package com.block.chainsaw.contract.subscription;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubcriptionController {
    private final SubscriptionService subscriptionService;

    // 구독 요청 API
    @PostMapping
    public ResponseEntity<?> subcribe(
            @AuthenticationPrincipal String email,
            @RequestBody SubscriptionRequestDTO dto
    ){
        subscriptionService.subcribe(email, dto.getTargetEmail());
        return ResponseEntity.ok(Map.of("message", "구독 성공"));
    }
}
