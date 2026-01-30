package com.increff.pos.dao;

import com.increff.pos.domain.OrderStatus;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class ReportDao {

    @PersistenceContext
    private EntityManager em;

    public Page<SalesReportRow> getSalesReport(
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            Integer clientId,
            Pageable pageable
    ) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<SalesReportRow> cq = cb.createQuery(SalesReportRow.class);

        Root<OrderEntity> order = cq.from(OrderEntity.class);
        Root<OrderItemEntity> orderItem = cq.from(OrderItemEntity.class);
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(orderItem.get("orderId"), order.get("id")));
        predicates.add(cb.equal(orderItem.get("productId"), product.get("id")));
        predicates.add(cb.equal(order.get("status"), OrderStatus.INVOICED));
        predicates.add(cb.between(order.get("createdAt"), startDate, endDate));

        if (!Objects.isNull(clientId)) {
            predicates.add(cb.equal(product.get("clientId"), clientId));
        }

        Expression<Integer> quantityExpr = cb.sum(orderItem.get("quantity"));
        Expression<BigDecimal> revenueExpr = cb.sum(cb.prod(orderItem.get("quantity"), orderItem.get("sellingPrice")));

        cq.select(cb.construct(
                        SalesReportRow.class,
                        product.get("productName").alias("productName"),
                        quantityExpr,
                        revenueExpr
                ))
                .where(predicates.toArray(new Predicate[0]))
                .groupBy(product.get("id"), product.get("productName"))
                .orderBy(cb.desc(revenueExpr));

        TypedQuery<SalesReportRow> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<SalesReportRow> rows = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        Root<OrderEntity> countOrder = countQuery.from(OrderEntity.class);
        Root<OrderItemEntity> countOrderItem = countQuery.from(OrderItemEntity.class);
        Root<ProductEntity> countProduct = countQuery.from(ProductEntity.class);

        List<Predicate> countPredicates = new ArrayList<>();

        countPredicates.add(cb.equal(countOrderItem.get("orderId"), countOrder.get("id")));
        countPredicates.add(cb.equal(countOrderItem.get("productId"), countProduct.get("id")));
        countPredicates.add(cb.equal(countOrder.get("status"), OrderStatus.INVOICED));
        countPredicates.add(cb.between(countOrder.get("createdAt"), startDate, endDate));

        if (clientId != null) {
            countPredicates.add(cb.equal(countProduct.get("clientId"), clientId));
        }

        countQuery.select(cb.countDistinct(countProduct.get("id")))
                .where(countPredicates.toArray(new Predicate[0]));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(rows, pageable, total);
    }

    public DaySalesAggregate getDaySalesAggregate(LocalDate date) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DaySalesAggregate> cq =
                cb.createQuery(DaySalesAggregate.class);

        Root<OrderEntity> order = cq.from(OrderEntity.class);
        Root<OrderItemEntity> item = cq.from(OrderItemEntity.class);

        Predicate joinPredicate =
                cb.equal(item.get("orderId"), order.get("id"));

        Predicate statusPredicate =
                cb.equal(order.get("status"), OrderStatus.INVOICED);

        Predicate datePredicate =
                cb.equal(
                        cb.function("DATE", LocalDate.class, order.get("createdAt")),
                        date
                );

        Expression<Long> ordersCount =
                cb.countDistinct(order.get("id"));

        Expression<Long> itemsCount =
                cb.sumAsLong(item.get("quantity"));

        Expression<BigDecimal> revenue =
                cb.sum(
                        cb.prod(item.get("quantity"), item.get("sellingPrice"))
                );

        cq.select(cb.construct(
                        DaySalesAggregate.class,
                        ordersCount,
                        itemsCount,
                        revenue
                ))
                .where(cb.and(joinPredicate, statusPredicate, datePredicate));

        return em.createQuery(cq).getSingleResult();
    }

}
