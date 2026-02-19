package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.model.domain.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderDaoIntegrationTest {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ClientDao clientDao;

    private static int clientCounter = 1;

    private ClientEntity createTestClient() {
        ClientEntity client = new ClientEntity();
        client.setClientName("Test Client " + (clientCounter++));
        client.setEnabled(true);
        return clientDao.save(client);
    }

    @Test
    void testInsert() {
        ClientEntity client = createTestClient();
        
        OrderEntity order = new OrderEntity();
        order.setClientId(client.getId());
        order.setStatus(OrderStatus.CREATED);

        OrderEntity saved = orderDao.save(order);

        assertNotNull(saved.getId());
        assertEquals(client.getId(), saved.getClientId());
        assertEquals(OrderStatus.CREATED, saved.getStatus());
    }

    @Test
    void testSelectById() {
        ClientEntity client = createTestClient();
        
        OrderEntity order = new OrderEntity();
        order.setClientId(client.getId());
        order.setStatus(OrderStatus.INVOICED);
        OrderEntity saved = orderDao.save(order);

        Optional<OrderEntity> found = orderDao.selectById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals(client.getId(), found.get().getClientId());
        assertEquals(OrderStatus.INVOICED, found.get().getStatus());
    }

    @Test
    void testSearchAll() {
        ClientEntity client = createTestClient();
        
        OrderEntity order1 = new OrderEntity();
        order1.setClientId(client.getId());
        order1.setStatus(OrderStatus.CREATED);
        orderDao.save(order1);

        OrderEntity order2 = new OrderEntity();
        order2.setClientId(client.getId());
        order2.setStatus(OrderStatus.INVOICED);
        orderDao.save(order2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> page = orderDao.selectByFilters(null, null, null, null, pageable);

        assertEquals(2, page.getTotalElements());
        assertEquals(2, page.getContent().size());
    }

    @Test
    void testSearchByStatus() {
        ClientEntity client = createTestClient();
        
        OrderEntity createdOrder = new OrderEntity();
        createdOrder.setClientId(client.getId());
        createdOrder.setStatus(OrderStatus.CREATED);
        orderDao.save(createdOrder);

        OrderEntity invoicedOrder = new OrderEntity();
        invoicedOrder.setClientId(client.getId());
        invoicedOrder.setStatus(OrderStatus.INVOICED);
        orderDao.save(invoicedOrder);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> page = orderDao.selectByFilters(OrderStatus.CREATED, null, null, null, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(OrderStatus.CREATED, page.getContent().get(0).getStatus());
    }

    @Test
    void testSearchByClientId() {
        ClientEntity client1 = createTestClient();
        ClientEntity client2 = createTestClient();
        client2.setClientName("Another Client " + System.currentTimeMillis());
        clientDao.save(client2);
        
        OrderEntity order1 = new OrderEntity();
        order1.setClientId(client1.getId());
        order1.setStatus(OrderStatus.CREATED);
        orderDao.save(order1);

        OrderEntity order2 = new OrderEntity();
        order2.setClientId(client2.getId());
        order2.setStatus(OrderStatus.CREATED);
        orderDao.save(order2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> page = orderDao.selectByFilters(null, client1.getId(), null, null, pageable);

        assertEquals(1, page.getTotalElements());
        assertEquals(client1.getId(), page.getContent().get(0).getClientId());
    }

    @Test
    void testSearchByDateRange() {
        ClientEntity client = createTestClient();
        
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime yesterday = now.minusDays(1);
        ZonedDateTime tomorrow = now.plusDays(1);
        
        OrderEntity order = new OrderEntity();
        order.setClientId(client.getId());
        order.setStatus(OrderStatus.CREATED);
        orderDao.save(order);

        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderEntity> page = orderDao.selectByFilters(null, null, yesterday, tomorrow, pageable);

        assertEquals(1, page.getTotalElements());
    }
}
