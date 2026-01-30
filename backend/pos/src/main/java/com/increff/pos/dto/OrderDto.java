package com.increff.pos.dto;

import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.data.OrderPageData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderPageForm;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderDto extends AbstractDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    public OrderData create(OrderForm form) {
        checkValid(form);

        List<OrderItemEntity> items = form.getItems()
                .stream()
                .map(ConversionUtil::orderItemFormToEntity)
                .collect(Collectors.toList());

        return ConversionUtil.orderEntityToData(orderFlow.createOrder(items));
    }

    public OrderData getById(Integer orderId) {
        validateOrderId(orderId);

        OrderEntity order = orderFlow.getById(orderId);
        OrderData data = ConversionUtil.orderEntityToData(order);
        
        // Load order items with product details
        List<OrderItemEntity> items = orderItemApi.getByOrderId(orderId);
        
        // Get product details
        List<Integer> productIds = items.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();
        List<ProductEntity> products = productApi.getByIds(productIds);
        Map<Integer, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));
        
        List<OrderItemData> itemDataList = ConversionUtil.orderItemEntitiesToData(items, productMap);
        data.setItems(itemDataList);
        
        return data;
    }

    public List<OrderItemData> getOrderItems(Integer orderId) {
        validateOrderId(orderId);
        List<OrderItemEntity> items = orderItemApi.getByOrderId(orderId);
        
        // Get product details
        List<Integer> productIds = items.stream()
                .map(OrderItemEntity::getProductId)
                .distinct()
                .toList();
        List<ProductEntity> products = productApi.getByIds(productIds);
        Map<Integer, ProductEntity> productMap = products.stream()
                .collect(Collectors.toMap(ProductEntity::getId, product -> product));
        
        return ConversionUtil.orderItemEntitiesToData(items, productMap);
    }

    public OrderData cancel(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order ID is required", "orderId", "Order ID is required");
        }
        return ConversionUtil.orderEntityToData(orderFlow.cancelOrder(orderId));
    }

    public OrderPageData getOrders(OrderPageForm form) {

        ZonedDateTime start = null;
        ZonedDateTime end = null;

        if (!Objects.isNull(form.getStartDate())) {
            start = ZonedDateTime.parse(form.getStartDate());
        }
        if (!Objects.isNull(form.getEndDate())) {
            end = ZonedDateTime.parse(form.getEndDate());
        }

        int page = !Objects.isNull(form.getPage()) ? form.getPage() : 0;
        int pageSize = !Objects.isNull(form.getPageSize()) ? form.getPageSize() : 10;

        Page<OrderEntity> pageResult =
                orderFlow.searchOrders(form.getStatus(), form.getClientId(), start, end, page, pageSize);

        List<OrderData> orders = pageResult.getContent().stream()
                .map(ConversionUtil::orderEntityToData)
                .toList();

        OrderPageData response = new OrderPageData();
        response.setContent(orders);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalElements(pageResult.getTotalElements());

        return response;
    }

    private void validateOrderId(Integer orderId) {
        if (Objects.isNull(orderId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Order ID is required", "orderId", "Order ID is required");
        }
    }

}
