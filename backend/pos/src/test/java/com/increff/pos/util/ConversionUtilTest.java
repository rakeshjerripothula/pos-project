package com.increff.pos.util;

import com.increff.pos.domain.OrderStatus;
import com.increff.pos.domain.UserRole;
import com.increff.pos.entity.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.*;
import com.increff.pos.model.form.*;
import com.increff.pos.model.internal.SalesReportRow;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConversionUtilTest {

    @Test
    void should_convert_order_entity_to_data_when_valid_entity() {
        // Arrange
        OrderEntity entity = new OrderEntity();
        entity.setId(1);
        entity.setClientId(2);
        entity.setStatus(OrderStatus.valueOf("CREATED"));
        
        // Act
        OrderData result = ConversionUtil.orderEntityToData(entity);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertEquals(2, result.getClientId());
        assertEquals("CREATED", result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void should_return_null_when_converting_null_order_entity() {
        // Act
        OrderData result = ConversionUtil.orderEntityToData(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_order_item_form_to_entity_when_valid_form() {
        // Arrange
        OrderItemForm form = new OrderItemForm();
        form.setProductId(1);
        form.setQuantity(5);
        form.setSellingPrice(new BigDecimal("10.50"));
        
        // Act
        OrderItemEntity result = ConversionUtil.orderItemFormToEntity(form);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(5, result.getQuantity());
        assertEquals(new BigDecimal("10.50"), result.getSellingPrice());
    }

    @Test
    void should_return_null_when_converting_null_order_item_form() {
        // Act
        OrderItemEntity result = ConversionUtil.orderItemFormToEntity(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_round_selling_price_when_converting_order_item_form() {
        // Arrange
        OrderItemForm form = new OrderItemForm();
        form.setProductId(1);
        form.setQuantity(5);
        form.setSellingPrice(new BigDecimal("10.556"));
        
        // Act
        OrderItemEntity result = ConversionUtil.orderItemFormToEntity(form);
        
        // Assert
        assertEquals(new BigDecimal("10.56"), result.getSellingPrice());
    }

    @Test
    void should_convert_order_item_entities_to_data_when_valid_list() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);
        item1.setQuantity(5);
        item1.setSellingPrice(new BigDecimal("10.50"));
        
        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(2);
        item2.setQuantity(3);
        item2.setSellingPrice(new BigDecimal("20.00"));
        
        ProductEntity product1 = new ProductEntity();
        product1.setProductName("Product 1");
        
        ProductEntity product2 = new ProductEntity();
        product2.setProductName("Product 2");
        
        Map<Integer, ProductEntity> productMap = Map.of(1, product1, 2, product2);
        
        // Act
        List<OrderItemData> result = ConversionUtil.orderItemEntitiesToData(List.of(item1, item2), productMap);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals("Product 2", result.get(1).getProductName());
    }

    @Test
    void should_return_empty_list_when_converting_empty_order_item_entities() {
        // Act
        List<OrderItemData> result = ConversionUtil.orderItemEntitiesToData(List.of(), Map.of());
        
        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void should_return_empty_list_when_converting_null_order_item_entities() {
        // Act
        List<OrderItemData> result = ConversionUtil.orderItemEntitiesToData(null, Map.of());
        
        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void should_convert_order_item_entity_to_data_when_valid_entity() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(5);
        item.setSellingPrice(new BigDecimal("10.50"));
        
        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        
        Map<Integer, ProductEntity> productMap = Map.of(1, product);
        
        // Act
        OrderItemData result = ConversionUtil.orderItemEntityToData(item, productMap);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(5, result.getQuantity());
        assertEquals(new BigDecimal("10.50"), result.getSellingPrice());
        assertEquals("Test Product", result.getProductName());
    }

    @Test
    void should_return_null_when_converting_null_order_item_entity() {
        // Act
        OrderItemData result = ConversionUtil.orderItemEntityToData(null, Map.of());
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_product_form_to_entity_when_valid_form() {
        // Arrange
        ProductForm form = new ProductForm();
        form.setProductName("Test Product");
        form.setMrp(new BigDecimal("100.00"));
        form.setClientId(1);
        form.setBarcode("12345");
        form.setImageUrl("http://example.com/image.jpg");
        
        // Act
        ProductEntity result = ConversionUtil.productFormToEntity(form);
        
        // Assert
        assertNotNull(result);
        assertEquals("test product", result.getProductName());
        assertEquals(new BigDecimal("100.00"), result.getMrp());
        assertEquals(1, result.getClientId());
        assertEquals("12345", result.getBarcode());
        assertEquals("http://example.com/image.jpg", result.getImageUrl());
    }

    @Test
    void should_round_mrp_when_converting_product_form() {
        // Arrange
        ProductForm form = new ProductForm();
        form.setProductName("Test Product");
        form.setMrp(new BigDecimal("100.556"));
        form.setClientId(1);
        form.setBarcode("12345");
        
        // Act
        ProductEntity result = ConversionUtil.productFormToEntity(form);
        
        // Assert
        assertEquals(new BigDecimal("100.56"), result.getMrp());
    }

    @Test
    void should_convert_product_entity_to_data_when_valid_entity() {
        // Arrange
        ProductEntity entity = new ProductEntity();
        entity.setId(1);
        entity.setProductName("Test Product");
        entity.setMrp(new BigDecimal("100.00"));
        entity.setClientId(2);
        entity.setBarcode("12345");
        entity.setImageUrl("http://example.com/image.jpg");
        
        // Act
        ProductData result = ConversionUtil.productEntityToData(entity);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Product", result.getProductName());
        assertEquals(new BigDecimal("100.00"), result.getMrp());
        assertEquals(2, result.getClientId());
        assertEquals("12345", result.getBarcode());
        assertEquals("http://example.com/image.jpg", result.getImageUrl());
    }

    @Test
    void should_return_null_when_converting_null_product_entity() {
        // Act
        ProductData result = ConversionUtil.productEntityToData(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_inventory_form_to_entity_when_valid_form() {
        // Arrange
        InventoryForm form = new InventoryForm();
        form.setProductId(1);
        form.setQuantity(10);
        
        // Act
        InventoryEntity result = ConversionUtil.inventoryFormToEntity(form);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
    }

    @Test
    void should_return_null_when_converting_null_inventory_form() {
        // Act
        InventoryEntity result = ConversionUtil.inventoryFormToEntity(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_inventory_entity_to_data_when_valid_entity() {
        // Arrange
        InventoryEntity entity = new InventoryEntity();
        entity.setProductId(1);
        entity.setQuantity(10);
        
        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        
        // Act
        InventoryData result = ConversionUtil.inventoryEntityToData(entity, product);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        assertEquals("Test Product", result.getProductName());
    }

    @Test
    void should_return_null_when_converting_null_inventory_entity() {
        // Act
        InventoryData result = ConversionUtil.inventoryEntityToData(null, null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_user_entity_to_data_when_valid_entity() {
        // Arrange
        UserEntity entity = new UserEntity();
        entity.setId(1);
        entity.setEmail("test@example.com");
        entity.setRole(UserRole.valueOf("ADMIN"));
        
        // Act
        UserData result = ConversionUtil.userEntityToData(entity);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void should_return_null_when_converting_null_user_entity() {
        // Act
        UserData result = ConversionUtil.userEntityToData(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_client_form_to_entity_when_valid_form() {
        // Arrange
        ClientForm form = new ClientForm();
        form.setClientName("Test Client");
        
        // Act
        ClientEntity result = ConversionUtil.clientFormToEntity(form);
        
        // Assert
        assertNotNull(result);
        assertEquals("test client", result.getClientName());
    }

    @Test
    void should_return_null_when_converting_null_client_form() {
        // Act
        ClientEntity result = ConversionUtil.clientFormToEntity(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_client_entity_to_data_when_valid_entity() {
        // Arrange
        ClientEntity entity = new ClientEntity();
        entity.setId(1);
        entity.setClientName("Test Client");
        entity.setEnabled(true);
        
        // Act
        ClientData result = ConversionUtil.clientEntityToData(entity);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Test Client", result.getClientName());
        assertTrue(result.getEnabled());
    }

    @Test
    void should_return_null_when_converting_null_client_entity() {
        // Act
        ClientData result = ConversionUtil.clientEntityToData(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_order_item_entity_to_invoice_item_form_when_valid_data() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        item.setQuantity(5);
        item.setSellingPrice(new BigDecimal("10.50"));
        
        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        
        Map<Integer, ProductEntity> productMap = Map.of(1, product);
        
        // Act
        InvoiceItemForm result = ConversionUtil.orderItemEntityToInvoiceItemForm(item, productMap);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        assertEquals(5, result.getQuantity());
        assertEquals(new BigDecimal("10.50"), result.getSellingPrice());
        assertEquals(new BigDecimal("52.50"), result.getLineTotal());
    }

    @Test
    void should_throw_exception_when_product_not_found_for_invoice_item() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);
        
        Map<Integer, ProductEntity> productMap = Map.of();
        
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, 
            () -> ConversionUtil.orderItemEntityToInvoiceItemForm(item, productMap));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Product not found: 1"));
    }

    @Test
    void should_convert_invoice_entity_to_summary_data_when_valid_entity() {
        // Arrange
        InvoiceEntity entity = new InvoiceEntity();
        entity.setOrderId(1);
        
        // Act
        InvoiceSummaryData result = ConversionUtil.invoiceEntityToSummaryData(entity);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getOrderId());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void should_convert_day_sales_entity_to_data_when_valid_entity() {
        // Arrange
        DaySalesEntity entity = new DaySalesEntity();
        entity.setDate(LocalDate.parse("2023-01-01"));
        entity.setInvoicedOrdersCount(10);
        entity.setInvoicedItemsCount(50);
        entity.setTotalRevenue(new BigDecimal("1000.00"));
        
        // Act
        DaySalesData result = ConversionUtil.daySalesEntityToData(entity);
        
        // Assert
        assertNotNull(result);
        assertEquals("2023-01-01", result.getDate());
        assertEquals(10, result.getInvoicedOrdersCount());
        assertEquals(50, result.getInvoicedItemsCount());
        assertEquals(new BigDecimal("1000.00"), result.getTotalRevenue());
    }

    @Test
    void should_return_null_when_converting_null_day_sales_entity() {
        // Act
        DaySalesData result = ConversionUtil.daySalesEntityToData(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_sales_report_row_to_data_when_valid_row() {
        // Arrange
        SalesReportRow row = new SalesReportRow("Test Product", 5, new BigDecimal("100.00"));
        
        // Act
        SalesReportRowData result = ConversionUtil.salesReportRowToData(row);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        assertEquals(5, result.getQuantitySold());
        assertEquals(100.0, result.getRevenue());
    }

    @Test
    void should_normalize_string_when_valid_input() {
        // Arrange
        String input = "  TEST STRING  ";
        
        // Act
        String result = ConversionUtil.normalize(input);
        
        // Assert
        assertEquals("test string", result);
    }

    @Test
    void should_return_null_when_normalizing_null_string() {
        // Act
        String result = ConversionUtil.normalize(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_convert_tsv_row_to_product_form_when_valid_row() {
        // Arrange
        String[] row = {"Test Product", "100.50", "1", "12345", "http://example.com/image.jpg"};
        
        // Act
        ProductForm result = ConversionUtil.tsvRowToProductForm(row);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        assertEquals(new BigDecimal("100.50"), result.getMrp());
        assertEquals(1, result.getClientId());
        assertEquals("12345", result.getBarcode());
        assertEquals("http://example.com/image.jpg", result.getImageUrl());
    }

    @Test
    void should_convert_tsv_row_to_product_form_when_minimal_row() {
        // Arrange
        String[] row = {"Test Product", "100.50", "1", "12345"};
        
        // Act
        ProductForm result = ConversionUtil.tsvRowToProductForm(row);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        assertEquals(new BigDecimal("100.50"), result.getMrp());
        assertEquals(1, result.getClientId());
        assertEquals("12345", result.getBarcode());
        assertNull(result.getImageUrl());
    }

    @Test
    void should_convert_tsv_row_to_product_form_when_empty_image_url() {
        // Arrange
        String[] row = {"Test Product", "100.50", "1", "12345", ""};
        
        // Act
        ProductForm result = ConversionUtil.tsvRowToProductForm(row);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
        assertEquals(new BigDecimal("100.50"), result.getMrp());
        assertEquals(1, result.getClientId());
        assertEquals("12345", result.getBarcode());
        assertEquals("", result.getImageUrl());
    }

    @Test
    void should_map_object_when_valid_source() {
        // Arrange
        ProductForm source = new ProductForm();
        source.setProductName("Test Product");
        
        // Act
        ProductData result = ConversionUtil.map(source, ProductData.class);
        
        // Assert
        assertNotNull(result);
        assertEquals("Test Product", result.getProductName());
    }

    @Test
    void should_return_null_when_mapping_null_source() {
        // Act
        ProductData result = ConversionUtil.map(null, ProductData.class);
        
        // Assert
        assertNull(result);
    }

    @Test
    void should_map_all_objects_when_valid_list() {
        // Arrange
        ProductForm form1 = new ProductForm();
        form1.setProductName("Product 1");
        
        ProductForm form2 = new ProductForm();
        form2.setProductName("Product 2");
        
        List<ProductForm> sourceList = List.of(form1, form2);
        
        // Act
        List<ProductData> result = ConversionUtil.mapAll(sourceList, ProductData.class);
        
        // Assert
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals("Product 2", result.get(1).getProductName());
    }

    @Test
    void should_return_empty_list_when_mapping_empty_list() {
        // Act
        List<ProductData> result = ConversionUtil.mapAll(List.of(), ProductData.class);
        
        // Assert
        assertEquals(0, result.size());
    }

    @Test
    void should_return_empty_list_when_mapping_null_list() {
        // Act
        List<ProductData> result = ConversionUtil.mapAll(null, ProductData.class);
        
        // Assert
        assertEquals(0, result.size());
    }
}
