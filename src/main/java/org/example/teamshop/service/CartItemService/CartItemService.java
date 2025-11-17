package org.example.teamshop.service.CartItemService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.teamshop.dto.CartItemDTO;
import org.example.teamshop.mapper.CartItemMapper;
import org.example.teamshop.model.Cart;
import org.example.teamshop.model.CartItem;
import org.example.teamshop.repository.CartItemRepository;
import org.example.teamshop.repository.CartRepository;
import org.example.teamshop.repository.ProductRepository;
import org.example.teamshop.request.UpdateCartItemRequest;
import org.example.teamshop.service.CartService.CartService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class CartItemService implements ICartItemService {
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final CartItemMapper cartItemMapper;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Override
    public CartItemDTO addNewCartItem(UpdateCartItemRequest request) {
        if (!productRepository.existsById(request.getProductId()))
            throw new EntityNotFoundException("Product with ID " + request.getProductId() + " not found");
        Cart cart = cartService.findCartEntityById(request.getCartId());
        CartItem cartItem = cartItemMapper.toCartItemFromUpdateCartItemRequest(request);
        cartItem.setCart(cart);
        cartItemRepository.save(cartItem);
        cart.getItems().add(cartItem);
        cartRepository.save(cart);
        return cartItemMapper.toCartItemDTO(cartItem);
    }

    @Override
    public CartItemDTO updateCartItem(UpdateCartItemRequest request) {
        Cart cart = cartService.findCartEntityById(request.getCartId());
        if (request.getQuantity() == 0) {
            deleteCartItem(request.getProductId(), request.getCartId());
            return null;
        }
        if (cart.getItems().isEmpty()) return addNewCartItem(request);
        else {
            for (CartItem cartItem1 : cart.getItems()) {
                if (cartItem1.getProductId().equals(request.getProductId())) {
                    cartItem1.setQuantity(request.getQuantity());
                    cartRepository.save(cart);
                    return cartItemMapper.toCartItemDTO(cartItem1);
                }
            }
        }
        return addNewCartItem(request);
    }

    @Override
    public CartItemDTO getCartItemByProductId(Long productId, Long cartId) {
        Cart cart = cartService.findCartEntityById(cartId);
        for (CartItem cartItem1 : cart.getItems()) {
            if (cartItem1.getProductId().equals(productId)) {
                return cartItemMapper.toCartItemDTO(cartItem1);
            }
        }
        throw new EntityNotFoundException("There is no such type of product in this cart");
    }

    @Override
    public List<CartItemDTO> getAllCartItems(Long cartId) {
        Cart cart = cartService.findCartEntityById(cartId);
        List<CartItem> cartItems = cart.getItems();
        List<CartItemDTO> cartItemDTOs = new ArrayList<>();
        for (CartItem cartItem1 : cartItems) {
            cartItemDTOs.add(cartItemMapper.toCartItemDTO(cartItem1));
        }
        return cartItemDTOs;
    }

    @Override
    public void deleteCartItem(Long productId, Long cartId) {
        Cart cart = cartService.findCartEntityById(cartId);
        for (CartItem cartItem1 : cart.getItems()) {
            if (cartItem1.getProductId().equals(productId)) {
                cart.getItems().remove(cartItem1);
                cartItemRepository.delete(cartItem1);
                cartRepository.save(cart);
                return;
            }
        }
    }
}
