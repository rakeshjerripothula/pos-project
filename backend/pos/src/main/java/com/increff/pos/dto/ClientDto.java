package com.increff.pos.dto;

import com.increff.pos.api.ClientApi;
import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.ClientSearchForm;
import com.increff.pos.model.form.ClientToggleForm;
import com.increff.pos.util.ConversionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ClientDto extends AbstractDto {

    @Autowired
    private ClientApi clientApi;

    @PreAuthorize("hasAnyRole('OPERATOR','SUPERVISOR')")
    public List<ClientData> getAll() {
        List<ClientEntity> entities = clientApi.getAll();

        return entities.stream().map(ConversionUtil::clientEntityToData).toList();
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public ClientData createClient(ClientForm form) {
        checkValid(form);
        
        ClientEntity entity = ConversionUtil.clientFormToEntity(form);
        ClientEntity saved = clientApi.createClient(entity);
        return ConversionUtil.clientEntityToData(saved);
    }

    @PreAuthorize("hasAnyRole('OPERATOR','SUPERVISOR')")
    public PagedResponse<ClientData> listClients(ClientSearchForm form) {
        checkValid(form);

        Pageable pageable = PageRequest.of(
                form.getPage(),
                form.getPageSize(),
                Sort.by("createdAt").descending()
        );

        Page<ClientEntity> page = clientApi.getClientsList(form.getClientName(), form.getEnabled(), pageable);

        List<ClientData> data = page.getContent()
                .stream()
                .map(ConversionUtil::clientEntityToData)
                .toList();

        return new PagedResponse<>(data, page.getTotalElements());
    }

    @PreAuthorize("hasAnyRole('SUPERVISOR')")
    public ClientData updateClient(Integer clientId, ClientForm form) {
        if (Objects.isNull(clientId)) {
            throw new ApiException(ApiStatus.BAD_REQUEST, "Client ID is required", "clientId", "Client ID is required");
        }
        
        checkValid(form);
        
        ClientEntity entity = ConversionUtil.clientFormToEntity(form);
        return ConversionUtil.clientEntityToData(clientApi.updateClient(clientId, entity));
    }

    @PreAuthorize("hasRole('SUPERVISOR')")
    public ClientData toggleClient(Integer id, ClientToggleForm form) {
        if (Objects.isNull(id)) {
            throw new ApiException(ApiStatus.BAD_REQUEST, "Client ID is required", "clientId", "Client ID is required");
        }
        
        checkValid(form);
        
        return ConversionUtil.clientEntityToData(clientApi.toggle(id, form.getEnabled()));
    }

}
