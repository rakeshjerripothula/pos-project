package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private UserApi userApi;

    public ClientEntity createClient(ClientEntity client) {

        if (clientDao.existsByClientName(client.getClientName())) {
            throw new ApiException(ApiStatus.CONFLICT, "Client already exists", "clientName", "Client already exists");
        }

        client.setEnabled(true);
        return clientDao.save(client);

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

    public ClientEntity toggle(Integer clientId) {

        ClientEntity client = getClientOrThrow(clientId);

        client.toggleEnabled();

        return client;
    }

    public List<ClientEntity> bulkCreate(List<ClientEntity> clients) {

        for (ClientEntity client : clients) {
            if (clientDao.existsByClientName(client.getClientName())) {
                throw new ApiException(
                    ApiStatus.CONFLICT,
                    "Client already exists: " + client.getClientName(),
                    "clientName",
                    "Client already exists: " + client.getClientName()
                );
            }
            client.setEnabled(true);
        }

        return clientDao.saveAll(clients);
    }

    public List<ClientEntity> getAllClients() {
        return clientDao.findAll();
    }

    public Boolean isClientEnabled(Integer clientId) {
        ClientEntity client = clientDao.findById(clientId)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND,
                        "Client not found: " + clientId, "clientId", "Client not found: " + clientId)
                );
        return client.getEnabled();
    }

    private ClientEntity getClientOrThrow(Integer clientId) {
        return clientDao.findById(clientId).orElseThrow(() -> new ApiException(
                ApiStatus.NOT_FOUND, "Client not found: " + clientId));
    }

}
