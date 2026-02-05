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

import java.util.List;

@Service
@Transactional
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    public ClientEntity createClient(ClientEntity client) {

        if (clientDao.existsByClientName(client.getClientName())) {
            throw new ApiException(ApiStatus.CONFLICT, "Client already exists", "clientName", "Client already exists");
        }

        client.setEnabled(true);
        return clientDao.save(client);

    }

    @Transactional(readOnly = true)
    public List<ClientEntity> getAll() {
        return clientDao.selectAll();
    }

    public ClientEntity getById(Integer clientId){
        return clientDao.findById(clientId).orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND,
                        "Client not found: " + clientId, "clientId", "Client not found: " + clientId)
                        );
    }

    public ClientEntity updateClient(Integer clientId, ClientEntity client) {

        ClientEntity existing = getClientOrThrow(clientId);

        if (clientDao.existsByClientNameAndIdNot(client.getClientName(), clientId)) {
            throw new ApiException(ApiStatus.CONFLICT, "Client already exists", "clientName", "Client already exists");
        }

        existing.setClientName(client.getClientName());
        return clientDao.save(existing);
    }

    public ClientEntity toggle(Integer clientId, Boolean enabled) {

        ClientEntity client = getClientOrThrow(clientId);

        if (!client.getEnabled().equals(enabled)) {
            client.setEnabled(enabled);
            client = clientDao.save(client);
        }

        return client;
    }

    public Page<ClientEntity> getAllClients(Pageable pageable) {
        return clientDao.findAll(pageable);
    }

    public Boolean isClientEnabled(Integer clientId) {
        return getClientOrThrow(clientId).getEnabled();
    }

    public List<Integer> getDisabledClientIds(List<Integer> clientIds){
        return clientDao.findDisabledClientIds(clientIds);
    }

    private ClientEntity getClientOrThrow(Integer clientId) {
        return clientDao.findById(clientId).orElseThrow(() -> new ApiException(
                ApiStatus.NOT_FOUND, "Client not found: " + clientId));
    }

}
