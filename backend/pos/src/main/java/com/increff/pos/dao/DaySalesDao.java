package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
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

        cq.select(root).where(cb.equal(root.get("date"), date));

        List<DaySalesEntity> result = em.createQuery(cq).getResultList();

        return result.isEmpty() ? Optional.empty() : Optional.of(result.getFirst());
    }

    public Page<DaySalesEntity> findByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<DaySalesEntity> cq = cb.createQuery(DaySalesEntity.class);
        Root<DaySalesEntity> root = cq.from(DaySalesEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }

        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(root.get("date")));

        TypedQuery<DaySalesEntity> query = em.createQuery(cq);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<DaySalesEntity> content = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<DaySalesEntity> countRoot = countQuery.from(DaySalesEntity.class);

        List<Predicate> countPredicates = new ArrayList<>();

        if (startDate != null) {
            countPredicates.add(cb.greaterThanOrEqualTo(countRoot.get("date"), startDate));
        }

        if (endDate != null) {
            countPredicates.add(cb.lessThanOrEqualTo(countRoot.get("date"), endDate));
        }

        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    public void deleteByDate(LocalDate date) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<DaySalesEntity> cd = cb.createCriteriaDelete(DaySalesEntity.class);

        Root<DaySalesEntity> root = cd.from(DaySalesEntity.class);

        cd.where(cb.equal(root.get("date"), date));

        em.createQuery(cd).executeUpdate();
    }

    public List<DaySalesEntity> findAllByDateRange(LocalDate startDate, LocalDate endDate) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesEntity> cq = cb.createQuery(DaySalesEntity.class);
        Root<DaySalesEntity> root = cq.from(DaySalesEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }

        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(root.get("date")));

        return em.createQuery(cq).getResultList();
    }
}
