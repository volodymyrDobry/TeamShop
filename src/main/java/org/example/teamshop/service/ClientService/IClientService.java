package org.example.teamshop.service.ClientService;

import org.example.teamshop.dto.ClientDTO;
import org.example.teamshop.request.CreateClientRequest;
import org.example.teamshop.request.UpdateClientRequest;
import org.example.teamshop.securityModel.SecurityClient;

public interface IClientService {

    ClientDTO findClientById(Long id);

    ClientDTO addClient(CreateClientRequest createClientRequest);

    ClientDTO updateClient(UpdateClientRequest updateClientRequest, Long id);

    void deleteClient(Long id);

    boolean clientAccess(SecurityClient securityClient, Long requestId);
}
