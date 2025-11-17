package org.example.teamshop.service.CartService;

import jakarta.persistence.EntityNotFoundException;
import org.example.teamshop.dto.CartDTO;
import org.example.teamshop.mapper.CartMapper;
import org.example.teamshop.model.Cart;
import org.example.teamshop.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class CartServiceTest {

    @InjectMocks
    private CartService cartService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartRepository cartRepository;

    @Test
    public void testCreateNewCart_Successfully_AndReturnCartId() {
        Long clientId = 1L;

        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation ->
        {
            Cart savedCart = invocation.getArgument(0);
            savedCart.setId(1L);
            return savedCart;
        });

        Long cartId = cartService.createNewCart(clientId);

        assertNotNull(cartId);
        assertEquals(1L, cartId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testCreateNewCart_ClientIdIsNull_ShouldThrowException() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.createNewCart(null);
        });

        assertEquals("Client Id cannot be null", exception.getMessage());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    public void testFindCartDTOById_Successfully_AndReturnCartDTO() {
        Long cartId = 1L;
        Cart cart = new Cart(1L, 1L, new ArrayList<>());
        CartDTO expectedCartDTO = new CartDTO(1L, 1L, new ArrayList<>());

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartDTO(cart)).thenReturn(expectedCartDTO);

        CartDTO cartDTO = cartService.findCartDTOById(cartId);

        assertNotNull(cartDTO);
        assertEquals(expectedCartDTO.getId(), cartDTO.getId());
        assertEquals(expectedCartDTO.getClientId(), cartDTO.getClientId());
        assertEquals(expectedCartDTO.getItems().size(), cartDTO.getItems().size());
        verify(cartRepository, times(1)).findById(anyLong());
        verify(cartMapper, times(1)).toCartDTO(cart);
    }

    @Test
    public void testFindCartDTOById_ClientIdIsNull_ShouldThrowException() {
        Long cartId = null;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.findCartDTOById(cartId);
        });

        assertEquals("Cart Id cannot be null", exception.getMessage());
        verify(cartRepository, never()).findById(anyLong());
        verify(cartMapper, never()).toCartDTO(any());
    }

    @Test
    public void testFindCartDTOById_IdDoesNotExist_ShouldThrowException() {
        Long cartId = 999L;

        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            cartService.findCartDTOById(cartId);
        });

        assertEquals("Cart not found", exception.getMessage());
        verify(cartMapper, never()).toCartDTO(any());
    }

    @Test
    public void testFindCartEntityById_Successfully_AndReturnCartEntity() {
        Long cartId = 1L;
        Cart cart = new Cart(1L, 1L, new ArrayList<>());

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        assertEquals(cart, cartService.findCartEntityById(cartId));
        verify(cartRepository, times(1)).findById(cartId);
    }

    @Test
    public void testFindCartEntityById_CartIdIsNull_ShouldThrowException() {
        Long cartId = null;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.findCartEntityById(cartId);
        });

        assertEquals("Cart Id cannot be null", exception.getMessage());
        verify(cartRepository, never()).findById(cartId);
    }

    @Test
    public void testFindCartEntityById_CartIdDoesNotExist_ShouldThrowException() {
        Long cartId = 999L;

        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            cartService.findCartEntityById(cartId);
        });

        assertEquals("Cart not found", exception.getMessage());
        verify(cartRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testDeleteCart_Successfully() {
        Long cartId = 1L;
        Cart cart = new Cart(cartId, 1L, new ArrayList<>());

        when(cartRepository.findById(cartId)).thenReturn(Optional.of(cart));

        cartService.deleteCart(cartId);

        verify(cartRepository, times(1)).findById(anyLong());
        verify(cartRepository, times(1)).delete(cart);
    }

    @Test
    public void testDeleteCart_CartIdIsNull_ShouldThrowException() {
        Long cartId = null;

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            cartService.deleteCart(cartId);
        });

        assertEquals("Cart Id cannot be null", exception.getMessage());
        verify(cartRepository, never()).findById(anyLong());
        verify(cartRepository, never()).delete(any());
    }

    @Test
    public void testDeleteCart_CartIdDoesNotExist_ShouldThrowException() {
        Long cartId = 999L;

        when(cartRepository.findById(cartId)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            cartService.deleteCart(cartId);
        });

        assertEquals("Cart not found", exception.getMessage());
        verify(cartRepository, times(1)).findById(anyLong());
    }
}
