package org.example.teamshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.teamshop.Exception.AlreadyExistingResourceException;
import org.example.teamshop.Exception.FailedOperationException;
import org.example.teamshop.dto.ClientDTO;
import org.example.teamshop.request.CreateClientRequest;
import org.example.teamshop.request.LoginRequest;
import org.example.teamshop.service.ClientService.ClientService;
import org.example.teamshop.service.SecurityServices.CustomUserDetailsService.CustomUserDetailsService;
import org.example.teamshop.service.SecurityServices.JwtService.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.path}/auth/")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    private final ClientService clientService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getEmail());
            String token = jwtService.generateToken(userDetails);
            return ResponseEntity.ok(token);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        }
    }

    @Operation(summary = "Add client to DB")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Client successfully added to the database"),
            @ApiResponse(responseCode = "400", description = "Invalid client data provided. The request body is missing required fields or contains invalid data."),
            @ApiResponse(responseCode = "500", description = "Internal server error. Something went wrong on the server side.")
    })
    @PostMapping("/register")
    public ResponseEntity<ClientDTO> createClient(
            @Parameter(description = "JSON file of client (without ID)", required = true)
            @RequestBody @Valid CreateClientRequest createClientRequest) {
        try {
            ClientDTO clientDTO = clientService.addClient(createClientRequest);
            return ResponseEntity.ok(clientDTO);
        } catch (FailedOperationException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (AlreadyExistingResourceException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // should be created admin registration
}
