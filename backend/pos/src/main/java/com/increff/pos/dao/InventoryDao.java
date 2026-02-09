package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class InventoryDao {

    @PersistenceContext
    private EntityManager em;

    public InventoryEntity save(InventoryEntity inventory) {
        if (Objects.isNull(inventory.getId())) {
            em.persist(inventory);
            return inventory;
        }
        return em.merge(inventory);
    }

    public Optional<InventoryEntity> findByProductId(Integer productId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> root = cq.from(InventoryEntity.class);

        cq.select(root).where(cb.equal(root.get("productId"), productId));

        List<InventoryEntity> result = em.createQuery(cq).getResultList();

        return result.stream().findFirst();
    }

    public Page<InventoryEntity> findAll(Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> root = cq.from(InventoryEntity.class);
        cq.select(root);

        List<InventoryEntity> data = em.createQuery(cq).setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InventoryEntity> countRoot = countQuery.from(InventoryEntity.class);
        countQuery.select(cb.count(countRoot));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    public List<InventoryEntity> saveAll(List<InventoryEntity> inventories) {
        int batchSize = 50;

        for (int i = 0; i < inventories.size(); i++) {
            em.persist(inventories.get(i));

            if (i > 0 && i % batchSize == 0) {
                em.flush();
                em.clear();
            }
        }
        return inventories;
    }


    public List<InventoryEntity> findByProductIds(List<Integer> productIds) {
        if (Objects.isNull(productIds) || productIds.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> root = cq.from(InventoryEntity.class);

        cq.select(root).where(root.get("productId").in(productIds));

        return em.createQuery(cq).getResultList();
    }

    public Page<InventoryEntity> findForEnabledClients(Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> inventoryRoot = cq.from(InventoryEntity.class);
        Root<ProductEntity> productRoot = cq.from(ProductEntity.class);
        Root<ClientEntity> clientRoot = cq.from(ClientEntity.class);

        Predicate invToProduct = cb.equal(inventoryRoot.get("productId"), productRoot.get("id"));

        Predicate productToClient = cb.equal(productRoot.get("clientId"), clientRoot.get("id"));

        Predicate clientEnabled = cb.isTrue(clientRoot.get("enabled"));

        cq.select(inventoryRoot)
                .where(cb.and(invToProduct, productToClient, clientEnabled));

        List<InventoryEntity> data = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InventoryEntity> countInv = countQuery.from(InventoryEntity.class);
        Root<ProductEntity> countProd = countQuery.from(ProductEntity.class);
        Root<ClientEntity> countClient = countQuery.from(ClientEntity.class);

        countQuery.select(cb.count(countInv))
                .where(
                        cb.and(
                                cb.equal(countInv.get("productId"), countProd.get("id")),
                                cb.equal(countProd.get("clientId"), countClient.get("id")),
                                cb.isTrue(countClient.get("enabled"))
                        )
                );

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    public Page<InventoryEntity> searchForEnabledClients(
            String barcode,
            String productName,
            Pageable pageable
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // ---------- DATA QUERY ----------
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> inventoryRoot = cq.from(InventoryEntity.class);
        Root<ProductEntity> productRoot = cq.from(ProductEntity.class);
        Root<ClientEntity> clientRoot = cq.from(ClientEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // joins
        predicates.add(cb.equal(inventoryRoot.get("productId"), productRoot.get("id")));
        predicates.add(cb.equal(productRoot.get("clientId"), clientRoot.get("id")));
        predicates.add(cb.isTrue(clientRoot.get("enabled")));

        // barcode (exact)
        if (barcode != null && !barcode.trim().isEmpty()) {
            predicates.add(
                    cb.equal(
                            cb.lower(productRoot.get("barcode")),
                            barcode.toLowerCase().trim()
                    )
            );
        }

        // productName (NORMAL contains search)
        if (productName != null && !productName.trim().isEmpty()) {
            predicates.add(
                    cb.like(
                            cb.lower(productRoot.get("productName")),
                            "%" + productName.toLowerCase().trim() + "%"
                    )
            );
        }

        cq.select(inventoryRoot)
                .where(predicates.toArray(new Predicate[0]));

        List<InventoryEntity> data = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // ---------- COUNT QUERY ----------
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        Root<InventoryEntity> countInv = countQuery.from(InventoryEntity.class);
        Root<ProductEntity> countProduct = countQuery.from(ProductEntity.class);
        Root<ClientEntity> countClient = countQuery.from(ClientEntity.class);

        List<Predicate> countPredicates = new ArrayList<>();

        countPredicates.add(cb.equal(countInv.get("productId"), countProduct.get("id")));
        countPredicates.add(cb.equal(countProduct.get("clientId"), countClient.get("id")));
        countPredicates.add(cb.isTrue(countClient.get("enabled")));

        if (barcode != null && !barcode.trim().isEmpty()) {
            countPredicates.add(
                    cb.equal(
                            cb.lower(countProduct.get("barcode")),
                            barcode.toLowerCase().trim()
                    )
            );
        }

        if (productName != null && !productName.trim().isEmpty()) {
            countPredicates.add(
                    cb.like(
                            cb.lower(countProduct.get("productName")),
                            "%" + productName.toLowerCase().trim() + "%"
                    )
            );
        }

        countQuery.select(cb.count(countInv))
                .where(countPredicates.toArray(new Predicate[0]));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    public List<InventoryEntity> findAllForEnabledClients() {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> inventoryRoot = cq.from(InventoryEntity.class);
        Root<ProductEntity> productRoot = cq.from(ProductEntity.class);
        Root<ClientEntity> clientRoot = cq.from(ClientEntity.class);

        Predicate invToProduct = cb.equal(inventoryRoot.get("productId"), productRoot.get("id"));
        Predicate productToClient = cb.equal(productRoot.get("clientId"), clientRoot.get("id"));
        Predicate clientEnabled = cb.isTrue(clientRoot.get("enabled"));

        cq.select(inventoryRoot)
                .where(cb.and(invToProduct, productToClient, clientEnabled));

        return em.createQuery(cq).getResultList();
    }

}
