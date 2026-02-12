package com.increff.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "inventory",
        uniqueConstraints = @UniqueConstraint(name = "uk_inventory_product_id", columnNames = {"product_id"})
)
@Setter
@Getter
public class InventoryEntity extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(
            name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "inventory_id", allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer quantity;

}
