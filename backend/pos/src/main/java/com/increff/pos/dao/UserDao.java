package com.increff.pos.dao;

import com.increff.pos.entity.UserEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class UserDao {

    @PersistenceContext
    private EntityManager em;

    public void insert(UserEntity user) {
        em.persist(user);
    }

    public Optional<UserEntity> findById(Integer userId) {
        return Optional.ofNullable(em.find(UserEntity.class, userId));
    }

    public Optional<UserEntity> findByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<UserEntity> cq = cb.createQuery(UserEntity.class);
        Root<UserEntity> root = cq.from(UserEntity.class);

        cq.select(root).where(cb.equal(root.get("email"), email));

        try {
            return Optional.of(em.createQuery(cq).getSingleResult());
        } catch (jakarta.persistence.NoResultException e) {
            return Optional.empty();
        }
    }
}
