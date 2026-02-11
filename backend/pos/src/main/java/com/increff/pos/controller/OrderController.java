package com.increff.pos.controller;

import com.increff.pos.dto.OrderDto;
import com.increff.pos.model.data.OrderData;
import com.increff.pos.model.data.OrderItemData;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderPageForm;
import com.increff.pos.model.data.OrderPageData;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @GetMapping
    public OrderPageData getOrders(OrderPageForm form) {
        return orderDto.getOrders(form);
    }

    @GetMapping("/{id}")
    public OrderData getById(@PathVariable Integer id) {
        return orderDto.getById(id);
    }

    @GetMapping("/{id}/items")
    public List<OrderItemData> getOrderItems(@PathVariable Integer id) {
        return orderDto.getOrderItems(id);
    }

    @PostMapping
    public OrderData create(@RequestBody @Valid OrderForm form) {
        return orderDto.create(form);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderData> cancel(@PathVariable Integer id) {
        OrderData orderData = orderDto.cancel(id);
        return ResponseEntity.ok(orderData);
    }

}
