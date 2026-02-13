package com.increff.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
        name = "order_item",uniqueConstraints = @UniqueConstraint(
            name = "uk_order_product",
            columnNames = {"order_id", "product_id"}
        ), indexes = {
                @Index(name = "idx_order_item_order_id", columnList = "order_id"),
                @Index(name = "idx_order_item_product_id", columnList = "product_id"),
                @Index(name = "idx_order_item_order_product", columnList = "order_id, product_id")
        }
)
@Getter
@Setter
public class OrderItemEntity extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(
            name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "order_item_id", allocationSize = 50
    )
    private Integer id;

    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private Integer productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal sellingPrice;
}
