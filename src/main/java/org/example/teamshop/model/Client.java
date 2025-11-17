package org.example.teamshop.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "shop_client",
        uniqueConstraints = @UniqueConstraint(columnNames = {"orderId"}))
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    private String name;
    private String password;

    @Column(unique = true)
    private String email;

    @Column(unique = true)
    private Long cartId;

    @Column(nullable = true)
    private Integer orderId;

}
