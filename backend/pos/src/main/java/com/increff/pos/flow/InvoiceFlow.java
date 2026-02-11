package com.increff.pos.flow;

import com.increff.pos.api.*;
import com.increff.pos.domain.OrderStatus;
import com.increff.pos.entity.InvoiceEntity;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.form.InvoiceForm;
import com.increff.pos.model.form.InvoiceItemForm;
import com.increff.pos.model.data.InvoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.increff.pos.util.ConversionUtil.orderItemEntityToInvoiceItemForm;

@Service
@Transactional
public class InvoiceFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private ClientApi clientApi;

    @Autowired
    private ProductApi productApi;

    public InvoiceEntity generateInvoice(Integer orderId) {
        if (invoiceApi.existsForOrder(orderId)) {
            throw new ApiException(ApiStatus.ORDER_ALREADY_INVOICED, "Invoice already generated for order " + orderId);
        }

        OrderEntity order = orderApi.getById(orderId);

        List<OrderItemEntity> orderItems = orderItemApi.getByOrderId(orderId);

        InvoiceForm form = buildInvoiceForm(order, orderItems);

        InvoiceData invoiceData = invoiceApi.generate(form);

        String filePath = savePdf(orderId, invoiceData.getBase64Pdf());

        InvoiceEntity invoice = new InvoiceEntity();
        invoice.setOrderId(orderId);
        invoice.setFilePath(filePath);

        InvoiceEntity invoiceEntity = invoiceApi.create(invoice);

        order.setStatus(OrderStatus.INVOICED);
        orderApi.update(order);

        return invoiceEntity;
    }

    public byte[] downloadInvoice(Integer orderId) {
        return invoiceApi.downloadInvoice(orderId);
    }


    private InvoiceForm buildInvoiceForm(OrderEntity order, List<OrderItemEntity> items) {
        List<Integer> productIds = items.stream().map(OrderItemEntity::getProductId).distinct().toList();
        
        List<ProductEntity> products = productApi.getByIds(productIds);
        Map<Integer, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));

        InvoiceForm form = new InvoiceForm();
        form.setInvoiceNumber("INV-" + order.getId());
        form.setInvoiceDate(order.getCreatedAt());

        String clientName = clientApi.getById(order.getClientId()).getClientName();;
        form.setClientName(clientName);

        List<InvoiceItemForm> invoiceItems = items.stream()
                .map(item -> orderItemEntityToInvoiceItemForm(item, productMap))
                .collect(Collectors.toList());

        form.setItems(invoiceItems);

        BigDecimal total = invoiceItems.stream().map(InvoiceItemForm::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        form.setTotalAmount(total);

        return form;
    }

    private String savePdf(Integer orderId, String base64Pdf) {
        try {
            byte[] pdfBytes = Base64.getDecoder().decode(base64Pdf);

            String dirPath = "invoices";
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filePath = dirPath + "/invoice-" + orderId + ".pdf";

            try (FileOutputStream fos = new FileOutputStream(filePath)) {
                fos.write(pdfBytes);
            }

            return filePath;

        } catch (Exception e) {
            throw new ApiException(ApiStatus.INTERNAL_ERROR, "Failed to save invoice PDF");
        }
    }
}
