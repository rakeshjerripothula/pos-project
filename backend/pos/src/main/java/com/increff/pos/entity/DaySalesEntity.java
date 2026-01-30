package com.increff.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "pos_day_sales",
        indexes = {
                @Index(name = "idx_day_sales_date", columnList = "date")
        }
)
@Getter
@Setter
public class DaySalesEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(
            name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "day_sales_id", allocationSize = 1
    )
    private Integer id;

    @Column(name = "date", nullable = false, unique = true)
    private LocalDate date;

    @Column(name = "invoiced_orders_count", nullable = false)
    private Integer invoicedOrdersCount;

    @Column(name = "invoiced_items_count", nullable = false)
    private Integer invoicedItemsCount;

    @Column(name = "total_revenue", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalRevenue;
}
