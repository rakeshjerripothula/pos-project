package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    @Transactional(readOnly = true)
    public List<ClientEntity> getAll() {
        return clientDao.selectAll();
    }

    @Transactional(readOnly = true)
    public ClientEntity getById(Integer clientId) {
        return clientDao.selectById(clientId)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND, "Client not found: " + clientId, "clientId", "Client not found: " + clientId));
    }

    @Transactional(readOnly = true)
    public Page<ClientEntity> getClientsList(String clientName, Boolean enabled, Pageable pageable) {
        return clientDao.selectByFilters(clientName, enabled, pageable);
    }

    public ClientEntity createClient(ClientEntity client) {
        if (clientDao.selectByClientName(client.getClientName()).isPresent())
            throw new ApiException(ApiStatus.CONFLICT, "Client already exists", "clientName", "Client already exists");
        client.setEnabled(true);
        return clientDao.save(client);
    }

    public ClientEntity updateClient(Integer clientId, ClientEntity client) {
        ClientEntity existing = getById(clientId);
        if (clientDao.selectByClientNameExcludingId(client.getClientName(), clientId).isPresent())
            throw new ApiException(ApiStatus.CONFLICT, "Client already exists", "clientName", "Client already exists");
        existing.setClientName(client.getClientName());
        return clientDao.save(existing);
    }

    public ClientEntity toggle(Integer clientId, Boolean enabled) {
        ClientEntity client = getById(clientId);
        if (!client.getEnabled().equals(enabled)) {
            client.setEnabled(enabled);
            return clientDao.save(client);
        }
        return client;
    }

    @Transactional(readOnly = true)
    public void checkClientEnabled(Integer clientId) {
        Boolean enabled = getById(clientId).getEnabled();
        if (!enabled) {
            throw new ApiException(ApiStatus.FORBIDDEN, "Client is disabled", "clientId", "Client is disabled");
        }
    }

    @Transactional(readOnly = true)
    public List<Integer> getEnabledClientIds(List<Integer> clientIds, Boolean enabled) {
        return clientDao.selectIdsByIdInAndEnabled(clientIds, enabled);
    }

    @Transactional(readOnly = true)
    public List<Integer> getNonExistentClientIds(List<Integer> clientIds) {
        List<Integer> existingIds = clientDao.selectExistingIds(clientIds);
        Set<Integer> existingSet = new HashSet<>(existingIds);
        return clientIds.stream().filter(id -> !existingSet.contains(id)).toList();
    }

    @Transactional(readOnly = true)
    public Integer getClientIdByName(String clientName) {
        return clientDao.selectByClientName(clientName)
                .map(ClientEntity::getId)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND, "Client not found: " + clientName, "clientName", "Client not found: " + clientName));
    }
}
