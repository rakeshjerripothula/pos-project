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
public class OrderItemDao extends AbstractDao<OrderItemEntity>{

    @PersistenceContext
    private EntityManager em;

    public OrderItemDao() {
        super(OrderItemEntity.class);
    }

    @Override
    protected boolean isNew(OrderItemEntity entity) {
        return entity.getId() == null;
    }

    public List<OrderItemEntity> selectByOrderId(Integer orderId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<OrderItemEntity> cq = cb.createQuery(OrderItemEntity.class);

        Root<OrderItemEntity> root = cq.from(OrderItemEntity.class);

        cq.select(root).where(cb.equal(root.get("orderId"), orderId));

        return em.createQuery(cq).getResultList();
    }
}
