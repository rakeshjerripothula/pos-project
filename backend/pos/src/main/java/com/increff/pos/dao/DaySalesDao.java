package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DaySalesDao extends AbstractDao<DaySalesEntity>{

    @PersistenceContext
    private EntityManager em;

    public DaySalesDao() {
        super(DaySalesEntity.class);
    }

    @Override
    protected boolean isNew(DaySalesEntity entity) {
        return entity.getId() == null;
    }

    public Optional<DaySalesEntity> selectByDate(LocalDate date) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesEntity> cq = cb.createQuery(DaySalesEntity.class);
        Root<DaySalesEntity> root = cq.from(DaySalesEntity.class);

        cq.select(root).where(cb.equal(root.get("date"), date));

        return em.createQuery(cq).getResultList().stream().findFirst();
    }

    public Page<DaySalesEntity> selectByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<DaySalesEntity> dataQuery = buildDateRangeQuery(cb, startDate, endDate);

        CriteriaQuery<Long> countQuery = buildDateRangeCountQuery(cb, startDate, endDate);

        return executePagedQuery(dataQuery, countQuery, pageable);
    }

    public List<DaySalesEntity> selectAllByDateRange(LocalDate startDate, LocalDate endDate) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesEntity> cq = buildDateRangeQuery(cb, startDate, endDate);
        return em.createQuery(cq).getResultList();
    }

    private CriteriaQuery<DaySalesEntity> buildDateRangeQuery(CriteriaBuilder cb, LocalDate startDate,
                                                              LocalDate endDate) {

        CriteriaQuery<DaySalesEntity> cq = cb.createQuery(DaySalesEntity.class);
        Root<DaySalesEntity> root = cq.from(DaySalesEntity.class);

        List<Predicate> predicates = buildDatePredicates(cb, root, startDate, endDate);

        cq.select(root).where(predicates.toArray(new Predicate[0])).orderBy(cb.desc(root.get("date")));

        return cq;
    }

    private CriteriaQuery<Long> buildDateRangeCountQuery(CriteriaBuilder cb, LocalDate startDate, LocalDate endDate) {

        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<DaySalesEntity> root = cq.from(DaySalesEntity.class);

        List<Predicate> predicates = buildDatePredicates(cb, root, startDate, endDate);

        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));

        return cq;
    }

    private List<Predicate> buildDatePredicates(CriteriaBuilder cb, Root<DaySalesEntity> root, LocalDate startDate,
                                                LocalDate endDate) {

        List<Predicate> predicates = new ArrayList<>();

        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }

        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        return predicates;
    }

}
