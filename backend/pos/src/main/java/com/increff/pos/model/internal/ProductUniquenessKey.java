package com.increff.pos.model.internal;

import java.math.BigDecimal;

public record ProductUniquenessKey(
        Integer clientId,
        String productName,
        BigDecimal mrp
) {}
