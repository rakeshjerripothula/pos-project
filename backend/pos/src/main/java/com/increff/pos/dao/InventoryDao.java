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
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class InventoryDao {

    @PersistenceContext
    private EntityManager em;

    // ---------- Save / Update ----------

    public InventoryEntity save(InventoryEntity inventory) {
        if (Objects.isNull(inventory.getId())) {
            em.persist(inventory);
            return inventory;
        }
        return em.merge(inventory);
    }

    // ---------- Find by Product ID ----------

    public Optional<InventoryEntity> findByProductId(Integer productId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq =
                cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> root = cq.from(InventoryEntity.class);

        cq.select(root)
                .where(cb.equal(root.get("productId"), productId));

        List<InventoryEntity> result =
                em.createQuery(cq).getResultList();

        return result.stream().findFirst();
    }

    // ---------- Find All ----------

    public List<InventoryEntity> findAll() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq =
                cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> root = cq.from(InventoryEntity.class);
        cq.select(root);

        return em.createQuery(cq).getResultList();
    }

    public List<InventoryEntity> saveAll(List<InventoryEntity> inventories) {
        return inventories.stream().map(this::save).toList();
    }

    public List<InventoryEntity> findByProductIds(List<Integer> productIds) {
        if (Objects.isNull(productIds) || productIds.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> root = cq.from(InventoryEntity.class);

        cq.select(root)
                .where(root.get("productId").in(productIds));

        return em.createQuery(cq).getResultList();
    }

    // ---------- JOIN: Inventory + Product + Client ----------

    public List<InventoryEntity> findInventoryForEnabledClients() {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq =
                cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> inventoryRoot =
                cq.from(InventoryEntity.class);
        Root<ProductEntity> productRoot =
                cq.from(ProductEntity.class);
        Root<ClientEntity> clientRoot =
                cq.from(ClientEntity.class);

        Predicate invToProd =
                cb.equal(
                        inventoryRoot.get("productId"),
                        productRoot.get("id")
                );

        Predicate prodToClient =
                cb.equal(
                        productRoot.get("clientId"),
                        clientRoot.get("id")
                );

        Predicate clientEnabled =
                cb.isTrue(clientRoot.get("enabled"));

        cq.select(inventoryRoot)
                .where(cb.and(invToProd, prodToClient, clientEnabled));

        return em.createQuery(cq).getResultList();
    }
}
