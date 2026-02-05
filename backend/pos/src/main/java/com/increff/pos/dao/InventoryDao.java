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

}
