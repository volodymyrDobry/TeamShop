package org.example.teamshop.service.CartService;

import org.example.teamshop.dto.CartDTO;
import org.example.teamshop.model.Cart;

public interface ICartService {
    // return cart ID
    Long createNewCart(Long clientId);

    CartDTO findCartDTOById(Long id);

    Cart findCartEntityById(Long clientId);

    void deleteCart(Long id);
}
