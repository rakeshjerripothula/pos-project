package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.entity.OrderEntity;
import com.increff.pos.model.domain.OrderStatus;
import com.increff.pos.exception.ApiException;
import com.increff.pos.exception.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
@Transactional
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    public Page<OrderEntity> search(OrderStatus status, Integer clientId, ZonedDateTime start, ZonedDateTime end,
                                    Integer page, Integer pageSize) {

        Pageable pageable = PageRequest.of(page, pageSize);
        return orderDao.selectByFilters(status, clientId, start, end, pageable);
    }

    public OrderEntity create(OrderEntity order) {
        return orderDao.save(order);
    }

    public OrderEntity getCheckById(Integer orderId) {
        return orderDao.selectById(orderId)
                .orElseThrow(() -> new ApiException(
                        ApiStatus.NOT_FOUND, "Order not found", "orderId", "Order not found: " + orderId));
    }

    public OrderEntity updateStatus(OrderEntity order, OrderStatus status) {
        order.setStatus(status);
        return orderDao.save(order);
    }

}
