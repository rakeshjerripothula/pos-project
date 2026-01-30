package com.increff.pos.util;

import com.increff.pos.entity.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.model.internal.SalesReportRow;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public final class ConversionUtil {

    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    private ConversionUtil() {

    }

    public static OrderData orderEntityToData(OrderEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        
        OrderData data = new OrderData();
        data.setOrderId(entity.getId());
        data.setClientId(entity.getClientId());
        data.setCreatedAt(entity.getCreatedAt());
        data.setStatus(entity.getStatus());
        return data;
    }

    public static OrderItemEntity orderItemFormToEntity(OrderItemForm form) {
        if (Objects.isNull(form)) {
            return null;
        }
        
        OrderItemEntity entity = new OrderItemEntity();
        entity.setProductId(form.getProductId());
        entity.setQuantity(form.getQuantity());
        entity.setSellingPrice(form.getSellingPrice().setScale(2, RoundingMode.HALF_UP));
        return entity;
    }

    public static List<OrderItemData> orderItemEntitiesToData(List<OrderItemEntity> items, Map<Integer, ProductEntity> productMap) {
        if (Objects.isNull(items) || items.isEmpty()) {
            return List.of();
        }

        return items.stream()
                .map(item -> orderItemEntityToData(item, productMap))
                .collect(Collectors.toList());
    }

    public static OrderItemData orderItemEntityToData(OrderItemEntity item, Map<Integer, ProductEntity> productMap) {
        if (Objects.isNull(item)) {
            return null;
        }
        
        OrderItemData itemData = new OrderItemData();
        itemData.setProductId(item.getProductId());
        itemData.setQuantity(item.getQuantity());
        itemData.setSellingPrice(item.getSellingPrice().setScale(2, RoundingMode.HALF_UP));
        
        ProductEntity product = productMap.get(item.getProductId());
        if (Objects.nonNull(product)) {
            itemData.setProductName(product.getProductName());
        }
        
        return itemData;
    }

    public static ProductEntity productFormToEntity(ProductForm form) {
        
        ProductEntity entity = new ProductEntity();
        entity.setProductName(normalize(form.getProductName()));
        entity.setMrp(form.getMrp().setScale(2, RoundingMode.HALF_UP));
        entity.setClientId(form.getClientId());
        entity.setBarcode(form.getBarcode());
        entity.setImageUrl(form.getImageUrl());
        return entity;
    }

    public static ProductData productEntityToData(ProductEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        
        ProductData data = new ProductData();
        data.setId(entity.getId());
        data.setProductName(entity.getProductName());
        data.setMrp(entity.getMrp());
        data.setClientId(entity.getClientId());
        data.setBarcode(entity.getBarcode());
        data.setImageUrl(entity.getImageUrl());
        return data;
    }

    // ==================== INVENTORY CONVERSIONS ====================

    public static InventoryEntity inventoryFormToEntity(InventoryForm form) {
        if (Objects.isNull(form)) {
            return null;
        }
        
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(form.getProductId());
        entity.setQuantity(form.getQuantity());
        return entity;
    }

    public static InventoryData inventoryEntityToData(InventoryEntity entity, ProductEntity product) {
        if (Objects.isNull(entity)) {
            return null;
        }
        
        InventoryData data = new InventoryData();
        data.setProductId(entity.getProductId());
        data.setQuantity(entity.getQuantity());
        
        if (Objects.nonNull(product)) {
            data.setProductName(product.getProductName());
        }
        
        return data;
    }

    public static UserData userEntityToData(UserEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        
        UserData data = new UserData();
        data.setId(entity.getId());
        data.setEmail(entity.getEmail());
        data.setRole(entity.getRole());
        return data;
    }

    public static ClientEntity clientFormToEntity(ClientForm form) {
        if (Objects.isNull(form)) {
            return null;
        }
        
        ClientEntity entity = new ClientEntity();
        entity.setClientName(normalize(form.getClientName()));
        return entity;
    }

    public static ClientData clientEntityToData(ClientEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        
        ClientData data = new ClientData();
        data.setId(entity.getId());
        data.setClientName(entity.getClientName());
        data.setEnabled(entity.getEnabled());
        return data;
    }

    public static InvoiceItemForm orderItemEntityToInvoiceItemForm(OrderItemEntity item, Map<Integer, ProductEntity> productMap) {
        ProductEntity product = productMap.get(item.getProductId());
        if (product == null) {
            throw new ApiException(ApiStatus.NOT_FOUND, "Product not found: " + item.getProductId());
        }

        InvoiceItemForm form = new InvoiceItemForm();
        form.setProductName(product.getProductName());
        form.setQuantity(item.getQuantity());
        form.setSellingPrice(item.getSellingPrice());

        BigDecimal lineTotal =
                item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        form.setLineTotal(lineTotal);

        return form;
    }

    public static InvoiceSummaryData invoiceEntityToSummaryData(InvoiceEntity invoiceEntity){
        InvoiceSummaryData invoiceData = new InvoiceSummaryData();
        invoiceData.setOrderId(invoiceEntity.getOrderId());
        invoiceData.setCreatedAt(invoiceEntity.getCreatedAt());
        return invoiceData;
    }

    public static DaySalesData daySalesEntityToData(DaySalesEntity entity) {
        if (Objects.isNull(entity)) {
            return null;
        }
        
        DaySalesData data = new DaySalesData();
        data.setDate(entity.getDate());
        data.setInvoicedOrdersCount(entity.getInvoicedOrdersCount());
        data.setInvoicedItemsCount(entity.getInvoicedItemsCount());
        data.setTotalRevenue(entity.getTotalRevenue());
        return data;
    }

    public static SalesReportRowData salesReportRowToData(SalesReportRow row) {
        SalesReportRowData data = new SalesReportRowData();
        data.setProductName(row.getProductName());
        data.setQuantitySold(row.getQuantitySold().intValue());
        data.setRevenue(row.getRevenue().doubleValue());
        return data;
    }


    public static String normalize(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }

    public static BigDecimal scaleToTwoDecimalPlaces(BigDecimal value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public static <S, T> T map(S source, Class<T> targetClass) {
        if (Objects.isNull(source)) {
            return null;
        }
        return modelMapper.map(source, targetClass);
    }

    public static <S, T> List<T> mapAll(List<S> sourceList, Class<T> targetClass) {
        if (Objects.isNull(sourceList) || sourceList.isEmpty()) {
            return List.of();
        }
        return sourceList.stream()
                .map(source -> modelMapper.map(source, targetClass))
                .collect(Collectors.toList());
    }

}
