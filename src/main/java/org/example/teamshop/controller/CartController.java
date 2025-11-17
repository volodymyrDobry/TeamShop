package org.example.teamshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.teamshop.dto.CartDTO;
import org.example.teamshop.securityModel.SecurityClient;
import org.example.teamshop.service.CartService.CartService;
import org.example.teamshop.service.ClientService.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.path}/client/cart/")
public class CartController {
    private final CartService cartService;
    private final ClientService clientService;

    @Operation(summary = "Find cart by client ID", description = "Retrieve cart details by client unique ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart successfully found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
            @ApiResponse(responseCode = "403", description = "User is denied access to this ID"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @GetMapping("/{clientId}")
    public ResponseEntity<CartDTO> getCartByClientId(@Parameter(description = "ID of the client to be fetched", required = true, example = "1")
                                                     @PathVariable final Long clientId,
                                                     @AuthenticationPrincipal SecurityClient securityClient) {
        try {
            if (clientService.clientAccess(securityClient, clientId)) {
                CartDTO cartDTO = cartService.findCartDTOById(clientService.findClientById(clientId).getCartId());
                return ResponseEntity.ok(cartDTO);
            }
            else return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
