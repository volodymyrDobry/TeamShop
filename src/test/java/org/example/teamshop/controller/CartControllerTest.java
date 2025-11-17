package org.example.teamshop.controller;

import org.example.teamshop.model.Cart;
import org.example.teamshop.model.Client;
import org.example.teamshop.repository.CartRepository;
import org.example.teamshop.repository.ClientRepository;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

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

    private Cart testCart;

    private SecurityClient securityClient;

    @BeforeEach
    void setUp() {
        testClient = clientRepository.save(
                new Client(null, "John Doe", "StrongP@ss1", "john@example.com", null, 1)
        );
        Long cartId = cartService.createNewCart(testClient.getId());
        testClient.setCartId(cartId);
        clientRepository.save(testClient);
        testCart = cartRepository.findById(cartId).get();
        securityClient = (SecurityClient) customUserDetailsService.loadUserByUsername(testClient.getEmail());
    }

    @AfterEach
    void tearDown() {
        clientRepository.deleteAll();
        cartRepository.deleteAll();
    }

    @Test
    public void testGetCartByClientId_CartExist_WhenClientHasAccess() throws Exception {
        mockMvc.perform(get(apiPath + "/client/cart/{clientId}", testClient.getId())
                        .with(user(securityClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCart.getId()))
                .andExpect(jsonPath("$.clientId").value(testCart.getClientId()));
    }

    @Test
    public void testGetCartByClientId_CartNotExist_WhenClientHasAccess() throws Exception {
        cartRepository.delete(testCart);

        mockMvc.perform(get(apiPath + "/client/cart/{clientId}", testClient.getId())
                        .with(user(securityClient)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetCartByClientId_WhenClientDoesNotHaveAccess() throws Exception {
        Client unauthorizedClient = new Client(null, "John Unauthorized", "StrongP@ss1", "unauthorized@example.com", null, 2);
        clientRepository.save(unauthorizedClient);
        SecurityClient unauthorizedSecurityClient = (SecurityClient) customUserDetailsService.loadUserByUsername(unauthorizedClient.getEmail());

        mockMvc.perform(get(apiPath + "/client/cart/{clientId}", testClient.getId())
                        .with(user(unauthorizedSecurityClient)))
                .andExpect(status().isForbidden());
    }
}
