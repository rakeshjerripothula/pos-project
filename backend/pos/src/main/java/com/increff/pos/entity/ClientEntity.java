package com.increff.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(
        name = "client",
        uniqueConstraints = @UniqueConstraint(name = "uk_client_client_name", columnNames = {"client_name"}),
        indexes = @Index(name = "idx_client_enabled", columnList = "enabled")
)
@AttributeOverride(name = "field2", column = @Column(name = "client_name", nullable = false))
@Setter
@Getter
public class ClientEntity extends AbstractClientEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "client_id", allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private Boolean enabled = true;

    private String name;

    public String getClientName() {
        return getField2();
    }

    public void setClientName(String clientName) {
        setField2(clientName);
    }
}
