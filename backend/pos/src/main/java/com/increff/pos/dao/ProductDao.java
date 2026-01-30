package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
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
        for (ProductEntity product : products) {
            em.persist(product);
        }
        return products;
    }

    public Optional<ProductEntity> findById(Integer id) {
        return Optional.ofNullable(em.find(ProductEntity.class, id));
    }

    public List<ProductEntity> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public List<ProductEntity> findAllById(List<Integer> ids) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(root)
                .where(root.get("id").in(ids));

        return em.createQuery(cq).getResultList();
    }

    public List<ProductEntity> findByClientId(Integer clientId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        cq.select(root)
                .where(cb.equal(root.get("clientId"), clientId));

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

    public boolean existsByBarcodeAndIdNot(String barcode, Integer id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ProductEntity> root = cq.from(ProductEntity.class);

        Predicate sameBarcode = cb.equal(root.get("barcode"), barcode);
        Predicate differentId = cb.notEqual(root.get("id"), id);

        cq.select(cb.count(root))
                .where(cb.and(sameBarcode, differentId));

        return em.createQuery(cq).getSingleResult() > 0;
    }

    public List<ProductEntity> findProductsForEnabledClients() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProductEntity> cq = cb.createQuery(ProductEntity.class);

        Root<ProductEntity> productRoot = cq.from(ProductEntity.class);
        Root<ClientEntity> clientRoot = cq.from(ClientEntity.class);

        Predicate joinCondition =
                cb.equal(productRoot.get("clientId"), clientRoot.get("id"));

        Predicate clientEnabled =
                cb.isTrue(clientRoot.get("enabled"));

        cq.select(productRoot)
                .where(cb.and(joinCondition, clientEnabled));

        return em.createQuery(cq).getResultList();
    }

}
