package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.InventoryData;
import com.increff.pos.model.data.PagedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class InventoryFlowTest {

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private InventoryFlow inventoryFlow;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void should_list_all_inventories_for_enabled_clients_successfully() {
        // Arrange
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100),
            createInventoryEntity(2, 200)
        );
        
        Map<Integer, ProductEntity> productMap = new HashMap<>();
        productMap.put(1, createProductEntity(1, "Product 1", 1));
        productMap.put(2, createProductEntity(2, "Product 2", 2));
        
        when(inventoryApi.getAllForEnabledClients()).thenReturn(inventories);
        when(productApi.getByIds(Arrays.asList(1, 2))).thenReturn(Arrays.asList(productMap.get(1), productMap.get(2)));

        // Act
        List<InventoryData> result = inventoryFlow.listAllForEnabledClientsWithData();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(100, result.get(0).getQuantity());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals(2, result.get(1).getProductId());
        assertEquals(200, result.get(1).getQuantity());
        assertEquals("Product 2", result.get(1).getProductName());
        verify(inventoryApi, times(1)).getAllForEnabledClients();
        verify(productApi, times(1)).getByIds(Arrays.asList(1, 2));
    }

    @Test
    void should_return_empty_list_when_no_inventories_exist() {
        // Arrange
        when(inventoryApi.getAllForEnabledClients()).thenReturn(Collections.emptyList());

        // Act
        List<InventoryData> result = inventoryFlow.listAllForEnabledClientsWithData();

        // Assert
        assertTrue(result.isEmpty());
        verify(inventoryApi, times(1)).getAllForEnabledClients();
        verify(productApi, never()).getByIds(any());
    }

    @Test
    void should_upsert_inventory_successfully() {
        // Arrange
        InventoryEntity inventory = createInventoryEntity(1, 100);
        ProductEntity product = createProductEntity(1, "Product 1", 1);
        InventoryEntity savedInventory = createInventoryEntity(1, 100);
        
        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(inventoryApi.upsertInventory(inventory)).thenReturn(savedInventory);

        // Act
        InventoryData result = inventoryFlow.upsert(inventory);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getProductId());
        assertEquals(100, result.getQuantity());
        assertEquals("Product 1", result.getProductName());
        verify(productApi, times(1)).getProductById(1);
        verify(clientApi, times(1)).isClientEnabled(1);
        verify(inventoryApi, times(1)).upsertInventory(inventory);
    }

    @Test
    void should_throw_exception_when_upserting_inventory_for_disabled_client() {
        // Arrange
        InventoryEntity inventory = createInventoryEntity(1, 100);
        ProductEntity product = createProductEntity(1, "Product 1", 1);
        
        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.upsert(inventory));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(productApi, times(1)).getProductById(1);
        verify(clientApi, times(1)).isClientEnabled(1);
        verify(inventoryApi, never()).upsertInventory(any());
    }

    @Test
    void should_search_inventories_for_enabled_clients_successfully() {
        // Arrange
        String barcode = "BARCODE123";
        String productName = "Product 1";
        Pageable pageable = mock(Pageable.class);
        
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100)
        );
        
        Map<Integer, ProductEntity> productMap = new HashMap<>();
        productMap.put(1, createProductEntity(1, "Product 1", 1));
        
        Page<InventoryEntity> inventoryPage = new PageImpl<>(inventories);
        
        when(inventoryApi.searchForEnabledClients(barcode, productName, pageable)).thenReturn(inventoryPage);
        when(productApi.getByIds(Arrays.asList(1))).thenReturn(Arrays.asList(productMap.get(1)));

        // Act
        PagedResponse<InventoryData> result = inventoryFlow.searchForEnabledClientsWithData(barcode, productName, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getData().get(0).getProductId());
        assertEquals(100, result.getData().get(0).getQuantity());
        assertEquals("Product 1", result.getData().get(0).getProductName());
        verify(inventoryApi, times(1)).searchForEnabledClients(barcode, productName, pageable);
        verify(productApi, times(1)).getByIds(Arrays.asList(1));
    }

    @Test
    void should_return_empty_paged_response_when_search_returns_no_results() {
        // Arrange
        String barcode = "BARCODE123";
        String productName = "Product 1";
        Pageable pageable = mock(Pageable.class);
        
        Page<InventoryEntity> emptyPage = new PageImpl<>(Collections.emptyList());
        
        when(inventoryApi.searchForEnabledClients(barcode, productName, pageable)).thenReturn(emptyPage);

        // Act
        PagedResponse<InventoryData> result = inventoryFlow.searchForEnabledClientsWithData(barcode, productName, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getData().size());
        assertEquals(0L, result.getTotal());
        verify(inventoryApi, times(1)).searchForEnabledClients(barcode, productName, pageable);
        verify(productApi, never()).getByIds(any());
    }

    @Test
    void should_bulk_upsert_inventories_successfully() {
        // Arrange
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100),
            createInventoryEntity(2, 200)
        );
        
        Map<Integer, ProductEntity> productMap = new HashMap<>();
        productMap.put(1, createProductEntity(1, "Product 1", 1));
        productMap.put(2, createProductEntity(2, "Product 2", 2));
        
        List<InventoryEntity> savedInventories = Arrays.asList(
            createInventoryEntity(1, 100),
            createInventoryEntity(2, 200)
        );
        
        when(productApi.getByIds(Arrays.asList(1, 2))).thenReturn(Arrays.asList(productMap.get(1), productMap.get(2)));
        when(clientApi.getDisabledClientIds(Arrays.asList(1, 2))).thenReturn(Collections.emptyList());
        when(inventoryApi.bulkUpsert(inventories)).thenReturn(savedInventories);

        // Act
        List<InventoryData> result = inventoryFlow.bulkUpsertAndGetData(inventories);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(100, result.get(0).getQuantity());
        assertEquals("Product 1", result.get(0).getProductName());
        assertEquals(2, result.get(1).getProductId());
        assertEquals(200, result.get(1).getQuantity());
        assertEquals("Product 2", result.get(1).getProductName());
        verify(productApi, times(2)).getByIds(Arrays.asList(1, 2)); // Called twice: once in bulkUpsert, once in bulkUpsertAndGetData
        verify(clientApi, times(1)).getDisabledClientIds(Arrays.asList(1, 2));
        verify(inventoryApi, times(1)).bulkUpsert(inventories);
    }

    @Test
    void should_throw_exception_when_bulk_upserting_product_not_found() {
        // Arrange
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100)
        );
        
        when(productApi.getByIds(Arrays.asList(1))).thenReturn(Collections.emptyList());
        when(clientApi.getDisabledClientIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.bulkUpsertAndGetData(inventories));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Product not found: 1"));
        verify(productApi, times(1)).getByIds(Arrays.asList(1));
        verify(clientApi, times(1)).getDisabledClientIds(Collections.emptyList());
        verify(inventoryApi, never()).bulkUpsert(any());
    }

    @Test
    void should_throw_exception_when_bulk_upserting_for_disabled_client() {
        // Arrange
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100)
        );
        
        Map<Integer, ProductEntity> productMap = new HashMap<>();
        productMap.put(1, createProductEntity(1, "Product 1", 1));
        
        when(productApi.getByIds(Arrays.asList(1))).thenReturn(Arrays.asList(productMap.get(1)));
        when(clientApi.getDisabledClientIds(Arrays.asList(1))).thenReturn(Arrays.asList(1));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.bulkUpsertAndGetData(inventories));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(productApi, times(1)).getByIds(Arrays.asList(1));
        verify(clientApi, times(1)).getDisabledClientIds(Arrays.asList(1));
        verify(inventoryApi, never()).bulkUpsert(any());
    }

    @Test
    void should_validate_and_get_inventories_successfully() {
        // Arrange
        List<OrderItemEntity> items = Arrays.asList(
            createOrderItemEntity(1, 5),
            createOrderItemEntity(2, 3)
        );
        
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100),
            createInventoryEntity(2, 200)
        );
        
        when(inventoryApi.getByProductIds(Arrays.asList(1, 2))).thenReturn(inventories);

        // Act
        List<InventoryEntity> result = inventoryFlow.getInventoriesByProductIds(items);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(100, result.get(0).getQuantity());
        assertEquals(2, result.get(1).getProductId());
        assertEquals(200, result.get(1).getQuantity());
        verify(inventoryApi, times(1)).getByProductIds(Arrays.asList(1, 2));
        verify(productApi, never()).getByIds(any());
        verify(clientApi, never()).isClientEnabled(any());
    }

    @Test
    void should_handle_empty_order_items() {
        // Arrange
        List<OrderItemEntity> items = Collections.emptyList();
        
        when(inventoryApi.getByProductIds(Collections.emptyList())).thenReturn(Collections.emptyList());

        // Act
        List<InventoryEntity> result = inventoryFlow.getInventoriesByProductIds(items);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(inventoryApi, times(1)).getByProductIds(Collections.emptyList());
        verify(productApi, never()).getByIds(any());
        verify(clientApi, never()).isClientEnabled(any());
    }

    @Test
    void should_throw_exception_when_validating_null_order_items() {
        // Arrange
        List<OrderItemEntity> items = null;

        // Act & Assert - The current implementation throws NullPointerException, not ApiException
        assertThrows(NullPointerException.class, () -> inventoryFlow.getInventoriesByProductIds(items));
        verify(inventoryApi, never()).getByProductIds(any());
        verify(productApi, never()).getByIds(any());
        verify(clientApi, never()).isClientEnabled(any());
    }

    @Test
    void should_throw_exception_when_validating_order_items_with_missing_inventory() {
        // Arrange
        List<OrderItemEntity> items = Arrays.asList(
            createOrderItemEntity(1, 5)
        );
        
        when(inventoryApi.getByProductIds(Arrays.asList(1))).thenReturn(Collections.emptyList());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.getInventoriesByProductIds(items));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertTrue(exception.getMessage().contains("Inventory not found for one or more products"));
        verify(inventoryApi, times(1)).getByProductIds(Arrays.asList(1));
        verify(productApi, never()).getByIds(any());
        verify(clientApi, never()).isClientEnabled(any());
    }

    @Test
    void should_return_inventories_when_found() {
        // Arrange
        List<OrderItemEntity> items = Arrays.asList(
            createOrderItemEntity(1, 5)
        );
        
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100)
        );
        
        when(inventoryApi.getByProductIds(Arrays.asList(1))).thenReturn(inventories);

        // Act
        List<InventoryEntity> result = inventoryFlow.getInventoriesByProductIds(items);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(100, result.get(0).getQuantity());
        verify(inventoryApi, times(1)).getByProductIds(Arrays.asList(1));
        verify(productApi, never()).getByIds(any());
        verify(clientApi, never()).isClientEnabled(any());
    }

    @Test
    void should_return_inventories_for_multiple_products() {
        // Arrange
        List<OrderItemEntity> items = Arrays.asList(
            createOrderItemEntity(1, 5),
            createOrderItemEntity(2, 3)
        );
        
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100),
            createInventoryEntity(2, 200)
        );
        
        when(inventoryApi.getByProductIds(Arrays.asList(1, 2))).thenReturn(inventories);

        // Act
        List<InventoryEntity> result = inventoryFlow.getInventoriesByProductIds(items);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(100, result.get(0).getQuantity());
        assertEquals(2, result.get(1).getProductId());
        assertEquals(200, result.get(1).getQuantity());
        verify(inventoryApi, times(1)).getByProductIds(Arrays.asList(1, 2));
        verify(productApi, never()).getByIds(any());
        verify(clientApi, never()).isClientEnabled(any());
    }

    @Test
    void should_return_inventory_when_found() {
        // Arrange
        List<OrderItemEntity> items = Arrays.asList(
            createOrderItemEntity(1, 5)
        );
        
        List<InventoryEntity> inventories = Arrays.asList(
            createInventoryEntity(1, 100)
        );
        
        when(inventoryApi.getByProductIds(Arrays.asList(1))).thenReturn(inventories);

        // Act
        List<InventoryEntity> result = inventoryFlow.getInventoriesByProductIds(items);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getProductId());
        assertEquals(100, result.get(0).getQuantity());
        verify(inventoryApi, times(1)).getByProductIds(Arrays.asList(1));
        verify(productApi, never()).getByIds(any());
        verify(clientApi, never()).isClientEnabled(any());
    }

    private InventoryEntity createInventoryEntity(Integer productId, Integer quantity) {
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    private ProductEntity createProductEntity(Integer id, String name, Integer clientId) {
        ProductEntity product = new ProductEntity();
        product.setId(id);
        product.setProductName(name);
        product.setClientId(clientId);
        return product;
    }

    private OrderItemEntity createOrderItemEntity(Integer productId, Integer quantity) {
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }
}
