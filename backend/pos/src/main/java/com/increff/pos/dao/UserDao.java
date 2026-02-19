package com.increff.pos.dao;

import com.increff.pos.entity.UserEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserDao extends AbstractDao<UserEntity> {

    public UserDao() {
        super(UserEntity.class);
    }

    @Override
    protected boolean isNew(UserEntity entity) {
        return entity.getId() == null;
    }

    public Optional<UserEntity> selectByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = cq.from(UserEntity.class);
        cq.select(root).where(cb.equal(root.get("email"), email));
        return em.createQuery(cq).getResultList().stream().findFirst();
    }
}
