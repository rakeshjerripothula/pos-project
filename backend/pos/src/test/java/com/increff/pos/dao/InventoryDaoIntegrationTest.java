package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class InventoryDaoIntegrationTest {

    @Autowired
    private InventoryDao inventoryDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ClientDao clientDao;

    private static int barcodeCounter = 1;
    private static int clientCounter = 1;

    private ProductEntity createTestProduct() {
        ClientEntity client = new ClientEntity();
        client.setClientName("Test Client " + (clientCounter++));
        client.setEnabled(true);
        client = clientDao.save(client);

        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(client.getId());
        product.setBarcode("BARCODE" + (barcodeCounter++));
        return productDao.save(product);
    }

    @Test
    void testInsert() {
        ProductEntity product = createTestProduct();
        
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(product.getId());
        inventory.setQuantity(100);

        InventoryEntity saved = inventoryDao.save(inventory);

        assertNotNull(saved.getId());
        assertEquals(product.getId(), saved.getProductId());
        assertEquals(100, saved.getQuantity());
    }

    @Test
    void testSelectByProductId() {
        ProductEntity product = createTestProduct();
        
        InventoryEntity inventory = new InventoryEntity();
        inventory.setProductId(product.getId());
        inventory.setQuantity(50);
        inventoryDao.save(inventory);

        Optional<InventoryEntity> found = inventoryDao.selectByProductId(product.getId());

        assertTrue(found.isPresent());
        assertEquals(product.getId(), found.get().getProductId());
        assertEquals(50, found.get().getQuantity());
    }

    @Test
    void testSelectByProductIds() {
        ProductEntity product1 = createTestProduct();
        ProductEntity product2 = createTestProduct();
        
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(product1.getId());
        inventory1.setQuantity(10);
        inventoryDao.save(inventory1);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(product2.getId());
        inventory2.setQuantity(20);
        inventoryDao.save(inventory2);

        List<InventoryEntity> found = inventoryDao.selectByProductIds(List.of(product1.getId(), product2.getId()));

        assertEquals(2, found.size());
    }

    @Test
    void testFindAllForEnabledClients() {
        ClientEntity enabledClient = new ClientEntity();
        enabledClient.setClientName("Enabled Client " + System.currentTimeMillis());
        enabledClient.setEnabled(true);
        enabledClient = clientDao.save(enabledClient);

        ClientEntity disabledClient = new ClientEntity();
        disabledClient.setClientName("Disabled Client " + System.currentTimeMillis());
        disabledClient.setEnabled(false);
        disabledClient = clientDao.save(disabledClient);

        ProductEntity enabledProduct = new ProductEntity();
        enabledProduct.setProductName("Enabled Product");
        enabledProduct.setMrp(new BigDecimal("10.00"));
        enabledProduct.setClientId(enabledClient.getId());
        enabledProduct.setBarcode("ENABLED" + (barcodeCounter++));
        enabledProduct = productDao.save(enabledProduct);

        ProductEntity disabledProduct = new ProductEntity();
        disabledProduct.setProductName("Disabled Product");
        disabledProduct.setMrp(new BigDecimal("20.00"));
        disabledProduct.setClientId(disabledClient.getId());
        disabledProduct.setBarcode("DISABLED" + (barcodeCounter++));
        disabledProduct = productDao.save(disabledProduct);

        InventoryEntity enabledInventory = new InventoryEntity();
        enabledInventory.setProductId(enabledProduct.getId());
        enabledInventory.setQuantity(100);
        inventoryDao.save(enabledInventory);

        InventoryEntity disabledInventory = new InventoryEntity();
        disabledInventory.setProductId(disabledProduct.getId());
        disabledInventory.setQuantity(200);
        inventoryDao.save(disabledInventory);

        List<InventoryEntity> found = inventoryDao.selectAllForEnabledClients();

        assertEquals(1, found.size());
        assertEquals(enabledProduct.getId(), found.get(0).getProductId());
    }

    @Test
    void testSaveAll() {
        ProductEntity product1 = createTestProduct();
        ProductEntity product2 = createTestProduct();
        
        InventoryEntity inventory1 = new InventoryEntity();
        inventory1.setProductId(product1.getId());
        inventory1.setQuantity(10);

        InventoryEntity inventory2 = new InventoryEntity();
        inventory2.setProductId(product2.getId());
        inventory2.setQuantity(20);

        List<InventoryEntity> inventories = List.of(inventory1, inventory2);
        List<InventoryEntity> saved = inventoryDao.saveAll(inventories);

        assertEquals(2, saved.size());
        assertNotNull(saved.get(0).getId());
        assertNotNull(saved.get(1).getId());
    }
}
