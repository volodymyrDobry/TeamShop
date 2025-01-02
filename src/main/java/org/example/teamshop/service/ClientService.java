package org.example.teamshop.service;

import lombok.RequiredArgsConstructor;
import org.example.teamshop.dto.ClientDTO;
import org.example.teamshop.mapper.ClientMapper;
import org.example.teamshop.model.Client;
import org.example.teamshop.repository.ClientRepository;
import org.example.teamshop.request.CreateClientRequest;
import org.example.teamshop.request.UpdateClientRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ClientService implements IClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper mapper;

    @Override
    public ClientDTO findClientById(Long id) {
        return clientRepository.findById(id)
                .map(mapper::clientToClientDTO)
                .orElseThrow(() -> new RuntimeException("Client not found"));
    }

    @Override
    public ClientDTO addClient(CreateClientRequest createClientRequest) {
        // rewrite try-catch
        try {
            Client newClient = mapper.createClientRequestToClient(createClientRequest);
            clientRepository.save(newClient);
            return mapper.clientToClientDTO(newClient);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new RuntimeException("Конфлікт: клієнт був змінений або видалений іншим користувачем.");
        }
    }


    @Override
    public ClientDTO updateClient(UpdateClientRequest updateClientRequest, Long id) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
        mapper.updateClientRequestToClient(updateClientRequest, client);
        clientRepository.save(client);
        return mapper.clientToClientDTO(client);
    }

    @Override
    public void deleteClient(Long id) {
        Client client = clientRepository.findById(id).orElseThrow(() -> new RuntimeException("Client not found"));
        clientRepository.delete(client);
    }

}