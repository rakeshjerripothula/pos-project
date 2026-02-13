package com.increff.pos.api;

import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientEntity;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientApiTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientApi clientApi;

    @Test
    void should_create_client_when_valid_input() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setClientName("test client");

        when(clientDao.existsByClientName("test client")).thenReturn(false);
        when(clientDao.save(any(ClientEntity.class))).thenAnswer(invocation -> {
            ClientEntity saved = invocation.getArgument(0);
            saved.setId(1);
            return saved;
        });

        // Act
        ClientEntity result = clientApi.createClient(client);

        // Assert
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName());
        assertTrue(result.getEnabled());
        verify(clientDao).existsByClientName("test client");
        verify(clientDao).save(client);
    }

    @Test
    void should_throw_exception_when_creating_client_with_duplicate_name() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setClientName("existing client");

        when(clientDao.existsByClientName("existing client")).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientApi.createClient(client));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("Client already exists", exception.getMessage());
        verify(clientDao).existsByClientName("existing client");
        verify(clientDao, never()).save(any());
    }

    @Test
    void should_get_all_clients() {
        // Arrange
        List<ClientEntity> clients = List.of(new ClientEntity(), new ClientEntity());
        when(clientDao.selectAll()).thenReturn(clients);

        // Act
        List<ClientEntity> result = clientApi.getAll();

        // Assert
        assertEquals(2, result.size());
        verify(clientDao).selectAll();
    }

    @Test
    void should_get_client_by_id_when_exists() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setClientName("test client");

        when(clientDao.findById(1)).thenReturn(Optional.of(client));

        // Act
        ClientEntity result = clientApi.getById(1);

        // Assert
        assertEquals(1, result.getId());
        assertEquals("test client", result.getClientName());
        verify(clientDao).findById(1);
    }

    @Test
    void should_throw_exception_when_getting_client_by_id_not_found() {
        // Arrange
        when(clientDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientApi.getById(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Client not found: 1", exception.getMessage());
        verify(clientDao).findById(1);
    }

    @Test
    void should_update_client_when_valid_input() {
        // Arrange
        ClientEntity existingClient = new ClientEntity();
        existingClient.setId(1);
        existingClient.setClientName("old name");
        existingClient.setEnabled(true);

        ClientEntity updateData = new ClientEntity();
        updateData.setClientName("new name");

        when(clientDao.findById(1)).thenReturn(Optional.of(existingClient));
        when(clientDao.existsByClientNameAndIdNot("new name", 1)).thenReturn(false);
        when(clientDao.save(any(ClientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ClientEntity result = clientApi.updateClient(1, updateData);

        // Assert
        assertEquals("new name", result.getClientName());
        assertTrue(result.getEnabled());
        verify(clientDao).findById(1);
        verify(clientDao).existsByClientNameAndIdNot("new name", 1);
        verify(clientDao).save(existingClient);
    }

    @Test
    void should_throw_exception_when_updating_client_not_found() {
        // Arrange
        ClientEntity updateData = new ClientEntity();
        updateData.setClientName("new name");

        when(clientDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientApi.updateClient(1, updateData));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Client not found: 1", exception.getMessage());
        verify(clientDao).findById(1);
        verify(clientDao, never()).existsByClientNameAndIdNot(anyString(), anyInt());
        verify(clientDao, never()).save(any());
    }

    @Test
    void should_throw_exception_when_updating_client_with_duplicate_name() {
        // Arrange
        ClientEntity existingClient = new ClientEntity();
        existingClient.setId(1);
        existingClient.setClientName("old name");

        ClientEntity updateData = new ClientEntity();
        updateData.setClientName("existing name");

        when(clientDao.findById(1)).thenReturn(Optional.of(existingClient));
        when(clientDao.existsByClientNameAndIdNot("existing name", 1)).thenReturn(true);

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientApi.updateClient(1, updateData));
        assertEquals(ApiStatus.CONFLICT, exception.getStatus());
        assertEquals("Client already exists", exception.getMessage());
        verify(clientDao).findById(1);
        verify(clientDao).existsByClientNameAndIdNot("existing name", 1);
        verify(clientDao, never()).save(any());
    }

    @Test
    void should_set_client_enabled_status_when_different() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setClientName("test client");
        client.setEnabled(true);

        when(clientDao.findById(1)).thenReturn(Optional.of(client));
        when(clientDao.save(any(ClientEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ClientEntity result = clientApi.toggle(1, false);

        // Assert
        assertFalse(result.getEnabled());
        verify(clientDao).findById(1);
        verify(clientDao).save(any(ClientEntity.class));
    }

    @Test
    void should_not_change_client_enabled_status_when_same() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setClientName("test client");
        client.setEnabled(true);

        when(clientDao.findById(1)).thenReturn(Optional.of(client));

        // Act
        ClientEntity result = clientApi.toggle(1, true);

        // Assert
        assertTrue(result.getEnabled());
        verify(clientDao).findById(1);
    }

    @Test
    void should_throw_exception_when_toggling_client_not_found() {
        // Arrange
        when(clientDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientApi.toggle(1, true));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Client not found: 1", exception.getMessage());
        verify(clientDao).findById(1);
    }

    @Test
    void should_return_true_when_client_is_enabled() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setEnabled(true);

        when(clientDao.findById(1)).thenReturn(Optional.of(client));

        // Act
        Boolean result = clientApi.isClientEnabled(1);

        // Assert
        assertTrue(result);
        verify(clientDao).findById(1);
    }

    @Test
    void should_return_false_when_client_is_disabled() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setId(1);
        client.setEnabled(false);

        when(clientDao.findById(1)).thenReturn(Optional.of(client));

        // Act
        Boolean result = clientApi.isClientEnabled(1);

        // Assert
        assertFalse(result);
        verify(clientDao).findById(1);
    }

    @Test
    void should_throw_exception_when_checking_enabled_status_for_nonexistent_client() {
        // Arrange
        when(clientDao.findById(1)).thenReturn(Optional.empty());

        // Act & Assert
        ApiException exception = assertThrows(ApiException.class, () -> clientApi.isClientEnabled(1));
        assertEquals(ApiStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Client not found: 1", exception.getMessage());
        verify(clientDao).findById(1);
    }

    @Test
    void should_get_disabled_client_ids() {
        // Arrange
        List<Integer> clientIds = List.of(1, 2, 3);
        List<Integer> disabledIds = List.of(2, 3);

        when(clientDao.findDisabledClientIds(clientIds)).thenReturn(disabledIds);

        // Act
        List<Integer> result = clientApi.getDisabledClientIds(clientIds);

        // Assert
        assertEquals(disabledIds, result);
        verify(clientDao).findDisabledClientIds(clientIds);
    }

    @Test
    void should_get_disabled_client_ids_when_empty_list() {
        // Arrange
        List<Integer> clientIds = List.of();
        List<Integer> disabledIds = List.of();

        when(clientDao.findDisabledClientIds(clientIds)).thenReturn(disabledIds);

        // Act
        List<Integer> result = clientApi.getDisabledClientIds(clientIds);

        // Assert
        assertEquals(0, result.size());
        verify(clientDao).findDisabledClientIds(clientIds);
    }

    @Test
    void should_handle_data_integrity_violation_when_creating_client() {
        // Arrange
        ClientEntity client = new ClientEntity();
        client.setClientName("test client");

        when(clientDao.existsByClientName("test client")).thenReturn(false);
        when(clientDao.save(client)).thenThrow(new DataIntegrityViolationException("Constraint violation"));

        // Act & Assert
        assertThrows(DataIntegrityViolationException.class, () -> clientApi.createClient(client));
        verify(clientDao).existsByClientName("test client");
        verify(clientDao).save(client);
    }

    @Test
    void should_get_non_existent_client_ids() {
        // Arrange
        List<Integer> clientIds = List.of(1, 2, 3, 4);
        List<Integer> nonExistentIds = List.of(2, 4);

        when(clientDao.findNonExistentClientIds(clientIds)).thenReturn(nonExistentIds);

        // Act
        List<Integer> result = clientApi.getNonExistentClientIds(clientIds);

        // Assert
        assertEquals(nonExistentIds, result);
        verify(clientDao).findNonExistentClientIds(clientIds);
    }

    @Test
    void should_get_non_existent_client_ids_when_all_exist() {
        // Arrange
        List<Integer> clientIds = List.of(1, 2, 3);
        List<Integer> nonExistentIds = List.of();

        when(clientDao.findNonExistentClientIds(clientIds)).thenReturn(nonExistentIds);

        // Act
        List<Integer> result = clientApi.getNonExistentClientIds(clientIds);

        // Assert
        assertEquals(0, result.size());
        verify(clientDao).findNonExistentClientIds(clientIds);
    }
}
