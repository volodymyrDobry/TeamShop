package org.example.teamshop.service.CartService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.teamshop.dto.CartDTO;
import org.example.teamshop.mapper.CartMapper;
import org.example.teamshop.model.Cart;
import org.example.teamshop.repository.CartRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CartService implements ICartService {
    private final CartRepository cartRepository;
    private final CartMapper cartMapper;


    @Override
    public Long createNewCart(Long clientId) {
        if (clientId == null)
            throw new IllegalArgumentException("Client Id cannot be null");

        Cart cart = new Cart();
        cart.setClientId(clientId);
        cartRepository.save(cart);
        return cart.getId();
    }

    @Override
    public CartDTO findCartDTOById(Long id) {
        Cart cart = findCartEntityById(id);
        return cartMapper.toCartDTO(cart);
    }

    @Override
    public Cart findCartEntityById(Long cartId) {
        if (cartId == null)
            throw new IllegalArgumentException("Cart Id cannot be null");

        return cartRepository
                .findById(cartId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found"));
    }

    @Override
    public void deleteCart(Long id) {
        Cart cart = findCartEntityById(id);
        cartRepository.delete(cart);
    }
}
