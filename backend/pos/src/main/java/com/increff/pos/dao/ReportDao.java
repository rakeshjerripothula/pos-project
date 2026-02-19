package com.increff.pos.dao;

import com.increff.pos.model.domain.OrderStatus;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.model.internal.DaySalesAggregate;
import com.increff.pos.model.internal.SalesReportRow;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ReportDao {

    @PersistenceContext
    private EntityManager em;

    public Page<SalesReportRow> selectByFiltersSalesPage(ZonedDateTime startDate, ZonedDateTime endDate, Integer clientId,
                                                         Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<SalesReportRow> dataQuery = buildSalesQuery(cb, startDate, endDate, clientId);
        TypedQuery<SalesReportRow> query = em.createQuery(dataQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<SalesReportRow> rows = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<OrderEntity> order = countQuery.from(OrderEntity.class);
        Root<OrderItemEntity> orderItem = countQuery.from(OrderItemEntity.class);
        Root<ProductEntity> product = countQuery.from(ProductEntity.class);

        List<Predicate> countPredicates = buildSalesPredicates(cb, order, orderItem, product, startDate, endDate, clientId);

        countQuery.select(cb.countDistinct(product.get("id"))).where(countPredicates.toArray(new Predicate[0]));
        Long total = em.createQuery(countQuery).getSingleResult();
        return new PageImpl<>(rows, pageable, total);
    }

    public List<SalesReportRow> selectAllSalesReport(ZonedDateTime startDate, ZonedDateTime endDate, Integer clientId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<SalesReportRow> cq = buildSalesQuery(cb, startDate, endDate, clientId);

        return em.createQuery(cq).getResultList();
    }

    public DaySalesAggregate selectDaySalesByDate(ZonedDateTime utcStart, ZonedDateTime utcEnd) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesAggregate> cq = cb.createQuery(DaySalesAggregate.class);

        Root<OrderEntity> order = cq.from(OrderEntity.class);
        Root<OrderItemEntity> item = cq.from(OrderItemEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(item.get("orderId"), order.get("id")));

        predicates.add(cb.equal(order.get("status"), OrderStatus.INVOICED));

        predicates.add(cb.greaterThanOrEqualTo(order.get("createdAt"), utcStart));
        predicates.add(cb.lessThan(order.get("createdAt"), utcEnd));

        Expression<Long> ordersCount = cb.countDistinct(order.get("id"));

        Expression<Long> itemsCount = cb.sumAsLong(item.get("quantity"));

        Expression<BigDecimal> revenue = cb.sum(cb.prod(item.get("quantity"), item.get("sellingPrice")));

        cq.select(cb.construct(DaySalesAggregate.class, ordersCount, itemsCount, revenue))
                .where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getSingleResult();
    }

    private CriteriaQuery<SalesReportRow> buildSalesQuery(CriteriaBuilder cb, ZonedDateTime startDate,
                                                          ZonedDateTime endDate, Integer clientId) {

        CriteriaQuery<SalesReportRow> cq = cb.createQuery(SalesReportRow.class);

        Root<OrderEntity> order = cq.from(OrderEntity.class);
        Root<OrderItemEntity> orderItem = cq.from(OrderItemEntity.class);
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        List<Predicate> predicates = buildSalesPredicates(cb, order, orderItem, product, startDate, endDate, clientId);

        Expression<Integer> quantityExpr = cb.sum(orderItem.get("quantity"));
        Expression<BigDecimal> revenueExpr = cb.sum(cb.prod(orderItem.get("quantity"), orderItem.get("sellingPrice")));

        cq.select(cb.construct(SalesReportRow.class, product.get("productName"), quantityExpr, revenueExpr))
                .where(predicates.toArray(new Predicate[0])).groupBy(product.get("id"), product.get("productName"))
                .orderBy(cb.desc(revenueExpr));

        return cq;
    }

    private List<Predicate> buildSalesPredicates(CriteriaBuilder cb, Root<OrderEntity> order,
                                                 Root<OrderItemEntity> orderItem, Root<ProductEntity> product,
                                                 ZonedDateTime startDate, ZonedDateTime endDate, Integer clientId) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(orderItem.get("orderId"), order.get("id")));
        predicates.add(cb.equal(orderItem.get("productId"), product.get("id")));
        predicates.add(cb.equal(order.get("status"), OrderStatus.INVOICED));
        predicates.add(cb.between(order.get("createdAt"), startDate, endDate));

        if (clientId != null) predicates.add(cb.equal(product.get("clientId"), clientId));

        return predicates;
    }

}
