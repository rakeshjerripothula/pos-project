package com.increff.pos.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Entity
@Table(
        name = "product",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_barcode", columnNames = {"barcode"}),
                @UniqueConstraint(name = "uk_client_name_mrp", columnNames = {"client_id", "product_name", "mrp"}),
    }
)
@Getter
@Setter
public class ProductEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(
            name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "product_id", allocationSize = 50
    )
    private Integer id;

    @Column(nullable = false)
    private String productName;


    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal mrp;

    @Column(nullable = false)
    private Integer clientId;

    @Column(nullable = false)
    private String barcode;

    @Column
    private String imageUrl;
}
