package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItemEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    public void createAll(List<OrderItemEntity> items) {
        orderItemDao.saveAll(items);
    }

    public List<OrderItemEntity> getByOrderId(Integer orderId) {
        return orderItemDao.findByOrderId(orderId);
    }
}
