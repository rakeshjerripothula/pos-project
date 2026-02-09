package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@Transactional
public class ClientDao {

    @PersistenceContext
    private EntityManager em;

    public ClientEntity save(ClientEntity client) {
        if (Objects.isNull(client.getId())) {
            em.persist(client);
            return client;
        }
        return em.merge(client);
    }

    public List<ClientEntity> selectAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);

        Root<ClientEntity> client = cq.from(ClientEntity.class);

        cq.select(client).orderBy(cb.asc(client.get("id")));

        TypedQuery<ClientEntity> query = em.createQuery(cq);
        return query.getResultList();
    }

    public Optional<ClientEntity> findById(Integer clientId) {
        return Optional.ofNullable(em.find(ClientEntity.class, clientId));
    }

    public Page<ClientEntity> findAll(Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        cq.select(root);

        List<ClientEntity> data = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ClientEntity> countRoot = countQuery.from(ClientEntity.class);
        countQuery.select(cb.count(countRoot));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    public Page<ClientEntity> searchByName(String clientName, Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        // -------- DATA QUERY --------
        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        if (clientName != null && !clientName.trim().isEmpty()) {
            predicates.add(
                    cb.like(
                            cb.lower(root.get("clientName")),
                            clientName.toLowerCase().trim() + "%"
                    )
            );
        }

        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("clientName")));

        List<ClientEntity> data = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // -------- COUNT QUERY --------
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ClientEntity> countRoot = countQuery.from(ClientEntity.class);

        countQuery.select(cb.count(countRoot));
        countQuery.where(predicates
                .stream()
                .map(p -> p) // same predicates, different root
                .toArray(Predicate[]::new)
        );

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }

    public List<Integer> findDisabledClientIds(List<Integer> clientIds) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Integer> cq = cb.createQuery(Integer.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);

        cq.select(root.get("id"))
                .where(
                        cb.and(
                                root.get("id").in(clientIds),
                                cb.isFalse(root.get("enabled"))
                        )
                );

        return em.createQuery(cq).getResultList();
    }

    public boolean existsByClientName(String clientName) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);

        cq.select(cb.count(root)).where(cb.equal(root.get("clientName"), clientName));

        Long count = em.createQuery(cq).getSingleResult();
        return count > 0;
    }

    public boolean existsByClientNameAndIdNot(String clientName, Integer clientId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);

        Predicate sameName = cb.equal(root.get("clientName"), clientName);
        Predicate differentId = cb.notEqual(root.get("id"), clientId);

        cq.select(cb.count(root)).where(cb.and(sameName, differentId));

        Long count = em.createQuery(cq).getSingleResult();
        return count > 0;
    }

}
