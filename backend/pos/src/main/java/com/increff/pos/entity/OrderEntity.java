package com.increff.pos.entity;

import com.increff.pos.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "`order`",
        indexes = {
            @Index(name = "idx_orders_client_id", columnList = "client_id"),
            @Index(name = "idx_orders_status", columnList = "status"),
            @Index(name = "idx_orders_client_status", columnList = "client_id, status")
        }
)
@Getter
@Setter
public class OrderEntity extends AbstractEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(
            name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "order_id", allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private Integer clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
}
