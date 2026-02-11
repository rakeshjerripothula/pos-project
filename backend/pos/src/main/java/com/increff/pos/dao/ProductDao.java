package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ProductDao {

    @PersistenceContext
    private EntityManager em;

    public List<ProductEntity> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);

        Root<ProductEntity> product = cq.from(ProductEntity.class);

        cq.select(product).orderBy(cb.asc(product.get("productName")));

        TypedQuery<ProductEntity> query = em.createQuery(cq);
        return query.getResultList();
    }

    public Optional<ProductEntity> findById(Integer id) {
        return Optional.ofNullable(em.find(ProductEntity.class, id));
    }

    public ProductEntity save(ProductEntity product) {
        if (Objects.isNull(product.getId())) {
            em.persist(product);
            return product;
        }
        return em.merge(product);
    }

    public List<ProductEntity> saveAll(List<ProductEntity> products) {
        int batchSize = 50;

        for (int i = 0; i < products.size(); i++) {
            ProductEntity product = products.get(i);
            if (product.getId() == null) {
                em.persist(product);
            } else {
                em.merge(product);
            }

            if (i > 0 && i % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }

        return products;
    }

    public List<ProductEntity> findAllById(List<Integer> ids) {

        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);
        cq.select(root).where(root.get("id").in(ids));

        return em.createQuery(cq).getResultList();
    }

    public List<String> findExistingBarcodes(List<String> barcodes) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(root.get("barcode")).where(root.get("barcode").in(barcodes));

        return em.createQuery(cq).getResultList();
    }

    public boolean existsByBarcodeAndNotId(String barcode, Integer id) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(cb.literal(1)).where(cb.equal(root.get("barcode"), barcode), cb.notEqual(root.get("id"), id));

        return !em.createQuery(cq).setMaxResults(1).getResultList().isEmpty();
    }


    public Page<ProductEntity> searchProducts(Integer clientId, String barcode, String productName, Pageable pageable) {
        String normalizedBarcode = barcode == null ? null : barcode.trim().toLowerCase();
        String normalizedProductName = productName == null ? null : productName.trim().toLowerCase();

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        Subquery<Integer> sub = cq.subquery(Integer.class);
        Root<ClientEntity> client = sub.from(ClientEntity.class);

        List<Predicate> subPredicates = new ArrayList<>();
        subPredicates.add(cb.equal(client.get("id"), product.get("clientId")));
        subPredicates.add(cb.isTrue(client.get("enabled")));

        if (clientId != null) {
            subPredicates.add(cb.equal(product.get("clientId"), clientId));
        }

        sub.select(cb.literal(1)).where(subPredicates.toArray(new Predicate[0]));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.exists(sub));

        if (normalizedBarcode != null && !normalizedBarcode.isEmpty()) {
            predicates.add(cb.equal(cb.lower(product.get("barcode")), normalizedBarcode));
        }

        if (normalizedProductName != null && !normalizedProductName.isEmpty()) {
            predicates.add(cb.like(cb.lower(product.get("productName")), "%" + normalizedProductName + "%"));
        }

        cq.select(product).where(predicates.toArray(new Predicate[0])).orderBy(cb.asc(product.get("productName")));

        List<ProductEntity> data = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset()).setMaxResults(pageable.getPageSize()).getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProductEntity> countProduct = countQuery.from(ProductEntity.class);

        Subquery<Integer> countSub = countQuery.subquery(Integer.class);
        Root<ClientEntity> countClient = countSub.from(ClientEntity.class);

        countSub.select(cb.literal(1))
                .where(
                        cb.equal(countClient.get("id"), countProduct.get("clientId")),
                        cb.isTrue(countClient.get("enabled"))
                );

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.exists(countSub));

        if (clientId != null) {
            countPredicates.add(cb.equal(countProduct.get("clientId"), clientId));
        }

        if (normalizedBarcode != null && !normalizedBarcode.isEmpty()) {
            countPredicates.add(cb.equal(cb.lower(countProduct.get("barcode")), normalizedBarcode));
        }

        if (normalizedProductName != null && !normalizedProductName.isEmpty()) {
            countPredicates.add(cb.like(cb.lower(countProduct.get("productName")), "%" + normalizedProductName + "%"));
        }

        countQuery.select(cb.count(countProduct)).where(countPredicates.toArray(new Predicate[0]));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }


    public boolean existsByClientIdAndProductNameAndMrpAndNotId(
            Integer clientId,
            String productName,
            BigDecimal mrp,
            Integer productId
    ) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(cb.literal(1))
                .where(cb.equal(root.get("clientId"), clientId), cb.equal(root.get("productName"), productName),
                        cb.equal(root.get("mrp"), mrp), cb.notEqual(root.get("id"), productId));

        return !em.createQuery(cq).setMaxResults(1).getResultList().isEmpty();
    }

    public boolean existsByBarcode(String barcode) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(cb.literal(1)).where(cb.equal(root.get("barcode"), barcode));

        return !em.createQuery(cq).setMaxResults(1).getResultList().isEmpty();
    }


    public boolean existsByClientIdAndProductNameAndMrp(Integer clientId, String productName, BigDecimal mrp) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(cb.literal(1)).where(
                        cb.equal(root.get("clientId"), clientId), cb.equal(root.get("productName"), productName),
                        cb.equal(root.get("mrp"), mrp));

        return !em.createQuery(cq).setMaxResults(1).getResultList().isEmpty();
    }

}
