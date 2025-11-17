package org.example.teamshop.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.teamshop.model.Client;
import org.example.teamshop.repository.CartRepository;
import org.example.teamshop.repository.ClientRepository;
import org.example.teamshop.request.UpdateClientRequest;
import org.example.teamshop.securityModel.SecurityClient;
import org.example.teamshop.service.CartService.CartService;
import org.example.teamshop.service.SecurityServices.CustomUserDetailsService.CustomUserDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ClientControllerTest {

    @Value("${api.path}")
    private String apiPath;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private CartRepository cartRepository;

    private Client testClient;

    private SecurityClient securityClient;

    @BeforeEach
    void setUp() {
        testClient = clientRepository.save(
                new Client(null, "John Doe", "StrongP@ss1", "john@example.com", null, 1)
        );
        Long cartId = cartService.createNewCart(testClient.getId());
        testClient.setCartId(cartId);
        clientRepository.save(testClient);
        securityClient = (SecurityClient) customUserDetailsService.loadUserByUsername(testClient.getEmail());
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
        cartRepository.deleteAll();
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testGetClientById_ClientExists_WhenClientHasAccess() throws Exception {
        mockMvc.perform(get(apiPath + "/client/{id}", testClient.getId())
                        .with(user(securityClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()))
                .andExpect(jsonPath("$.name").value(testClient.getName()))
                .andExpect(jsonPath("$.email").value(testClient.getEmail()))
                .andExpect(jsonPath("$.cartId").value(testClient.getCartId()))
                .andExpect(jsonPath("$.orderId").value(testClient.getOrderId()));
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testGetClientById_ClientExists_WhenClientHasNotAccess() throws Exception {
        Client otherClient = clientRepository.save(
                new Client(null, "Notjohn Doe", "NOTStrongP@ss1", "NOTjohn@example.com", null, null)
        );

        mockMvc.perform(get(apiPath + "/client/{id}", otherClient.getId())
                        .with(user(securityClient)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testGetClientById_WhenClientDoesNotExist() throws Exception {
        Long nonExistentClientId = Long.MAX_VALUE;

        mockMvc.perform(get(apiPath + "/client/{id}", nonExistentClientId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testGetClientById_WhenIllegalArgumentException() throws Exception {
        String badRequest = "badRequest";

        mockMvc.perform(get(apiPath + "/client/{id}", badRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testUpdateClient_ClientExists_WhenClientHasAccess() throws Exception {
        UpdateClientRequest updateClientRequest = new UpdateClientRequest("newEmail@gmail.com");

        mockMvc.perform(put(apiPath + "/client/{id}", testClient.getId())
                        .with(user(securityClient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateClientRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testClient.getId()))
                .andExpect(jsonPath("$.name").value(testClient.getName()))
                .andExpect(jsonPath("$.email").value("newEmail@gmail.com"))
                .andExpect(jsonPath("$.cartId").value(testClient.getCartId()))
                .andExpect(jsonPath("$.orderId").value(testClient.getOrderId()));
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testUpdateClient_ClientExist_WhenClientHasNoAccess() throws Exception {
        Client otherTestClient = clientRepository.save(
                new Client(null, "Notjohn Doe", "StrongP@ss1", "NOTjohn@example.com", null, null)
        );
        UpdateClientRequest updateClientRequest = new UpdateClientRequest("newEmail@gmail.com");

        mockMvc.perform(put(apiPath + "/client/{id}", otherTestClient.getId())
                        .with(user(securityClient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateClientRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testUpdateClient_ClientDoesNotExist() throws Exception {
        Long nonExistentClientId = Long.MAX_VALUE;
        UpdateClientRequest updateClientRequest = new UpdateClientRequest("newEmail@gmail.com");

        mockMvc.perform(put(apiPath + "/client/{id}", nonExistentClientId)
                        .with(user(securityClient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateClientRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void updateClient_BadRequest() throws Exception {
        UpdateClientRequest updateClientRequest = new UpdateClientRequest("wrongValue");

        mockMvc.perform(put(apiPath + "/client/{id}", testClient.getId())
                        .with(user(securityClient))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(updateClientRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testDeleteClient_ClientExists_WhenClientHasAccess() throws Exception {
        mockMvc.perform(delete(apiPath + "/client/{id}", testClient.getId())
                        .with(user(securityClient)))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "tester", roles = "USER")
    public void testDeleteClient_ClientExists_WhenClientHasNoAccess() throws Exception {
        Client otherTestClient = clientRepository.save(
                new Client(null, "Notjohn Doe", "StrongP@ss1", "NOTjohn@example.com", null, null)
        );

        mockMvc.perform(delete(apiPath + "/client/{id}", otherTestClient.getId())
                        .with(user(securityClient)))
                .andExpect(status().isForbidden());
    }
}
