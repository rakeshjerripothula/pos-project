package com.increff.pos.dao;

import com.increff.pos.entity.OrderEntity;
import com.increff.pos.domain.OrderStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class OrderDao {

    @PersistenceContext
    private EntityManager em;

    public Page<OrderEntity> search(OrderStatus status, Integer clientId, ZonedDateTime start, ZonedDateTime end,
            Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<OrderEntity> cq = cb.createQuery(OrderEntity.class);
        Root<OrderEntity> root = cq.from(OrderEntity.class);

        List<Predicate> predicates = buildPredicates(cb, root, status, clientId, start, end);

        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(root.get("createdAt")));

        TypedQuery<OrderEntity> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<OrderEntity> orders = query.getResultList();

        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<OrderEntity> countRoot = countCq.from(OrderEntity.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, status, clientId, start, end);

        countCq.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(orders, pageable, total);
    }

    public OrderEntity save(OrderEntity order) {
        if (Objects.isNull(order.getId())) {
            em.persist(order);
            return order;
        }
        return em.merge(order);
    }

    public Optional<OrderEntity> findById(Integer orderId) {
        return Optional.ofNullable(em.find(OrderEntity.class, orderId));
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
