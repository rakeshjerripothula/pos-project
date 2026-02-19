package com.increff.pos.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public abstract class AbstractDao<T> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> entityClass;

    protected AbstractDao(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Optional<T> selectById(Integer id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    public T save(T entity) {
        if (isNew(entity)) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    public List<T> saveAll(List<T> entities) {
        if (entities == null || entities.isEmpty()) return List.of();

        int batchSize = 50;

        for (int i = 0; i < entities.size(); i++) {
            T entity = entities.get(i);

            if (isNew(entity)) em.persist(entity);
            else em.merge(entity);

            if ((i + 1) % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }

        em.flush();
        em.clear();

        return entities;
    }

    protected boolean exists(CriteriaQuery<Long> countQuery) {
        Long count = em.createQuery(countQuery).getSingleResult();
        return count > 0;
    }

    protected List<T> selectAllOrderedBy(String fieldName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> cq = cb.createQuery(entityClass);
        Root<T> root = cq.from(entityClass);

        cq.select(root).orderBy(cb.asc(root.get(fieldName)));

        return em.createQuery(cq).getResultList();
    }

    protected Page<T> executePagedQuery(CriteriaQuery<T> dataQuery, CriteriaQuery<Long> countQuery, Pageable pageable) {
        List<T> data = em.createQuery(dataQuery).setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize()).getResultList();

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    protected abstract boolean isNew(T entity);
}
