package com.increff.pos.dao;

import com.increff.pos.entity.ClientEntity;
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

    public List<ClientEntity> saveAll(List<ClientEntity> clients) {
        for (ClientEntity client : clients) {
            em.persist(client);
        }
        return clients;
    }

    public Optional<ClientEntity> findById(Integer clientId) {
        return Optional.ofNullable(em.find(ClientEntity.class, clientId));
    }

    public List<ClientEntity> findAll() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ClientEntity> cq = cb.createQuery(ClientEntity.class);
        Root<ClientEntity> root = cq.from(ClientEntity.class);
        cq.select(root);

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
