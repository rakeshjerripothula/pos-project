package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItemEntity;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class OrderItemApi {

    private final OrderItemDao orderItemDao;

    public OrderItemApi(OrderItemDao orderItemDao) {
        this.orderItemDao = orderItemDao;
    }

    public void createAll(List<OrderItemEntity> items) {
        orderItemDao.saveAll(items);
    }

    public List<OrderItemEntity> getByOrderId(Integer orderId) {
        return orderItemDao.findByOrderId(orderId);
    }
}
