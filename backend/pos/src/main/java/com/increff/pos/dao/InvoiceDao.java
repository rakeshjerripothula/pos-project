package com.increff.pos.dao;

import com.increff.pos.entity.InvoiceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InvoiceDao {

    @PersistenceContext
    private EntityManager em;

    public InvoiceEntity save(InvoiceEntity invoice) {
        em.persist(invoice);
        return invoice;
    }

    public InvoiceEntity selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InvoiceEntity> cq = cb.createQuery(InvoiceEntity.class);

        Root<InvoiceEntity> root = cq.from(InvoiceEntity.class);

        cq.select(root).where(cb.equal(root.get("orderId"), orderId));

        return em.createQuery(cq).getResultStream().findFirst().orElse(null);
    }
}
