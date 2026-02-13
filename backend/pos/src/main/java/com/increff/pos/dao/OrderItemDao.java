package com.increff.pos.dao;

import com.increff.pos.entity.OrderItemEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderItemDao {

    @PersistenceContext
    private EntityManager em;

    public List<OrderItemEntity> saveAll(List<OrderItemEntity> items) {
        for (OrderItemEntity item : items) {
            em.persist(item);
        }
        return items;
    }

    public List<OrderItemEntity> findByOrderId(Integer orderId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemEntity> cq = cb.createQuery(OrderItemEntity.class);

        Root<OrderItemEntity> root = cq.from(OrderItemEntity.class);

        cq.select(root).where(cb.equal(root.get("orderId"), orderId));

        return em.createQuery(cq).getResultList();
    }
}
