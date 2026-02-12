package com.increff.pos.util;

import com.increff.pos.entity.*;
import com.increff.pos.model.form.*;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceConverter {

    public static InvoiceForm convert(OrderEntity order, List<OrderItemEntity> items,
                                      Map<Integer, ProductEntity> productMap, String clientName) {

        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-" + order.getId());
        form.setInvoiceDate(order.getCreatedAt().withZoneSameInstant(ZoneOffset.UTC));
        form.setClientName(clientName);

        List<InvoiceItemForm> invoiceItems = items.stream().map(item -> {
            ProductEntity product = productMap.get(item.getProductId());

            InvoiceItemForm f = new InvoiceItemForm();
            f.setProductName(product.getProductName());
            f.setQuantity(item.getQuantity());
            f.setSellingPrice(item.getSellingPrice());

            BigDecimal lineTotal = item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            f.setLineTotal(lineTotal);

            return f;
        }).collect(Collectors.toList());

        form.setItems(invoiceItems);

        BigDecimal total = invoiceItems.stream().map(InvoiceItemForm::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        form.setTotalAmount(total);
        return form;
    }
}
