package com.increff.pos.dao;

import com.increff.pos.entity.InvoiceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class InvoiceDao extends AbstractDao<InvoiceEntity> {

    @PersistenceContext
    private EntityManager em;

    public InvoiceDao() {
        super(InvoiceEntity.class);
    }

    @Override
    protected boolean isNew(InvoiceEntity entity) {
        return entity.getOrderId() == null;
    }

    public Optional<InvoiceEntity> selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InvoiceEntity> cq = cb.createQuery(InvoiceEntity.class);

        Root<InvoiceEntity> root = cq.from(InvoiceEntity.class);

        cq.select(root).where(cb.equal(root.get("orderId"), orderId));

        return em.createQuery(cq).getResultList().stream().findFirst();
    }
}
