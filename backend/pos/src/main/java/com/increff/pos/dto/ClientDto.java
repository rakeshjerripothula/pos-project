package com.increff.pos.dto;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.api.ClientApi;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.increff.pos.util.Utils.normalize;

@Service
public class ClientDto extends AbstractDto {

    @Autowired
    private ClientApi clientApi;

    public ClientData createClient(ClientForm form) {
        checkValid(form);
        
        ClientEntity entity = new ClientEntity();
        entity.setClientName(normalize(form.getClientName()));
        ClientEntity saved = clientApi.createClient(entity);
        return ConverttoClientData(saved);
    }

    public ClientData updateClient(Integer clientId, ClientForm form) {
        if (Objects.isNull(clientId)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Client ID is required", "clientId", "Client ID is required");
        }
        
        checkValid(form);
        
        ClientEntity entity = new ClientEntity();
        entity.setClientName(normalize(form.getClientName()));
        return ConverttoClientData(clientApi.updateClient(clientId, entity));
    }

    public ClientData toggleClient(Integer id) {
        return ConverttoClientData(clientApi.toggle(id));
    }

    public List<ClientData> getAllClients() {
        return clientApi.getAllClients()
                .stream()
                .map(this::ConverttoClientData)
                .collect(Collectors.toList());
    }

    public List<ClientData> bulkCreate(List<ClientForm> forms) {
        checkValidList(forms);

        List<ClientEntity> entities = forms.stream()
                .map(form -> {
                    ClientEntity e = new ClientEntity();
                    e.setClientName(normalize(form.getClientName()));
                    return e;
                })
                .toList();

        List<ClientEntity> saved =
                clientApi.bulkCreate(entities);

        return saved.stream()
                .map(this::ConverttoClientData)
                .toList();
    }

    private ClientData ConverttoClientData(ClientEntity entity) {
        ClientData data = new ClientData();
        data.setId(entity.getId());
        data.setClientName(entity.getClientName());
        data.setEnabled(entity.getEnabled());
        return data;
    }

}
