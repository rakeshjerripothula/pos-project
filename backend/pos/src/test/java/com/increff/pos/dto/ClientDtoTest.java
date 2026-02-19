package com.increff.pos.dto;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.data.ClientData;
import com.increff.pos.model.data.PagedResponse;
import com.increff.pos.model.form.ClientForm;
import com.increff.pos.model.form.ClientSearchForm;
import com.increff.pos.model.form.ClientToggleForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ClientDtoTest {

    @Mock
    private com.increff.pos.api.ClientApi clientApi;

    @InjectMocks
    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createClient_validForm_success() {
        // Arrange
        ClientForm form = new ClientForm();
        form.setClientName("Test Client");
        
        com.increff.pos.entity.ClientEntity savedEntity = new com.increff.pos.entity.ClientEntity();
        savedEntity.setId(1);
        savedEntity.setClientName("test client");
        savedEntity.setEnabled(true);
        
        when(clientApi.createClient(any())).thenReturn(savedEntity);

        // Act
        ClientData result = clientDto.createClient(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName());
        assertTrue(result.getEnabled());
        verify(clientApi, times(1)).createClient(any());
    }

    @Test
    void createClient_invalidName_throwsException() {
        // Arrange
        ClientForm form = new ClientForm();
        form.setClientName(""); // Empty name

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientDto.createClient(form));
        assertEquals("BAD_REQUEST", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().get(0).getMessage().contains("Client name cannot be empty"));
        verify(clientApi, never()).createClient(any());
    }

    @Test
    void createClient_nullForm_throwsException() {
        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientDto.createClient(null));
        assertEquals("Input cannot be null", exception.getMessage());
        verify(clientApi, never()).createClient(any());
    }

    @Test
    void updateClient_validForm_success() {
        // Arrange
        Integer clientId = 1;
        ClientForm form = new ClientForm();
        form.setClientName("Updated Client");
        
        com.increff.pos.entity.ClientEntity updatedEntity = new com.increff.pos.entity.ClientEntity();
        updatedEntity.setId(clientId);
        updatedEntity.setClientName("updated client");
        updatedEntity.setEnabled(true);
        
        when(clientApi.updateClient(eq(clientId), any())).thenReturn(updatedEntity);

        // Act
        ClientData result = clientDto.updateClient(clientId, form);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getId());
        assertEquals("updated client", result.getClientName());
        verify(clientApi, times(1)).updateClient(eq(clientId), any());
    }

    @Test
    void updateClient_invalidClientId_throwsException() {
        // Arrange
        ClientForm form = new ClientForm();
        form.setClientName("Test Client");

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientDto.updateClient(null, form));
        assertEquals("BAD_REQUEST", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Client ID is required"));
        verify(clientApi, never()).updateClient(any(), any());
    }

    @Test
    void updateClient_invalidName_throwsException() {
        // Arrange
        Integer clientId = 1;
        ClientForm form = new ClientForm();
        form.setClientName(""); // Empty name

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientDto.updateClient(clientId, form));
        assertEquals("BAD_REQUEST", exception.getStatus().name());
        assertTrue(exception.hasErrors());
        assertTrue(exception.getErrors().get(0).getMessage().contains("Client name cannot be empty"));
        verify(clientApi, never()).updateClient(any(), any());
    }

    @Test
    void toggleClient_validForm_success() {
        // Arrange
        Integer clientId = 1;
        ClientToggleForm form = new ClientToggleForm();
        form.setEnabled(false);
        
        com.increff.pos.entity.ClientEntity toggledEntity = new com.increff.pos.entity.ClientEntity();
        toggledEntity.setId(clientId);
        toggledEntity.setClientName("test client");
        toggledEntity.setEnabled(false);
        
        when(clientApi.toggle(eq(clientId), eq(false))).thenReturn(toggledEntity);

        // Act
        ClientData result = clientDto.toggleClient(clientId, form);

        // Assert
        assertNotNull(result);
        assertEquals(clientId, result.getId());
        assertFalse(result.getEnabled());
        verify(clientApi, times(1)).toggle(eq(clientId), eq(false));
    }

    @Test
    void toggleClient_invalidId_throwsException() {
        // Arrange
        ClientToggleForm form = new ClientToggleForm();
        form.setEnabled(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientDto.toggleClient(null, form));
        assertEquals("BAD_REQUEST", exception.getStatus().name());
        assertTrue(exception.getMessage().contains("Client ID is required"));
        verify(clientApi, never()).toggle(any(), any());
    }

    @Test
    void getAll_success() {
        // Arrange
        List<ClientEntity> entities = Arrays.asList(
            createClientEntity(1, "Client 1", true),
            createClientEntity(2, "Client 2", false)
        );
        when(clientApi.getAll()).thenReturn(entities);

        // Act
        List<ClientData> result = clientDto.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Client 1", result.get(0).getClientName());
        assertTrue(result.get(0).getEnabled());
        assertEquals("Client 2", result.get(1).getClientName());
        assertFalse(result.get(1).getEnabled());
        verify(clientApi, times(1)).getAll();
    }

    @Test
    void testListClients() {
        // Arrange
        ClientSearchForm form = new ClientSearchForm();
        form.setPage(0);
        form.setPageSize(10);
        form.setClientName("Test Client");
        form.setEnabled(true);

        ClientEntity entity = createClientEntity(1, "Test Client", true);
        Page<ClientEntity> page = new PageImpl<>(List.of(entity));

        when(clientApi.getClientsList(any(), any(), any())).thenReturn(page);

        // Act
        PagedResponse<ClientData> result = clientDto.listClients(form);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotal());
        assertEquals("Test Client", result.getData().get(0).getClientName());
        assertTrue(result.getData().get(0).getEnabled());
        verify(clientApi).getClientsList(any(), any(), any());
    }

    private com.increff.pos.entity.ClientEntity createClientEntity(Integer id, String name, Boolean enabled) {
        com.increff.pos.entity.ClientEntity entity = new com.increff.pos.entity.ClientEntity();
        entity.setId(id);
        entity.setClientName(name);
        entity.setEnabled(enabled);
        return entity;
    }
}
