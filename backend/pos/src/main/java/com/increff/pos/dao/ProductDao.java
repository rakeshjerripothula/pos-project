package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.ProductEntity;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ProductDao {

    @PersistenceContext
    private EntityManager em;

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
            em.persist(products.get(i));

            if (i > 0 && i % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }

        return products;
    }

    public Optional<ProductEntity> findById(Integer id) {
        return Optional.ofNullable(em.find(ProductEntity.class, id));
    }

    public List<ProductEntity> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);

        Root<ProductEntity> product = cq.from(ProductEntity.class);

        cq.select(product).orderBy(cb.asc(product.get("id")));

        TypedQuery<ProductEntity> query = em.createQuery(cq);
        return query.getResultList();
    }

    public List<ProductEntity> findAllById(List<Integer> ids) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(root).where(root.get("id").in(ids));

        return em.createQuery(cq).getResultList();
    }

    public boolean existsByBarcode(String barcode) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(cb.count(root))
                .where(cb.equal(root.get("barcode"), barcode));

        return em.createQuery(cq).getSingleResult() > 0;
    }

    public List<String> findExistingBarcodes(List<String> barcodes) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> cq = cb.createQuery(String.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(root.get("barcode"))
                .where(root.get("barcode").in(barcodes));

        return em.createQuery(cq).getResultList();
    }

    public boolean existsByBarcodeAndNotId(String barcode, Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        Predicate sameBarcode = cb.equal(root.get("barcode"), barcode);
        Predicate differentId = cb.notEqual(root.get("id"), id);

        cq.select(cb.count(root))
                .where(cb.and(sameBarcode, differentId));

        return em.createQuery(cq).getSingleResult() > 0;
    }

    public Page<ProductEntity> findProductsForEnabledClients(Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> productRoot = cq.from(ProductEntity.class);
        Root<ClientEntity> clientRoot = cq.from(ClientEntity.class);

        Predicate joinCondition = cb.equal(productRoot.get("clientId"), clientRoot.get("id"));

        Predicate clientEnabled = cb.isTrue(clientRoot.get("enabled"));

        cq.select(productRoot).where(cb.and(joinCondition, clientEnabled));

        List<ProductEntity> data = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ProductEntity> countProduct = countQuery.from(ProductEntity.class);
        Root<ClientEntity> countClient = countQuery.from(ClientEntity.class);

        Predicate countJoin = cb.equal(countProduct.get("clientId"), countClient.get("id"));

        Predicate countEnabled = cb.isTrue(countClient.get("enabled"));

        countQuery.select(cb.count(countProduct)).where(cb.and(countJoin, countEnabled));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    public boolean existsByClientIdAndProductNameAndMrpAndNotId(
            Integer clientId,
            String productName,
            BigDecimal mrp,
            Integer productId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);

        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(cb.literal(1))
                .where(
                        cb.equal(root.get("clientId"), clientId),
                        cb.equal(root.get("productName"), productName),
                        cb.equal(root.get("mrp"), mrp),
                        cb.notEqual(root.get("id"), productId)
                );

        return !em.createQuery(cq)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    public boolean existsByClientIdAndProductNameAndMrp(
            Integer clientId,
            String productName,
            BigDecimal mrp) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(cb.count(root)).where(
                        cb.equal(root.get("clientId"), clientId),
                        cb.equal(root.get("productName"), productName),
                        cb.equal(root.get("mrp"), mrp)
                    );

        Long count = em.createQuery(cq).getSingleResult();
        return count > 0;
    }

}
