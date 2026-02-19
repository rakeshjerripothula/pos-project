package com.increff.pos.dao;

import com.increff.pos.entity.ProductEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class ProductDao extends AbstractDao<ProductEntity> {

    public ProductDao() {
        super(ProductEntity.class);
    }

    @Override
    protected boolean isNew(ProductEntity entity) {
        return entity.getId() == null;
    }

    public List<ProductEntity> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public Optional<ProductEntity> selectByBarcode(String barcode) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root).where(cb.equal(root.get("barcode"), barcode));
        return em.createQuery(cq).getResultList().stream().findFirst();
    }

    public Optional<ProductEntity> selectByBarcodeExcludingId(String barcode, Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root).where(cb.and(cb.equal(root.get("barcode"), barcode), cb.notEqual(root.get("id"), id)));
        return em.createQuery(cq).getResultList().stream().findFirst();
    }

    public Optional<ProductEntity> selectByClientIdAndProductNameAndMrp(Integer clientId, String productName,
                                                                        BigDecimal mrp) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root).where(cb.and(
                cb.equal(root.get("clientId"), clientId),
                cb.equal(root.get("productName"), productName),
                cb.equal(root.get("mrp"), mrp)
        ));
        return em.createQuery(cq).getResultList().stream().findFirst();
    }

    public Optional<ProductEntity> selectByClientIdAndProductNameAndMrpExcludingId(Integer clientId, String productName,
                                                                                   BigDecimal mrp, Integer productId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root).where(cb.and(cb.equal(root.get("clientId"), clientId),
                cb.equal(root.get("productName"), productName), cb.equal(root.get("mrp"), mrp),
                cb.notEqual(root.get("id"), productId)
        ));
        return em.createQuery(cq).getResultList().stream().findFirst();
    }

    public List<ProductEntity> selectByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root).where(root.get("id").in(ids));
        return em.createQuery(cq).getResultList();
    }

    public ProductEntity selectByName(String productName) {
        if (productName == null || productName.isEmpty()) return null;

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(root).where(cb.equal(cb.lower(root.get("productName")), productName.trim().toLowerCase()));

        List<ProductEntity> result = em.createQuery(cq).setMaxResults(1).getResultList();
        return result.isEmpty() ? null : result.getFirst();
    }

    public List<ProductEntity> selectByNameIn(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);

        Root<ProductEntity> root = cq.from(ProductEntity.class);

        Predicate namePredicate = root.get("name").in(names);

        cq.select(root).where(namePredicate);

        return em.createQuery(cq).getResultList();
    }

    public Page<ProductEntity> selectByFilters(Integer clientId, String barcode, String productName, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> dataQuery = buildSelectQuery(cb, clientId, barcode, productName);
        CriteriaQuery<Long> countQuery = buildCountQuery(cb, clientId, barcode, productName);
        return executePagedQuery(dataQuery, countQuery, pageable);
    }

    private CriteriaQuery<ProductEntity> buildSelectQuery(CriteriaBuilder cb, Integer clientId, String barcode,
                                                          String productName) {
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        List<Predicate> predicates = buildPredicates(cb, root, clientId, barcode, productName);
        cq.select(root).where(predicates.toArray(new Predicate[0]));
        return cq;
    }

    private CriteriaQuery<Long> buildCountQuery(CriteriaBuilder cb, Integer clientId, String barcode, String productName) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        List<Predicate> predicates = buildPredicates(cb, root, clientId, barcode, productName);
        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
        return cq;
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<ProductEntity> root, Integer clientId,
                                            String barcode, String productName) {
        List<Predicate> predicates = new ArrayList<>();

        if (clientId != null)
            predicates.add(cb.equal(root.get("clientId"), clientId));

        if (barcode != null && !barcode.isEmpty())
            predicates.add(cb.equal(root.get("barcode"), barcode));

        if (productName != null && !productName.isEmpty())
            predicates.add(cb.like(root.get("productName"), "%" + productName + "%"));

        return predicates;
    }
}
