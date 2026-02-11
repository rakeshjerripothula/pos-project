package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ProductDaoIntegrationTest {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ClientDao clientDao;

    private static int barcodeCounter = 1;

    private ClientEntity createTestClient() {
        ClientEntity client = new ClientEntity();
        client.setClientName("test-client-" + UUID.randomUUID());
        client.setEnabled(true);
        return clientDao.save(client);
    }

    @Test
    void testInsert() {
        ClientEntity client = createTestClient();
        
        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(client.getId());
        product.setBarcode("BARCODE" + (barcodeCounter++));

        ProductEntity saved = productDao.save(product);

        assertNotNull(saved.getId());
        assertEquals("Test Product", saved.getProductName());
        assertEquals(new BigDecimal("99.99"), saved.getMrp());
        assertEquals(client.getId(), saved.getClientId());
        String expectedBarcode = "BARCODE";
        String actualBarcode = saved.getBarcode();
        assertTrue(actualBarcode.startsWith(expectedBarcode));
    }

    @Test
    void testSelectById() {
        ClientEntity client = createTestClient();
        
        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(client.getId());
        product.setBarcode("BARCODE" + (barcodeCounter++));
        ProductEntity saved = productDao.save(product);

        Optional<ProductEntity> found = productDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Test Product", found.get().getProductName());
    }

    @Test
    void testSelectAll() {
        ClientEntity client = createTestClient();
        
        ProductEntity product1 = new ProductEntity();
        product1.setProductName("Product 1");
        product1.setMrp(new BigDecimal("10.00"));
        product1.setClientId(client.getId());
        product1.setBarcode("BARCODE" + (barcodeCounter++));
        productDao.save(product1);

        ProductEntity product2 = new ProductEntity();
        product2.setProductName("Product 2");
        product2.setMrp(new BigDecimal("20.00"));
        product2.setClientId(client.getId());
        product2.setBarcode("BARCODE" + (barcodeCounter++));
        productDao.save(product2);

        List<ProductEntity> all = productDao.selectAll();

        assertEquals(2, all.size());
        assertTrue(all.get(0).getId() < all.get(1).getId());
    }

    @Test
    void testSelectByBarcode() {
        ClientEntity client = createTestClient();
        
        ProductEntity product = new ProductEntity();
        product.setProductName("Test Product");
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(client.getId());
        product.setBarcode("UNIQUE123");
        productDao.save(product);

        boolean exists = productDao.existsByBarcode("UNIQUE123");
        assertTrue(exists);

        boolean notExists = productDao.existsByBarcode("NOTEXIST");
        assertFalse(notExists);
    }

    @Test
    void testFindAllById() {
        ClientEntity client = createTestClient();
        
        ProductEntity product1 = new ProductEntity();
        product1.setProductName("Product 1");
        product1.setMrp(new BigDecimal("10.00"));
        product1.setClientId(client.getId());
        product1.setBarcode("BARCODE" + (barcodeCounter++));
        ProductEntity saved1 = productDao.save(product1);

        ProductEntity product2 = new ProductEntity();
        product2.setProductName("Product 2");
        product2.setMrp(new BigDecimal("20.00"));
        product2.setClientId(client.getId());
        product2.setBarcode("BARCODE" + (barcodeCounter++));
        ProductEntity saved2 = productDao.save(product2);

        List<ProductEntity> found = productDao.findAllById(List.of(saved1.getId(), saved2.getId()));

        assertEquals(2, found.size());
    }

}
