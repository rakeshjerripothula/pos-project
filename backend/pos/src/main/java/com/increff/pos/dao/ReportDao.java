package com.increff.pos.dao;

import com.increff.pos.entity.OrderEntity;
import com.increff.pos.entity.OrderItemEntity;
import com.increff.pos.entity.ProductEntity;
import com.increff.pos.model.domain.OrderStatus;
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

    public Page<SalesReportRow> selectByFiltersSalesPage(ZonedDateTime startDate, ZonedDateTime endDate,
                                                         Integer clientId, Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<SalesReportRow> dataQuery = buildSalesDataQuery(cb, startDate, endDate, clientId);

        TypedQuery<SalesReportRow> typedQuery = em.createQuery(dataQuery);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<SalesReportRow> rows = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = buildSalesCountQuery(cb, startDate, endDate, clientId);

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(rows, pageable, total);
    }

    public List<SalesReportRow> selectAllSalesReport(ZonedDateTime startDate, ZonedDateTime endDate, Integer clientId) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SalesReportRow> query = buildSalesDataQuery(cb, startDate, endDate, clientId);

        return em.createQuery(query).getResultList();
    }

    public DaySalesAggregate selectDaySalesByDate(ZonedDateTime utcStart, ZonedDateTime utcEnd) {

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<DaySalesAggregate> query = buildDaySalesQuery(cb, utcStart, utcEnd);

        return em.createQuery(query).getSingleResult();
    }

    private CriteriaQuery<SalesReportRow> buildSalesDataQuery(CriteriaBuilder cb, ZonedDateTime startDate,
                                                              ZonedDateTime endDate, Integer clientId) {

        CriteriaQuery<SalesReportRow> cq = cb.createQuery(SalesReportRow.class);

        SalesRoots roots = createSalesRoots(cq);

        List<Predicate> predicates = buildSalesPredicates(cb, roots, startDate, endDate, clientId);

        Expression<Integer> quantityExpr = cb.sum(roots.orderItem.get("quantity"));

        Expression<BigDecimal> revenueExpr = cb.sum(cb.prod(roots.orderItem.get("quantity"),
                roots.orderItem.get("sellingPrice")));

        cq.select(cb.construct(SalesReportRow.class, roots.product.get("productName"), quantityExpr, revenueExpr))
                .where(predicates.toArray(new Predicate[0])).groupBy(roots.product.get("id"), roots.product.get("productName"))
                .orderBy(cb.desc(revenueExpr));

        return cq;
    }

    private CriteriaQuery<Long> buildSalesCountQuery(CriteriaBuilder cb, ZonedDateTime startDate, ZonedDateTime endDate,
                                                     Integer clientId) {

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);

        SalesRoots roots = createSalesRoots(countQuery);

        List<Predicate> predicates = buildSalesPredicates(cb, roots, startDate, endDate, clientId);

        countQuery.select(cb.countDistinct(roots.product.get("id"))).where(predicates.toArray(new Predicate[0]));

        return countQuery;
    }

    private List<Predicate> buildSalesPredicates(CriteriaBuilder cb, SalesRoots roots, ZonedDateTime startDate,
                                                 ZonedDateTime endDate, Integer clientId) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(roots.orderItem.get("orderId"), roots.order.get("id")));

        predicates.add(cb.equal(roots.orderItem.get("productId"), roots.product.get("id")));

        predicates.add(cb.equal(roots.order.get("status"), OrderStatus.INVOICED));

        predicates.add(cb.between(roots.order.get("createdAt"), startDate, endDate));

        if (clientId != null) {
            predicates.add(cb.equal(roots.product.get("clientId"), clientId));
        }

        return predicates;
    }

    private SalesRoots createSalesRoots(CriteriaQuery<?> cq) {

        Root<OrderEntity> order = cq.from(OrderEntity.class);
        Root<OrderItemEntity> orderItem = cq.from(OrderItemEntity.class);
        Root<ProductEntity> product = cq.from(ProductEntity.class);

        return new SalesRoots(order, orderItem, product);
    }

    private CriteriaQuery<DaySalesAggregate> buildDaySalesQuery(CriteriaBuilder cb, ZonedDateTime utcStart,
                                                                ZonedDateTime utcEnd) {

        CriteriaQuery<DaySalesAggregate> cq = cb.createQuery(DaySalesAggregate.class);

        DaySalesRoots roots = createDaySalesRoots(cq);

        List<Predicate> predicates = buildDaySalesPredicates(cb, roots, utcStart, utcEnd);

        Expression<Long> ordersCount = cb.countDistinct(roots.order.get("id"));

        Expression<Long> itemsCount = cb.sumAsLong(roots.item.get("quantity"));

        Expression<BigDecimal> revenue = cb.sum(cb.prod(roots.item.get("quantity"), roots.item.get("sellingPrice")));

        cq.select(cb.construct(DaySalesAggregate.class, ordersCount, itemsCount, revenue))
                .where(predicates.toArray(new Predicate[0]));

        return cq;
    }

    private List<Predicate> buildDaySalesPredicates(CriteriaBuilder cb, DaySalesRoots roots, ZonedDateTime utcStart,
                                                    ZonedDateTime utcEnd) {

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(roots.item.get("orderId"), roots.order.get("id")));

        predicates.add(cb.equal(roots.order.get("status"), OrderStatus.INVOICED));

        predicates.add(cb.greaterThanOrEqualTo(roots.order.get("createdAt"), utcStart));

        predicates.add(cb.lessThan(roots.order.get("createdAt"), utcEnd));

        return predicates;
    }

    private DaySalesRoots createDaySalesRoots(CriteriaQuery<?> cq) {

        Root<OrderEntity> order = cq.from(OrderEntity.class);
        Root<OrderItemEntity> item = cq.from(OrderItemEntity.class);

        return new DaySalesRoots(order, item);
    }

    private record SalesRoots(Root<OrderEntity> order, Root<OrderItemEntity> orderItem, Root<ProductEntity> product) { }

    private record DaySalesRoots(Root<OrderEntity> order, Root<OrderItemEntity> item) { }
}
