package com.increff.pos.entity;

import com.increff.pos.domain.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "`user`",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_email", columnNames = {"email"})
)
@Getter
@Setter
public class UserEntity extends AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "id_gen")
    @TableGenerator(
            name = "id_gen", table = "id_generator", pkColumnName = "gen_name", valueColumnName = "gen_value",
            pkColumnValue = "user_id", allocationSize = 1
    )
    private Integer id;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
