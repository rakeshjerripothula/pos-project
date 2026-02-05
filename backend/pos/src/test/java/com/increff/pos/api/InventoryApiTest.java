package com.increff.pos.api;

import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryApiTest {

    @Mock
    private InventoryDao inventoryDao;

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @InjectMocks
    private InventoryApi inventoryApi;

    @Test
    void should_upsert_inventory_when_new_product() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        when(inventoryDao.findByProductId(1)).thenReturn(Optional.empty());
        when(inventoryDao.save(any(InventoryEntity.class))).thenAnswer(invocation -> {
            InventoryEntity saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        // Act
        InventoryEntity result = inventoryApi.upsert(inventory);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryDao).findByProductId(1);
        verify(inventoryDao).save(any(InventoryEntity.class));
    }

    @Test
    void should_upsert_inventory_when_existing_product() {
        // Arrange
        InventoryEntity existingInventory = new InventoryEntity();
        existingInventory.setId(1);
        existingInventory.setProductId(1);
        existingInventory.setQuantity(5);

        InventoryEntity updateInventory = new InventoryEntity();
        updateInventory.setProductId(1);
        updateInventory.setQuantity(15);

        when(inventoryDao.findByProductId(1)).thenReturn(Optional.of(existingInventory));
        when(inventoryDao.save(any(InventoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        InventoryEntity result = inventoryApi.upsert(updateInventory);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(1, result.getProductId());
        assertEquals(15, result.getQuantity());
        verify(inventoryDao).findByProductId(1);
        verify(inventoryDao).save(existingInventory);
    }

    @Test
    void should_throw_exception_when_upserting_inventory_with_negative_quantity() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(-5);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryApi.upsert(inventory));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Quantity cannot be negative", exception.getMessage());
        verifyNoInteractions(inventoryDao);
    }

    @Test
    void should_list_all_inventory() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        List<InventoryEntity> inventories = List.of(new InventoryEntity(), new InventoryEntity());
        Page<InventoryEntity> inventoryPage = new PageImpl<>(inventories);

        when(inventoryDao.findAll(pageable)).thenReturn(inventoryPage);

        // Act
        Page<InventoryEntity> result = inventoryApi.listAll(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(inventoryDao).findAll(pageable);
    }

    @Test
    void should_get_inventory_by_product_id_when_exists() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setId(1);
        inventory.setProductId(1);
        inventory.setQuantity(10);

        when(inventoryDao.findByProductId(1)).thenReturn(Optional.of(inventory));

        // Act
        InventoryEntity result = inventoryApi.getByProductId(1);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(1, result.getProductId());
        assertEquals(10, result.getQuantity());
        verify(inventoryDao).findByProductId(1);
    }

    @Test
    void should_throw_exception_when_getting_inventory_by_product_id_not_found() {
        // Arrange
        when(inventoryDao.findByProductId(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryApi.getByProductId(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Inventory not found for productId: 1", exception.getMessage());
        verify(inventoryDao).findByProductId(1);
    }

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

        when(inventoryDao.findByProductIds(List.of(1, 2))).thenReturn(inventories);
        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.isClientEnabled(1)).thenReturn(true);

        // Act
        List<InventoryEntity> result = inventoryApi.getByProductIds(List.of(1, 2));

        // Assert
        assertEquals(2, result.size());
        verify(inventoryDao).findByProductIds(List.of(1, 2));
        verify(productApi).getByIds(List.of(1, 2));
        verify(clientApi, times(2)).isClientEnabled(1);
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

        when(inventoryDao.findByProductIds(List.of(1))).thenReturn(List.of(inventory));
        when(productApi.getByIds(List.of(1))).thenReturn(List.of(product));
        when(clientApi.isClientEnabled(1)).thenReturn(false);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryApi.getByProductIds(List.of(1)));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(inventoryDao).findByProductIds(List.of(1));
        verify(productApi).getByIds(List.of(1));
        verify(clientApi).isClientEnabled(1);
    }

    @Test
    void should_return_empty_list_when_no_inventory_found_by_product_ids() {
        // Arrange
        when(inventoryDao.findByProductIds(List.of(1, 2))).thenReturn(List.of());

        // Act
        List<InventoryEntity> result = inventoryApi.getByProductIds(List.of(1, 2));

        // Assert
        assertEquals(0, result.size());
        verify(inventoryDao).findByProductIds(List.of(1, 2));
        verifyNoInteractions(productApi, clientApi);
    }

    @Test
    void should_throw_exception_when_product_not_found_for_inventory() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(10);

        when(inventoryDao.findByProductIds(List.of(1))).thenReturn(List.of(inventory));
        when(productApi.getByIds(List.of(1))).thenReturn(List.of());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryApi.getByProductIds(List.of(1)));
        assertEquals(ApiStatus.FORBIDDEN, exception.getStatus());
        assertEquals("Client is disabled", exception.getMessage());
        verify(inventoryDao).findByProductIds(List.of(1));
        verify(productApi).getByIds(List.of(1));
        verifyNoInteractions(clientApi);
    }

    @Test
    void should_list_inventory_for_enabled_clients() {
        // Arrange
        Pageable pageable = mock(Pageable.class);
        List<InventoryEntity> inventories = List.of(new InventoryEntity(), new InventoryEntity());
        Page<InventoryEntity> inventoryPage = new PageImpl<>(inventories);

        when(inventoryDao.findForEnabledClients(pageable)).thenReturn(inventoryPage);

        // Act
        Page<InventoryEntity> result = inventoryApi.listForEnabledClients(pageable);

        // Assert
        assertEquals(2, result.getContent().size());
        verify(inventoryDao).findForEnabledClients(pageable);
    }

    @Test
    void should_bulk_upsert_inventory_when_valid_input() {
        // Arrange
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);
        inventory2.setQuantity(5);

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);

        when(inventoryDao.saveAll(inventories)).thenAnswer(invocation -> {
            List<InventoryEntity> saved = invocation.getArgument(0);
            saved.get(0).setId(1);
            saved.get(1).setId(2);
            return saved;
        });

        // Act
        List<InventoryEntity> result = inventoryApi.bulkUpsert(inventories);

        // Assert
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getId());
        assertEquals(2, result.get(1).getId());
        verify(inventoryDao).saveAll(inventories);
    }

    @Test
    void should_throw_exception_when_bulk_upserting_inventory_with_negative_quantity() {
        // Arrange
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(2);
        inventory2.setQuantity(-5); // Negative quantity

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> inventoryApi.bulkUpsert(inventories));
        assertEquals(ApiStatus.BAD_DATA, exception.getStatus());
        assertEquals("Quantity cannot be negative", exception.getMessage());
        verifyNoInteractions(inventoryDao);
    }

    @Test
    void should_handle_duplicate_product_ids_in_bulk_upsert() {
        // Arrange
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(1);
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(1); // Same product ID
        inventory2.setQuantity(15);

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);

        when(inventoryDao.saveAll(inventories)).thenReturn(inventories);

        // Act
        List<InventoryEntity> result = inventoryApi.bulkUpsert(inventories);

        // Assert
        assertEquals(2, result.size());
        verify(inventoryDao).saveAll(inventories);
    }

    @Test
    void should_handle_zero_quantity_in_upsert() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(0);

        when(inventoryDao.findByProductId(1)).thenReturn(Optional.empty());
        when(inventoryDao.save(any(InventoryEntity.class))).thenAnswer(invocation -> {
            InventoryEntity saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        // Act
        InventoryEntity result = inventoryApi.upsert(inventory);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(0, result.getQuantity());
        verify(inventoryDao).findByProductId(1);
        verify(inventoryDao).save(any(InventoryEntity.class));
    }

    @Test
    void should_handle_zero_quantity_in_bulk_upsert() {
        // Arrange
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(1);
        inventory.setQuantity(0);

        List<InventoryEntity> inventories = List.of(inventory);

        when(inventoryDao.saveAll(inventories)).thenAnswer(invocation -> {
            List<InventoryEntity> saved = invocation.getArgument(0);
            saved.get(0).setId(1);
            return saved;
        });

        // Act
        List<InventoryEntity> result = inventoryApi.bulkUpsert(inventories);

        // Assert
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getQuantity());
        verify(inventoryDao).saveAll(inventories);
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

        when(inventoryDao.findByProductIds(List.of(1, 2))).thenReturn(inventories);
        when(productApi.getByIds(List.of(1, 2))).thenReturn(List.of(product1, product2));
        when(clientApi.isClientEnabled(1)).thenReturn(true);
        when(clientApi.isClientEnabled(2)).thenReturn(true);

        // Act
        List<InventoryEntity> result = inventoryApi.getByProductIds(List.of(1, 2));

        // Assert
        assertEquals(2, result.size());
        verify(inventoryDao).findByProductIds(List.of(1, 2));
        verify(productApi).getByIds(List.of(1, 2));
        verify(clientApi).isClientEnabled(1);
        verify(clientApi).isClientEnabled(2);
    }
}
