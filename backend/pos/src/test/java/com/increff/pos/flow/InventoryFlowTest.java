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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryFlowTest {

    @Mock
    private InventoryApi inventoryApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private InventoryFlow inventoryFlow;

    @Test
    void should_get_inventory_by_product_ids_when_enabled_clients() {
        // Arrange
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);
        inventory2.setQuantity(5);

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setClientId(1);

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setClientId(1);

        when(inventoryApi.getByProductIds(List.of(1, 2))).thenReturn(inventories);
        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.getDisabledClientIds(List.of(1))).thenReturn(List.of());


        // Act
        List<InventoryEntity> result = inventoryFlow.getByProductIds(List.of(1, 2));

        // Assert
        assertEquals(2, result.size());
        verify(inventoryApi).getByProductIds(List.of(1, 2));
        verify(productApi).getByIds(List.of(1, 2));
        verify(clientApi).getDisabledClientIds(List.of(1));
    }

    @Test
    void should_throw_exception_when_getting_inventory_for_disabled_client() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        when(inventoryApi.getByProductIds(List.of(1))).thenReturn(List.of(inventory));
        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.getDisabledClientIds(List.of(1))).thenReturn(List.of(1));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.getByProductIds(List.of(1)));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(inventoryApi).getByProductIds(List.of(1));
        verify(productApi).getByIds(List.of(1));
        verify(clientApi).getDisabledClientIds(List.of(1));
    }

    @Test
    void should_return_empty_list_when_no_inventory_found_by_product_ids() {
        // Arrange
        when(inventoryApi.getByProductIds(List.of(1, 2))).thenReturn(List.of());

        // Act
        List<InventoryEntity> result = inventoryFlow.getByProductIds(List.of(1, 2));

        // Assert
        assertEquals(0, result.size());
        verify(inventoryApi).getByProductIds(List.of(1, 2));
        verifyNoInteractions(productApi, clientApi);
    }

    @Test
    void should_throw_exception_when_product_not_found_for_inventory() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        when(inventoryApi.getByProductIds(List.of(1))).thenReturn(List.of(inventory));
        when(productApi.getByIds(List.of(1))).thenReturn(List.of());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.getByProductIds(List.of(1)));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(inventoryApi).getByProductIds(List.of(1));
        verify(productApi).getByIds(List.of(1));
        verifyNoInteractions(clientApi);
    }

    @Test
    void should_handle_multiple_clients_in_get_by_product_ids() {
        // Arrange
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);
        inventory2.setQuantity(5);

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setClientId(1);

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setClientId(2);

        when(inventoryApi.getByProductIds(List.of(1, 2))).thenReturn(inventories);
        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.getDisabledClientIds(List.of(1, 2))).thenReturn(List.of());


        // Act
        List<InventoryEntity> result = inventoryFlow.getByProductIds(List.of(1, 2));

        // Assert
        assertEquals(2, result.size());
        verify(inventoryApi).getByProductIds(List.of(1, 2));
        verify(productApi).getByIds(List.of(1, 2));
        verify(clientApi).getDisabledClientIds(List.of(1, 2));
    }

    @Test
    void should_upsert_inventory_when_client_enabled() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        InventoryEntity savedInventory = new InventoryEntity();
        savedInventory.setProductId(1);
        savedInventory.setQuantity(10);

        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(inventoryApi.upsert(inventory)).thenReturn(savedInventory);

        // Act
        InventoryEntity result = inventoryFlow.upsert(inventory);

        // Assert
        assertEquals(savedInventory, result);
        verify(productApi).getProductById(1);
        verify(clientApi).isClientEnabled(1);
        verify(inventoryApi).upsert(inventory);
    }

    @Test
    void should_throw_exception_when_upserting_inventory_for_disabled_client() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.upsert(inventory));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(productApi).getProductById(1);
        verify(clientApi).isClientEnabled(1);
        verifyNoInteractions(inventoryApi);
    }

    @Test
    void should_upsert_and_get_data() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        InventoryEntity savedInventory = new InventoryEntity();
        savedInventory.setProductId(1);
        savedInventory.setQuantity(10);

        InventoryData expectedData = new InventoryData();
        expectedData.setProductId(1);
        expectedData.setQuantity(10);

        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(inventoryApi.upsert(inventory)).thenReturn(savedInventory);

        try (MockedStatic<com.increff.pos.util.ConversionUtil> mockedConversionUtil = mockStatic(com.increff.pos.util.ConversionUtil.class)) {
            mockedConversionUtil.when(() -> com.increff.pos.util.ConversionUtil.inventoryEntityToData(savedInventory, product))
                    .thenReturn(expectedData);

            // Act
            InventoryData result = inventoryFlow.upsertAndGetData(inventory);

            // Assert
            assertEquals(expectedData, result);
            verify(productApi, times(2)).getProductById(1);
            verify(clientApi).isClientEnabled(1);
            verify(inventoryApi).upsert(inventory);
        }
    }

    @Test
    void should_validate_and_get_inventories_successfully() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(2);

        List<OrderItemEntity> items = List.of(item1, item2);

        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setClientId(1);

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setClientId(1);

        when(inventoryApi.getByProductIds(List.of(1, 2))).thenReturn(inventories);
        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.isClientEnabled(1)).thenReturn(true);

        // Act
        List<InventoryEntity> result = inventoryFlow.validateAndGetInventories(items);

        // Assert
        assertEquals(2, result.size());
        verify(inventoryApi).getByProductIds(List.of(1, 2));
        verify(productApi).getByIds(List.of(1, 2));
        verify(clientApi).isClientEnabled(1);
    }

    @Test
    void should_throw_exception_when_inventory_not_found_for_validation() {
        // Arrange
        OrderItemEntity item = new OrderItemEntity();
        item.setProductId(1);

        when(inventoryApi.getByProductIds(List.of(1))).thenReturn(List.of());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.validateAndGetInventories(List.of(item)));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Inventory not found for one or more products", exception.getMessage());
        verify(inventoryApi).getByProductIds(List.of(1));
        verifyNoInteractions(productApi, clientApi);
    }

    @Test
    void should_throw_exception_when_products_belong_to_different_clients() {
        // Arrange
        OrderItemEntity item1 = new OrderItemEntity();
        item1.setProductId(1);

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setProductId(2);

        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);
        product1.setClientId(1);

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);
        product2.setClientId(2);

        when(inventoryApi.getByProductIds(List.of(1, 2))).thenReturn(List.of(inventory1, inventory2));
        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.validateAndGetInventories(List.of(item1, item2)));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("All products in an order must belong to the same client", exception.getMessage());
    }

    @Test
    void should_get_inventory_by_product_id() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        when(inventoryApi.getByProductId(1)).thenReturn(inventory);
        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(true);

        // Act
        InventoryEntity result = inventoryFlow.getByProductId(1);

        // Assert
        assertEquals(inventory, result);
        verify(inventoryApi).getByProductId(1);
        verify(productApi).getProductById(1);
        verify(clientApi).isClientEnabled(1);
    }

    @Test
    void should_throw_exception_when_getting_inventory_for_disabled_client_by_id() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        when(inventoryApi.getByProductId(1)).thenReturn(inventory);
        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.getByProductId(1));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(inventoryApi).getByProductId(1);
        verify(productApi).getProductById(1);
        verify(clientApi).isClientEnabled(1);
    }

    @Test
    void should_get_inventory_by_product_id_with_data() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        InventoryData expectedData = new InventoryData();
        expectedData.setProductId(1);
        expectedData.setQuantity(10);

        when(inventoryApi.getByProductId(1)).thenReturn(inventory);
        when(productApi.getProductById(1)).thenReturn(product);
        when(clientApi.isClientEnabled(1)).thenReturn(true);

        try (MockedStatic<com.increff.pos.util.ConversionUtil> mockedConversionUtil = mockStatic(com.increff.pos.util.ConversionUtil.class)) {
            mockedConversionUtil.when(() -> com.increff.pos.util.ConversionUtil.inventoryEntityToData(inventory, product))
                    .thenReturn(expectedData);

            // Act
            InventoryData result = inventoryFlow.getByProductIdWithData(1);

            // Assert
            assertEquals(expectedData, result);
            verify(inventoryApi).getByProductId(1);
            verify(productApi, times(2)).getProductById(1);
            verify(clientApi).isClientEnabled(1);
        }
    }

    @Test
    void should_list_inventories_for_enabled_clients() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<InventoryEntity> page = mock(Page.class);

        when(inventoryApi.listForEnabledClients(pageable)).thenReturn(page);

        // Act
        Page<InventoryEntity> result = inventoryFlow.listForEnabledClients(pageable);

        // Assert
        assertEquals(page, result);
        verify(inventoryApi).listForEnabledClients(pageable);
    }

    @Test
    void should_list_inventories_for_enabled_clients_with_data() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<InventoryEntity> page = mock(Page.class);

        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        InventoryData expectedData = new InventoryData();
        expectedData.setProductId(1);
        expectedData.setQuantity(10);

        when(page.isEmpty()).thenReturn(false);
        when(page.getContent()).thenReturn(List.of(inventory));
        when(page.getTotalElements()).thenReturn(1L);
        when(inventoryApi.listForEnabledClients(pageable)).thenReturn(page);
        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));

        try (MockedStatic<com.increff.pos.util.ConversionUtil> mockedConversionUtil = mockStatic(com.increff.pos.util.ConversionUtil.class)) {
            mockedConversionUtil.when(() -> com.increff.pos.util.ConversionUtil.inventoryEntityToData(inventory, product))
                    .thenReturn(expectedData);

            // Act
            PagedResponse<InventoryData> result = inventoryFlow.listForEnabledClientsWithData(pageable);

            // Assert
            assertEquals(1, result.getData().size());
            assertEquals(1L, result.getTotal());
            verify(inventoryApi).listForEnabledClients(pageable);
            verify(productApi).getByIds(List.of(1));
        }
    }

    @Test
    void should_return_empty_paged_response_when_no_inventories_found() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        Page<InventoryEntity> page = mock(Page.class);

        when(page.isEmpty()).thenReturn(true);
        when(inventoryApi.listForEnabledClients(pageable)).thenReturn(page);

        // Act
        PagedResponse<InventoryData> result = inventoryFlow.listForEnabledClientsWithData(pageable);

        // Assert
        assertEquals(0, result.getData().size());
        assertEquals(0L, result.getTotal());
        verify(inventoryApi).listForEnabledClients(pageable);
        verifyNoInteractions(productApi);
    }

    @Test
    void should_list_all_inventories_for_enabled_clients() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        List<InventoryEntity> inventories = List.of(inventory);
        when(inventoryApi.listAllForEnabledClients()).thenReturn(inventories);

        // Act
        List<InventoryEntity> result = inventoryFlow.listAllForEnabledClients();

        // Assert
        assertEquals(inventories, result);
        verify(inventoryApi).listAllForEnabledClients();
    }

    @Test
    void should_list_all_inventories_for_enabled_clients_with_data() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        InventoryData expectedData = new InventoryData();
        expectedData.setProductId(1);
        expectedData.setQuantity(10);

        when(inventoryApi.listAllForEnabledClients()).thenReturn(List.of(inventory));
        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));

        try (MockedStatic<com.increff.pos.util.ConversionUtil> mockedConversionUtil = mockStatic(com.increff.pos.util.ConversionUtil.class)) {
            mockedConversionUtil.when(() -> com.increff.pos.util.ConversionUtil.inventoryEntityToData(inventory, product))
                    .thenReturn(expectedData);

            // Act
            List<InventoryData> result = inventoryFlow.listAllForEnabledClientsWithData();

            // Assert
            assertEquals(1, result.size());
            verify(inventoryApi).listAllForEnabledClients();
            verify(productApi).getByIds(List.of(1));
        }
    }

    @Test
    void should_return_empty_list_when_no_all_inventories_found() {
        // Arrange
        when(inventoryApi.listAllForEnabledClients()).thenReturn(List.of());

        // Act
        List<InventoryData> result = inventoryFlow.listAllForEnabledClientsWithData();

        // Assert
        assertEquals(0, result.size());
        verify(inventoryApi).listAllForEnabledClients();
        verifyNoInteractions(productApi);
    }

    @Test
    void should_bulk_upsert_and_get_data() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        InventoryEntity savedInventory = new InventoryEntity();
        savedInventory.setProductId(1);
        savedInventory.setQuantity(10);

        InventoryData expectedData = new InventoryData();
        expectedData.setProductId(1);
        expectedData.setQuantity(10);

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.getDisabledClientIds(List.of(1))).thenReturn(List.of());
        when(inventoryApi.bulkUpsert(List.of(inventory))).thenReturn(List.of(savedInventory));

        try (MockedStatic<com.increff.pos.util.ConversionUtil> mockedConversionUtil = mockStatic(com.increff.pos.util.ConversionUtil.class)) {
            mockedConversionUtil.when(() -> com.increff.pos.util.ConversionUtil.inventoryEntityToData(savedInventory, product))
                    .thenReturn(expectedData);

            // Act
            List<InventoryData> result = inventoryFlow.bulkUpsertAndGetData(List.of(inventory));

            // Assert
            assertEquals(1, result.size());
            verify(productApi, times(2)).getByIds(List.of(1));
            verify(clientApi).getDisabledClientIds(List.of(1));
            verify(inventoryApi).bulkUpsert(List.of(inventory));
        }
    }

    @Test
    void should_bulk_upsert_inventories() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        InventoryEntity savedInventory = new InventoryEntity();
        savedInventory.setProductId(1);
        savedInventory.setQuantity(10);

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.getDisabledClientIds(List.of(1))).thenReturn(List.of());
        when(inventoryApi.bulkUpsert(List.of(inventory))).thenReturn(List.of(savedInventory));

        // Act
        List<InventoryEntity> result = inventoryFlow.bulkUpsert(List.of(inventory));

        // Assert
        assertEquals(1, result.size());
        verify(productApi).getByIds(List.of(1));
        verify(clientApi).getDisabledClientIds(List.of(1));
        verify(inventoryApi).bulkUpsert(List.of(inventory));
    }

    @Test
    void should_throw_exception_when_bulk_upserting_for_disabled_client() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        ProductEntity product = new ProductEntity();
        product.setId(1);
        product.setClientId(1);

        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.getDisabledClientIds(List.of(1))).thenReturn(List.of(1));

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.bulkUpsert(List.of(inventory)));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(productApi).getByIds(List.of(1));
        verify(clientApi).getDisabledClientIds(List.of(1));
        verifyNoInteractions(inventoryApi);
    }

    @Test
    void should_throw_exception_when_product_not_found_during_bulk_upsert() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        when(productApi.getByIds(List.of(1))).thenReturn(List.of());
        when(clientApi.getDisabledClientIds(any())).thenReturn(List.of());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryFlow.bulkUpsert(List.of(inventory)));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Product not found: 1", exception.getMessage());
        verify(productApi).getByIds(List.of(1));
        verify(inventoryApi, never()).bulkUpsert(any());
    }

    @Test
    void should_get_product_ids_from_inventories() throws Exception {
        // Arrange
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);

        ProductEntity product1 = new ProductEntity();
        product1.setId(1);

        ProductEntity product2 = new ProductEntity();
        product2.setId(2);

        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));

        // Act
        Method method = InventoryFlow.class.getDeclaredMethod("getProductIds", List.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, ProductEntity> result = (Map<Integer, ProductEntity>) method.invoke(inventoryFlow, inventories);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.containsKey(1));
        assertTrue(result.containsKey(2));
        verify(productApi).getByIds(List.of(1, 2));
    }
}
