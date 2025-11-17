package org.example.teamshop.service.ClientService;

import jakarta.persistence.EntityNotFoundException;
import org.example.teamshop.Exception.AlreadyExistingResourceException;
import org.example.teamshop.Exception.FailedOperationException;
import org.example.teamshop.dto.ClientDTO;
import org.example.teamshop.mapper.ClientMapper;
import org.example.teamshop.model.Client;
import org.example.teamshop.repository.ClientRepository;
import org.example.teamshop.request.CreateClientRequest;
import org.example.teamshop.request.UpdateClientRequest;
import org.example.teamshop.service.CartService.CartService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ClientServiceTest {

    @InjectMocks
    private ClientService clientService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @Mock
    private CartService cartService;

    @Test
    public void testFindClientById_ClientExist() {
        Long clientId = 1L;
        Client client = new Client(1L, "John", "12345", "email@gmail.com", 1L, 1);
        ClientDTO clientDTO = new ClientDTO(1L, "John", "12345", "email@gmail.com", 1L, 1);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.clientToClientDTO(client)).thenReturn(clientDTO);

        ClientDTO result = clientService.findClientById(clientId);

        assertNotNull(result);
        assertEquals(clientId, result.getId());
        assertEquals("John", result.getName());
        assertEquals("12345", result.getPassword());
        assertEquals("email@gmail.com", result.getEmail());
        assertEquals(1L, result.getCartId());
        assertEquals(1, result.getOrderId());

        verify(clientRepository, times(1)).findById(clientId);
        when(clientMapper.clientToClientDTO(any(Client.class))).thenReturn(clientDTO);
    }

    @Test
    public void testFindClientById_ShouldThrowException_WhenClientNotFound() {
        Long clientId = 2L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                clientService.findClientById(clientId));

        assertEquals("Client with ID " + clientId + " not found", exception.getMessage());

        verify(clientRepository, times(1)).findById(clientId);
        verify(clientMapper, never()).clientToClientDTO(any(Client.class));
    }

    @Test
    public void testAddClient_ShouldReturnClientDTO_WhenClientIsAddedSuccessfully() {
        CreateClientRequest createClientRequest = new CreateClientRequest("John Doe", "StrongP@ss1", "johndoe@example.com");
        Client newClient = new Client(1L, "John Doe", "StrongP@ss1", "johndoe@example.com", 1L, 1);
        ClientDTO clientDTO = new ClientDTO(1L, "John Doe", "StrongP@ss1", "johndoe@example.com", 1L, 1);

        when(clientRepository.existsByEmail(createClientRequest.getEmail())).thenReturn(false);
        when(clientMapper.createClientRequestToClient(createClientRequest)).thenReturn(newClient);
        when(cartService.createNewCart(newClient.getId())).thenReturn(1L);
        when(clientRepository.save(newClient)).thenReturn(newClient);
        when(clientMapper.clientToClientDTO(newClient)).thenReturn(clientDTO);

        ClientDTO result = clientService.addClient(createClientRequest);

        assertNotNull(result);
        assertEquals(newClient.getId(), result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("StrongP@ss1", result.getPassword());
        assertEquals("johndoe@example.com", result.getEmail());
        assertEquals(1L, result.getCartId());
        assertEquals(1, result.getOrderId());

        verify(clientRepository, times(1)).existsByEmail(createClientRequest.getEmail());
        verify(clientRepository, times(2)).save(newClient);
        verify(cartService, times(1)).createNewCart(newClient.getId());
        verify(clientMapper, times(1)).createClientRequestToClient(createClientRequest);
        verify(clientMapper, times(1)).clientToClientDTO(newClient);
    }

    @Test
    public void testAddClient_ShouldThrowException_WhenClientAlreadyExists() {
        CreateClientRequest createClientRequest = new CreateClientRequest("John Doe", "StrongP@ss1", "johndoe@example.com");

        when(clientRepository.existsByEmail(createClientRequest.getEmail())).thenReturn(true);

        AlreadyExistingResourceException alreadyExistingResourceException = assertThrows(AlreadyExistingResourceException.class, () ->
                clientService.addClient(createClientRequest));

        assertEquals("A client with this email already exists", alreadyExistingResourceException.getMessage());

        verify(clientRepository, times(1)).existsByEmail(createClientRequest.getEmail());
    }

    @Test
    public void testAddClient_ShouldThrowException_WhenOptimisticLockingFails() {
        CreateClientRequest createClientRequest = new CreateClientRequest("John Doe", "StrongP@ss1", "johndoe@example.com");
        Client newClient = new Client(1L, "John Doe", "StrongP@ss1", "johndoe@example.com", 1L, 1);

        when(clientRepository.existsByEmail(createClientRequest.getEmail())).thenReturn(false);
        when(clientMapper.createClientRequestToClient(createClientRequest)).thenReturn(newClient);
        when(clientRepository.save(newClient)).thenThrow(new ObjectOptimisticLockingFailureException(Client.class, 1L));

        FailedOperationException failedOperationException = assertThrows(FailedOperationException.class, () ->
                clientService.addClient(createClientRequest));

        assertEquals("Conflict! Bad request", failedOperationException.getMessage());

        verify(clientRepository, times(1)).existsByEmail(createClientRequest.getEmail());
        verify(clientRepository, times(1)).save(newClient);
        verify(cartService, never()).createNewCart(newClient.getId());
    }

    @Test
    public void testUpdateClient_ShouldReturnClientDTO_WhenClientIsUpdatedSuccessfully() {
        Long clientId = 1L;
        UpdateClientRequest updateClientRequest = new UpdateClientRequest("newEmail@gmail.com");

        Client existingClient = new Client(1L, "John Doe", "StrongP@ss1", "johndoe@example.com", 1L, 1);
        ClientDTO updatedClientDTO = new ClientDTO(1L, "John Doe", "StrongP@ss1", "newEmail@gmail.com", 1L, 1);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));
        doAnswer(invocation -> {
            UpdateClientRequest request = invocation.getArgument(0);
            Client clientToUpdate = invocation.getArgument(1);

            if (request.getEmail() != null) {
                clientToUpdate.setEmail(request.getEmail());
            }

            return null;
        }).when(clientMapper).updateClientRequestToClient(updateClientRequest, existingClient);

        when(clientRepository.save(existingClient)).thenReturn(existingClient);
        when(clientMapper.clientToClientDTO(existingClient)).thenReturn(updatedClientDTO);

        ClientDTO result = clientService.updateClient(updateClientRequest, clientId);

        assertNotNull(result);
        assertEquals(existingClient.getId(), result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("StrongP@ss1", result.getPassword());
        assertEquals("newEmail@gmail.com", result.getEmail());
        assertEquals(1L, result.getCartId());
        assertEquals(1, result.getOrderId());

        verify(clientRepository, times(1)).findById(clientId);
        verify(clientMapper, times(1)).updateClientRequestToClient(updateClientRequest, existingClient);
        verify(clientRepository, times(1)).save(existingClient);
        verify(clientMapper, times(1)).clientToClientDTO(existingClient);
    }

    @Test
    public void testUpdateClient_ShouldThrowException_WhenClientDoesNotExist() {
        Long clientId = 2L;
        UpdateClientRequest updateClientRequest = new UpdateClientRequest("newEmail@gmail.com");

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                clientService.updateClient(updateClientRequest, clientId));

        assertEquals("Client not found", exception.getMessage());

        verify(clientRepository, times(1)).findById(clientId);
        verify(clientMapper, never()).updateClientRequestToClient(any(), any());
        verify(clientRepository, never()).save(any());
    }

    @Test
    public void testDeleteClient_WhenClientIsDeletedSuccessfully() {
        Long clientId = 1L;
        Client existingClient = new Client(1L, "John Doe", "StrongP@ss1", "johndoe@example.com", 1L, 1);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(existingClient));

        clientService.deleteClient(clientId);

        verify(clientRepository, times(1)).findById(clientId);
        verify(cartService, times(1)).deleteCart(existingClient.getId());
        verify(clientRepository, times(1)).delete(existingClient);
    }

    @Test
    public void testDeleteClient_ShouldThrowException_WhenClientDoesNotExist() {
        Long clientId = 2L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                clientService.deleteClient(clientId));

        assertEquals("Client not found", exception.getMessage());

        verify(clientRepository, times(1)).findById(clientId);
        verify(cartService, never()).deleteCart(anyLong());
        verify(clientRepository, never()).delete(any());
    }

}
