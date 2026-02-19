package com.increff.pos.dao;

import com.increff.pos.entity.OrderEntity;
import com.increff.pos.model.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class OrderDao extends AbstractDao<OrderEntity>{

    @PersistenceContext
    private EntityManager em;

    public OrderDao() {
        super(OrderEntity.class);
    }

    @Override
    protected boolean isNew(OrderEntity entity) {
        return entity.getId() == null;
    }

    public Page<OrderEntity> selectByFilters(OrderStatus status, Integer clientId, ZonedDateTime start, ZonedDateTime end,
                                    Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<OrderEntity> dataQuery = buildSearchQuery(cb, status, clientId, start, end);

        CriteriaQuery<Long> countQuery = buildCountQuery(cb, status, clientId, start, end);

        return executePagedQuery(dataQuery, countQuery, pageable);
    }

    private CriteriaQuery<OrderEntity> buildSearchQuery(CriteriaBuilder cb, OrderStatus status, Integer clientId,
                                                        ZonedDateTime start, ZonedDateTime end) {

        CriteriaQuery<OrderEntity> cq = cb.createQuery(OrderEntity.class);
        Root<OrderEntity> root = cq.from(OrderEntity.class);

        List<Predicate> predicates = buildPredicates(cb, root, status, clientId, start, end);

        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(root.get("createdAt")));

        return cq;
    }

    private CriteriaQuery<Long> buildCountQuery(CriteriaBuilder cb, OrderStatus status, Integer clientId,
                                                ZonedDateTime start, ZonedDateTime end) {

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<OrderEntity> root = cq.from(OrderEntity.class);

        List<Predicate> predicates = buildPredicates(cb, root, status, clientId, start, end);

        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));

        return cq;
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<OrderEntity> root, OrderStatus status,
                                            Integer clientId, ZonedDateTime start, ZonedDateTime end) {

        List<Predicate> predicates = new ArrayList<>();

        if (status != null) {
            predicates.add(cb.equal(root.get("status"), status));
        }

        if (clientId != null) {
            predicates.add(cb.equal(root.get("clientId"), clientId));
        }

        if (start != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
        }

        if (end != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
        }

        return predicates;
    }

}
