package com.increff.pos.util;

import com.increff.pos.entity.*;
import com.increff.pos.model.data.InvoiceClientForm;
import com.increff.pos.model.data.InvoiceItemData;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InvoiceConverter {

    public static InvoiceClientForm convert(OrderEntity order, List<OrderItemEntity> items,
                                            Map<Integer, ProductEntity> productMap, String clientName) {

        InvoiceClientForm data = new InvoiceClientForm();
        data.setOrderId(order.getId());
        data.setClientName(clientName);

        List<InvoiceItemData> invoiceItems = items.stream().map(item -> {return createInvoiceItemData(productMap, item);})
                                                        .collect(Collectors.toList());

        data.setItems(invoiceItems);

        BigDecimal total = invoiceItems.stream().map(InvoiceItemData::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        data.setTotalAmount(total);
        return data;
    }

    private static InvoiceItemData createInvoiceItemData(Map<Integer, ProductEntity> productMap, OrderItemEntity item) {
        ProductEntity product = productMap.get(item.getProductId());
        InvoiceItemData f = new InvoiceItemData();
        f.setProductName(product.getProductName());
        f.setQuantity(item.getQuantity());
        f.setSellingPrice(item.getSellingPrice());

        BigDecimal lineTotal = item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        f.setLineTotal(lineTotal);

        return f;
    }
}
