package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.domain.OrderStatus;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Service
@Transactional
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    public OrderEntity create(OrderEntity order) {
        return orderDao.save(order);
    }

    public OrderEntity getById(Integer orderId) {
        return orderDao.findById(orderId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Order not found: " + orderId)
                );
    }

    public OrderEntity update(OrderEntity order) {
        return orderDao.save(order);
    }

    public Page<OrderEntity> search(OrderStatus status, Integer clientId, ZonedDateTime start, ZonedDateTime end,
            Integer page, Integer pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);

        return orderDao.search(status, clientId, start, end, pageable);
    }

}
