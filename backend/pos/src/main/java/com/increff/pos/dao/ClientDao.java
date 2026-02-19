package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ClientDao extends AbstractDao<ClientEntity> {

    public ClientDao() {
        super(ClientEntity.class);
    }

    @Override
    protected boolean isNew(ClientEntity entity) {
        return entity.getId() == null;
    }

    public List<ClientEntity> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        cq.select(root);
        return em.createQuery(cq).getResultList();
    }

    public Page<ClientEntity> selectByFilters(String clientName, Boolean enabled, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientEntity> dataQuery = buildSelectQuery(cb, clientName, enabled);
        CriteriaQuery<Long> countQuery = buildCountQuery(cb, clientName, enabled);
        return executePagedQuery(dataQuery, countQuery, pageable);
    }

    public List<Integer> selectIdsByIdInAndEnabled(List<Integer> ids, Boolean enabled) {
        if (ids == null || ids.isEmpty()) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(root.get("id").in(ids));
        if (enabled != null) predicates.add(cb.equal(root.get("enabled"), enabled));
        cq.select(root.get("id")).where(predicates.toArray(new Predicate[0]));
        return em.createQuery(cq).getResultList();
    }

    public List<Integer> selectExistingIds(List<Integer> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) return List.of();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        cq.select(root.get("id")).where(root.get("id").in(clientIds));
        return em.createQuery(cq).getResultList();
    }

    public Optional<ClientEntity> selectByClientName(String clientName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        cq.select(root).where(cb.equal(root.get("clientName"), clientName));
        return em.createQuery(cq).getResultList().stream().findFirst();
    }

    public Optional<ClientEntity> selectByClientNameExcludingId(String clientName, Integer clientId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        cq.select(root).where(cb.and(
                cb.equal(root.get("clientName"), clientName),
                cb.notEqual(root.get("id"), clientId)
        ));
        return em.createQuery(cq).getResultList().stream().findFirst();
    }

    private CriteriaQuery<ClientEntity> buildSelectQuery(CriteriaBuilder cb, String clientName, Boolean enabled) {
        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        List<Predicate> predicates = buildPredicates(cb, root, clientName, enabled);
        cq.select(root).where(predicates.toArray(new Predicate[0]));
        return cq;
    }

    private CriteriaQuery<Long> buildCountQuery(CriteriaBuilder cb, String clientName, Boolean enabled) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        List<Predicate> predicates = buildPredicates(cb, root, clientName, enabled);
        cq.select(cb.count(root)).where(predicates.toArray(new Predicate[0]));
        return cq;
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<ClientEntity> root, String clientName, Boolean enabled) {
        List<Predicate> predicates = new ArrayList<>();
        if (clientName != null && !clientName.trim().isEmpty())
            predicates.add(cb.like(root.get("clientName"), clientName + "%"));
        if (enabled != null)
            predicates.add(cb.equal(root.get("enabled"), enabled));
        return predicates;
    }
}