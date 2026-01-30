package com.increff.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "invoice")
@Getter
@Setter
public class InvoiceEntity extends AbstractEntity {

    @Id
    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = false)
    private String filePath;

}
