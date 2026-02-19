package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import com.increff.pos.entity.InventoryEntity;
import com.increff.pos.entity.ProductEntity;
import jakarta.persistence.criteria.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InventoryDao extends AbstractDao<InventoryEntity> {

    public InventoryDao() { super(InventoryEntity.class); }

    @Override
    protected boolean isNew(InventoryEntity entity) { return entity.getId() == null; }

    public Optional<InventoryEntity> selectByProductId(Integer productId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> root = cq.from(InventoryEntity.class);
        cq.select(root).where(cb.equal(root.get("productId"), productId));
        return em.createQuery(cq).setMaxResults(1).getResultList().stream().findFirst();
    }

    public List<InventoryEntity> selectByProductIds(List<Integer> productIds) {
        if (productIds == null || productIds.isEmpty()) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> root = cq.from(InventoryEntity.class);
        cq.select(root).where(root.get("productId").in(productIds));
        return em.createQuery(cq).getResultList();
    }

    public List<InventoryEntity> selectAllForEnabledClients() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> inventory = cq.from(InventoryEntity.class);
        cq.select(inventory).where(cb.exists(buildEnabledClientSubquery(cq, inventory, null, null)));
        return em.createQuery(cq).getResultList();
    }

    public Page<InventoryEntity> selectPagedForEnabledClients(String barcode, String productName, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<InventoryEntity> dataQuery = buildDataQuery(cb, barcode, productName);
        CriteriaQuery<Long> countQuery = buildCountQuery(cb, barcode, productName);

        return executePagedQuery(dataQuery, countQuery, pageable);
    }

    private CriteriaQuery<InventoryEntity> buildDataQuery(CriteriaBuilder cb, String barcode, String productName) {
        CriteriaQuery<InventoryEntity> cq = cb.createQuery(InventoryEntity.class);
        Root<InventoryEntity> inventory = cq.from(InventoryEntity.class);
        cq.select(inventory).where(cb.exists(buildEnabledClientSubquery(cq, inventory, barcode, productName)));
        return cq;
    }

    private CriteriaQuery<Long> buildCountQuery(CriteriaBuilder cb, String barcode, String productName) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<InventoryEntity> inventory = cq.from(InventoryEntity.class);
        cq.select(cb.count(inventory)).where(cb.exists(buildEnabledClientSubquery(cq, inventory, barcode, productName)));
        return cq;
    }

    private Subquery<Integer> buildEnabledClientSubquery(AbstractQuery<?> parent, Root<InventoryEntity> inventory,
                                                         String barcode, String productName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        Subquery<Integer> sub = parent.subquery(Integer.class);
        Root<ProductEntity> product = sub.from(ProductEntity.class);
        Root<ClientEntity> client = sub.from(ClientEntity.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(product.get("id"), inventory.get("productId")));
        predicates.add(cb.equal(client.get("id"), product.get("clientId")));
        predicates.add(cb.isTrue(client.get("enabled")));

        if (barcode != null && !barcode.trim().isEmpty())
            predicates.add(cb.equal(cb.lower(product.get("barcode")), barcode.toLowerCase().trim()));

        if (productName != null && !productName.trim().isEmpty())
            predicates.add(cb.like(cb.lower(product.get("productName")), "%" + productName.toLowerCase().trim() + "%"));

        sub.select(cb.literal(1)).where(predicates.toArray(new Predicate[0]));
        return sub;
    }
}
