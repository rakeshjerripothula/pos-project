package com.increff.pos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractClientEntity extends AbstractEntity {

    @Column(nullable = false)
    private String field2;
}
