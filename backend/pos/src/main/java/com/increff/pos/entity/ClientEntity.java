package com.increff.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(
        name = "client",
        uniqueConstraints = @UniqueConstraint(name = "uk_client_client_name", columnNames = {"client_name"})
)
@Setter
@Getter
public class ClientEntity extends AbstractEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "client_id", allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private String clientName;

    @Column(nullable = false)
    private Boolean enabled = true;

}