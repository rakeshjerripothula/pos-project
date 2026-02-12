package com.increff.pos.dto;

import com.increff.pos.client.InvoiceClient;
import com.increff.pos.entity.InvoiceEntity;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.InvoiceForm;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderPageForm;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.util.ConversionUtil;
import com.increff.pos.util.PdfUtil;
import com.increff.pos.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderDto extends AbstractDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private InvoiceClient invoiceClient;

    public OrderPageData getOrders(OrderPageForm form) {

        ZonedDateTime start = null;
        ZonedDateTime end = null;

        try {
            if (!Objects.isNull(form.getStartDate())) {
                start = ZonedDateTime.parse(form.getStartDate());
            }

            if (!Objects.isNull(form.getEndDate())) {
                end = ZonedDateTime.parse(form.getEndDate());
            }
        } catch (Exception e) {
            throw new ApiException(
                    ApiStatus.BAD_DATA, "Invalid date format. Please use ISO date format (e.g., 2023-01-01T00:00:00Z)",
                    "dates", "Invalid date format"
            );
        }

        ValidationUtil.validateOptionalDateRange(start, end);

        int page = !Objects.isNull(form.getPage()) ? form.getPage() : 0;
        int pageSize = !Objects.isNull(form.getPageSize()) ? form.getPageSize() : 10;

        Page<OrderEntity> pageResult =
                orderFlow.searchOrders(form.getStatus(), form.getClientId(), start, end, page, pageSize);

        List<OrderData> orders = pageResult.getContent().stream().map(ConversionUtil::orderEntityToData).toList();

        OrderPageData response = new OrderPageData();
        response.setContent(orders);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalElements(pageResult.getTotalElements());

        return response;
    }

    public List<OrderItemData> getOrderItems(Integer orderId) {
        validateOrderId(orderId);
        return orderFlow.getOrderItems(orderId);
    }

    public OrderData create(OrderForm form) {
        checkValid(form);

        List<OrderItemEntity> items = form.getItems().stream().map(ConversionUtil::orderItemFormToEntity)
                .collect(Collectors.toList());

        return ConversionUtil.orderEntityToData(orderFlow.createOrder(items));
    }

    public OrderData cancel(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order ID is required", "orderId", "Order ID is required");
        }
        return ConversionUtil.orderEntityToData(orderFlow.cancelOrder(orderId));
    }

    public InvoiceSummaryData generateInvoice(Integer orderId) {

        InvoiceForm form = orderFlow.buildInvoiceForm(orderId);

        InvoiceData data = invoiceClient.generate(form);

        String filePath = PdfUtil.save(data.getBase64Pdf(), orderId);

        InvoiceEntity invoice = orderFlow.saveInvoice(orderId, filePath);

        return ConversionUtil.convertInvoiceEntityToSummary(invoice);
    }

    public byte[] downloadInvoice(Integer orderId) {

        if (orderId == null) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order ID is required", "orderId", "Order ID is required");
        }

        return orderFlow.downloadInvoice(orderId);
    }

    private void validateOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order ID is required", "orderId", "Order ID is required");
        }
    }

}
