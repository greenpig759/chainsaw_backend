package com.block.chainsaw.contract.subscription;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SubscriptionRequestDTO {
    private String targetEmail;
}
