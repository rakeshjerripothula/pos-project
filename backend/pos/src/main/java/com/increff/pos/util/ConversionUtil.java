package com.increff.pos.util;

import com.increff.pos.entity.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.model.internal.DaySalesAggregate;
import com.increff.pos.model.internal.InventoryUploadModel;
import com.increff.pos.model.internal.ProductUploadModel;
import com.increff.pos.model.internal.SalesReportRow;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public final class ConversionUtil {

    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT).setSkipNullEnabled(true);

        modelMapper.typeMap(OrderEntity.class, OrderData.class).addMapping(OrderEntity::getId, OrderData::setOrderId)
                .addMappings(mapper -> mapper.skip(OrderData::setItems));
    }

    private ConversionUtil() {}

    public static ClientEntity clientFormToEntity(ClientForm form) {
        ClientEntity entity = new ClientEntity();
        entity.setClientName(normalize(form.getClientName()));
        return entity;
    }

    public static ClientData clientEntityToData(ClientEntity entity) {
        return map(entity, ClientData.class);
    }

    public static OrderData orderEntityToData(OrderEntity entity) {
        return map(entity, OrderData.class);
    }

    public static OrderItemEntity orderItemFormToEntity(OrderItemForm form) {
        OrderItemEntity entity = new OrderItemEntity();
        entity.setProductId(form.getProductId());
        entity.setQuantity(form.getQuantity());
        entity.setSellingPrice(form.getSellingPrice().setScale(2, RoundingMode.HALF_UP));
        return entity;
    }

    public static OrderItemEntity createOrderItem(OrderItemEntity item, Integer orderId) {
        OrderItemEntity orderItem = new OrderItemEntity();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(item.getProductId());
        orderItem.setQuantity(item.getQuantity());
        orderItem.setSellingPrice(item.getSellingPrice());
        return orderItem;
    }

    public static List<OrderItemData> orderItemEntitiesToData(
            List<OrderItemEntity> items, Map<Integer, ProductEntity> productMap
    ) {
        if (Objects.isNull(items) || items.isEmpty()) {
            return List.of();
        }
        return items.stream().map(item -> orderItemEntityToData(item, productMap)).collect(Collectors.toList());
    }

    public static OrderItemData orderItemEntityToData(OrderItemEntity item, Map<Integer, ProductEntity> productMap) {
        if (Objects.isNull(item)) {
            return null;
        }

        OrderItemData data = new OrderItemData();
        data.setProductId(item.getProductId());
        data.setQuantity(item.getQuantity());
        data.setSellingPrice(item.getSellingPrice().setScale(2, RoundingMode.HALF_UP));

        ProductEntity product = productMap.get(item.getProductId());
        if (Objects.nonNull(product)) {
            data.setProductName(product.getProductName());
        }
        return data;
    }

    public static ProductEntity productFormToEntity(ProductForm form) {
        ProductEntity entity = new ProductEntity();
        entity.setProductName(normalize(form.getProductName()));
        entity.setMrp(form.getMrp().setScale(2, RoundingMode.HALF_UP));
        entity.setClientId(form.getClientId());
        entity.setBarcode(normalize(form.getBarcode()));
        entity.setImageUrl(form.getImageUrl());
        return entity;
    }

    public static ProductEntity convertProductUploadToEntity(ProductUploadModel upload, Integer clientId) {
        ProductEntity entity = new ProductEntity();
        entity.setClientId(clientId);
        entity.setProductName(upload.getProductName());
        entity.setBarcode(upload.getBarcode());
        entity.setMrp(upload.getMrp());
        entity.setImageUrl(upload.getImageUrl());
        return entity;
    }

    public static ProductUploadModel convertProductUploadFormToUploadModel(ProductUploadForm form){
        return map(form, ProductUploadModel.class);
    }

    public static ProductData productEntityToData(ProductEntity entity) {
        return map(entity, ProductData.class);
    }

    public static InventoryUploadModel inventoryUploadFormToModel(InventoryUploadForm uploadForm){
        return map(uploadForm, InventoryUploadModel.class);
    }

    public static InventoryEntity inventoryFormToEntity(InventoryForm form) {
        return map(form, InventoryEntity.class);
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
        return map(entity, UserData.class);
    }

    public static InvoiceItemData orderItemEntityToInvoiceItemForm(
            OrderItemEntity item, Map<Integer, ProductEntity> productMap
    ) {
        ProductEntity product = productMap.get(item.getProductId());
        if (product == null) {
            throw new ApiException(ApiStatus.NOT_FOUND,
                    "Product not found: " + item.getProductId());
        }

        InvoiceItemData form = new InvoiceItemData();
        form.setProductName(product.getProductName());
        form.setQuantity(item.getQuantity());
        form.setSellingPrice(item.getSellingPrice());

        BigDecimal lineTotal = item.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        form.setLineTotal(lineTotal);

        return form;
    }

    public static InvoiceSummaryData invoiceEntityToSummaryData(InvoiceEntity entity) {
        return map(entity, InvoiceSummaryData.class);
    }

    public static DaySalesData daySalesEntityToData(DaySalesEntity entity) {
        return map(entity, DaySalesData.class);
    }

    public static DaySalesEntity daySalesAggregateToEntity(LocalDate date, DaySalesAggregate aggregate) {
        DaySalesEntity entity = new DaySalesEntity();
        entity.setDate(date);
        entity.setInvoicedOrdersCount(aggregate.getInvoicedOrdersCountAsInt());
        entity.setInvoicedItemsCount(aggregate.getInvoicedItemsCountAsInt());
        entity.setTotalRevenue(aggregate.getTotalRevenue().setScale(2, RoundingMode.HALF_UP));
        return entity;
    }

    public static SalesReportRowData salesReportRowToData(SalesReportRow row) {
        SalesReportRowData data = new SalesReportRowData();
        data.setProductName(row.getProductName());
        data.setQuantitySold(row.getQuantitySold());
        data.setRevenue(row.getRevenue().doubleValue());
        return data;
    }

    public static ProductForm tsvRowToProductForm(String[] r) {
        ProductForm f = new ProductForm();
        f.setProductName(r[0].trim());
        f.setMrp(new BigDecimal(r[1].trim()).setScale(2, RoundingMode.HALF_UP));
        f.setClientId(Integer.parseInt(r[2].trim()));
        f.setBarcode(r[3].trim());

        if (r.length > 4) {
            f.setImageUrl(r[4].trim());
        }
        return f;
    }

    public static String normalize(String value) {
        if (Objects.isNull(value)) {
            return null;
        }
        return value.trim().toLowerCase();
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
        return sourceList.stream().map(source -> modelMapper.map(source, targetClass)).collect(Collectors.toList());
    }

    public static OrderPageData orderPageEntityToResponse(List<OrderData> orders, int page, int pageSize, long totalElements) {
        OrderPageData response = new OrderPageData();
        response.setContent(orders);
        response.setPage(page);
        response.setPageSize(pageSize);
        response.setTotalElements(totalElements);
        return response;
    }
}