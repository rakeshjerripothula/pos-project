package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ClientDaoIntegrationTest {

    @Autowired
    private ClientDao clientDao;

    @Test
    void testInsert() {
        ClientEntity client = new ClientEntity();
        client.setClientName("Test Client");
        client.setEnabled(true);

        ClientEntity saved = clientDao.save(client);
        
        assertNotNull(saved.getId());
        assertEquals("Test Client", saved.getClientName());
        assertTrue(saved.getEnabled());
    }

    @Test
    void testSelectById() {
        ClientEntity client = new ClientEntity();
        client.setClientName("Test Client");
        client.setEnabled(true);
        ClientEntity saved = clientDao.save(client);

        Optional<ClientEntity> found = clientDao.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Test Client", found.get().getClientName());
    }

    @Test
    void testSelectAll() {
        ClientEntity client1 = new ClientEntity();
        client1.setClientName("Client 1");
        client1.setEnabled(true);
        clientDao.save(client1);

        ClientEntity client2 = new ClientEntity();
        client2.setClientName("Client 2");
        client2.setEnabled(false);
        clientDao.save(client2);

        List<ClientEntity> all = clientDao.selectAll();

        assertEquals(2, all.size());
        assertTrue(all.get(0).getId() < all.get(1).getId());
    }

    @Test
    void testSelectByClientName() {
        ClientEntity client = new ClientEntity();
        client.setClientName("Unique Client");
        client.setEnabled(true);
        clientDao.save(client);

        boolean exists = clientDao.existsByClientName("Unique Client");
        assertTrue(exists);

        boolean notExists = clientDao.existsByClientName("Non Existent");
        assertFalse(notExists);
    }

}
