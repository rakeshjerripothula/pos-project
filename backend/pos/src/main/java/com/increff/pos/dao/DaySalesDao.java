package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class DaySalesDao {

    @PersistenceContext
    private EntityManager em;

    public DaySalesEntity save(DaySalesEntity entity) {
        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    public Optional<DaySalesEntity> findByDate(LocalDate date) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesEntity> cq = cb.createQuery(DaySalesEntity.class);

        Root<DaySalesEntity> root = cq.from(DaySalesEntity.class);

        cq.select(root)
                .where(cb.equal(root.get("date"), date));

        List<DaySalesEntity> result = em.createQuery(cq).getResultList();

        return result.isEmpty()
                ? Optional.empty()
                : Optional.of(result.getFirst());
    }

    public List<DaySalesEntity> findByDateRange(LocalDate startDate, LocalDate endDate) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesEntity> cq = cb.createQuery(DaySalesEntity.class);

        Root<DaySalesEntity> root = cq.from(DaySalesEntity.class);

        cq.select(root)
                .where(cb.between(root.get("date"), startDate, endDate))
                .orderBy(cb.desc(root.get("date")));

        return em.createQuery(cq).getResultList();
    }

    public void deleteByDate(LocalDate date) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<DaySalesEntity> cd = cb.createCriteriaDelete(DaySalesEntity.class);

        Root<DaySalesEntity> root = cd.from(DaySalesEntity.class);

        cd.where(cb.equal(root.get("date"), date));

        em.createQuery(cd).executeUpdate();
    }
}
