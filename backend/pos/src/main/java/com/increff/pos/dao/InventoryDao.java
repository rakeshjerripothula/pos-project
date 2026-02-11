package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
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

    public List<InventoryEntity> findAllForEnabledClients() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> inventory = cq.from(InventoryEntity.class);

        Subquery<Integer> subquery = cq.subquery(Integer.class);
        Root<ProductEntity> product = subquery.from(ProductEntity.class);
        Root<ClientEntity> client = subquery.from(ClientEntity.class);

        subquery.select(cb.literal(1)).where(
                                                    cb.equal(product.get("id"), inventory.get("productId")),
                                                    cb.equal(client.get("id"), product.get("clientId")),
                                                    cb.isTrue(client.get("enabled"))
                                                );

        cq.select(inventory).distinct(true).where(cb.exists(subquery));

        return em.createQuery(cq).getResultList();
    }

    public Optional<InventoryEntity> findByProductId(Integer productId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);

        Root<InventoryEntity> root = cq.from(InventoryEntity.class);

        cq.select(root).where(cb.equal(root.get("productId"), productId));

        return em.createQuery(cq).setMaxResults(1).getResultList().stream().findFirst();

    }

    public InventoryEntity save(InventoryEntity inventory) {
        if (Objects.isNull(inventory.getId())) {
            em.persist(inventory);
            return inventory;
        }
        return em.merge(inventory);
    }


    public Page<InventoryEntity> searchForEnabledClients(String barcode, String productName, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> inventory = cq.from(InventoryEntity.class);

        Subquery<Integer> dataSub = cq.subquery(Integer.class);
        Root<ProductEntity> product = dataSub.from(ProductEntity.class);
        Root<ClientEntity> client = dataSub.from(ClientEntity.class);

        List<Predicate> subPredicates = new ArrayList<>();
        subPredicates.add(cb.equal(product.get("id"), inventory.get("productId")));
        subPredicates.add(cb.equal(client.get("id"), product.get("clientId")));
        subPredicates.add(cb.isTrue(client.get("enabled")));

        if (barcode != null && !barcode.trim().isEmpty()) {
            subPredicates.add(cb.equal(cb.lower(product.get("barcode")), barcode.toLowerCase().trim()));
        }

        if (productName != null && !productName.trim().isEmpty()) {
            subPredicates.add(cb.like(cb.lower(product.get("productName")), "%" + productName.toLowerCase().trim() + "%")
            );
        }

        dataSub.select(cb.literal(1)).where(subPredicates.toArray(new Predicate[0]));

        cq.select(inventory).where(cb.exists(dataSub));

        List<InventoryEntity> data = em.createQuery(cq).setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize()).getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<InventoryEntity> countInventory = countQuery.from(InventoryEntity.class);

        Subquery<Integer> countSub = countQuery.subquery(Integer.class);
        Root<ProductEntity> countProduct = countSub.from(ProductEntity.class);
        Root<ClientEntity> countClient = countSub.from(ClientEntity.class);

        List<Predicate> countSubPredicates = new ArrayList<>();
        countSubPredicates.add(cb.equal(countProduct.get("id"), countInventory.get("productId")));
        countSubPredicates.add(cb.equal(countClient.get("id"), countProduct.get("clientId")));
        countSubPredicates.add(cb.isTrue(countClient.get("enabled")));

        if (barcode != null && !barcode.trim().isEmpty()) {
            countSubPredicates.add(cb.equal(cb.lower(countProduct.get("barcode")), barcode.toLowerCase().trim()));
        }

        if (productName != null && !productName.trim().isEmpty()) {
            countSubPredicates.add(cb.like(
                            cb.lower(countProduct.get("productName")),
                            "%" + productName.toLowerCase().trim() + "%"
                    )
            );
        }

        countSub.select(cb.literal(1)).where(countSubPredicates.toArray(new Predicate[0]));

        countQuery.select(cb.count(countInventory)).where(cb.exists(countSub));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    public List<InventoryEntity> saveAll(List<InventoryEntity> inventories) {
        inventories.forEach(em::merge);
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

}
