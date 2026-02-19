package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.model.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderItemDaoIntegrationTest {

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ClientDao clientDao;

    private ClientEntity createTestClient() {
        ClientEntity client = new ClientEntity();
        client.setClientName("test-client-" + UUID.randomUUID());
        client.setEnabled(true);
        return clientDao.save(client);
    }

    private OrderEntity createTestOrder() {
        ClientEntity client = new ClientEntity();
        client.setClientName("Test Client " + System.currentTimeMillis());
        client.setEnabled(true);
        client = clientDao.save(client);

        OrderEntity order = new OrderEntity();
        order.setClientId(client.getId());
        order.setStatus(OrderStatus.CREATED);
        return orderDao.save(order);
    }

    private ProductEntity createTestProduct() {
        ClientEntity client = createTestClient();

        ProductEntity product = new ProductEntity();
        product.setProductName("product-" + UUID.randomUUID());
        product.setMrp(new BigDecimal("99.99"));
        product.setClientId(client.getId());
        product.setBarcode("barcode-" + UUID.randomUUID());

        return productDao.save(product);
    }

    @Test
    void testInsertAll() {
        OrderEntity order = createTestOrder();
        ProductEntity product1 = createTestProduct();
        ProductEntity product2 = createTestProduct();

        OrderItemEntity item1 = new OrderItemEntity();
        item1.setOrderId(order.getId());
        item1.setProductId(product1.getId());
        item1.setQuantity(2);
        item1.setSellingPrice(new BigDecimal("89.99"));

        OrderItemEntity item2 = new OrderItemEntity();
        item2.setOrderId(order.getId());
        item2.setProductId(product2.getId());
        item2.setQuantity(1);
        item2.setSellingPrice(new BigDecimal("79.99"));

        orderItemDao.saveAll(List.of(item1, item2));

        List<OrderItemEntity> found = orderItemDao.selectByOrderId(order.getId());
        assertEquals(2, found.size());
    }

    @Test
    void testSelectByOrderId() {
        OrderEntity order = createTestOrder();
        ProductEntity product = createTestProduct();

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setQuantity(5);
        item.setSellingPrice(new BigDecimal("99.99"));

        orderItemDao.saveAll(List.of(item));

        List<OrderItemEntity> found = orderItemDao.selectByOrderId(order.getId());

        assertEquals(1, found.size());
        assertEquals(order.getId(), found.get(0).getOrderId());
        assertEquals(product.getId(), found.get(0).getProductId());
        assertEquals(5, found.get(0).getQuantity());
        assertEquals(new BigDecimal("99.99"), found.get(0).getSellingPrice());
    }

    @Test
    void testSelectByOrderIdEmpty() {
        List<OrderItemEntity> found = orderItemDao.selectByOrderId(999);
        assertTrue(found.isEmpty());
    }
}
