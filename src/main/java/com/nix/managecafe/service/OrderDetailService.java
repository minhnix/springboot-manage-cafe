package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Menu;
import com.nix.managecafe.model.Order;
import com.nix.managecafe.model.OrderDetail;
import com.nix.managecafe.payload.request.OrderDetailRequest;
import com.nix.managecafe.repository.MenuRepo;
import com.nix.managecafe.repository.OrderDetailRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderDetailService {
    private final OrderDetailRepo orderDetailRepo;

    private final MenuRepo menuRepo;

    public OrderDetailService(MenuRepo menuRepo, OrderDetailRepo orderDetailRepo) {
        this.menuRepo = menuRepo;
        this.orderDetailRepo = orderDetailRepo;
    }

    @Transactional(rollbackFor = {ResourceNotFoundException.class})
    public OrderDetail create(OrderDetailRequest orderDetailRequest, Order order) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setQuantity(orderDetailRequest.getQuantity());
        Long menuId = orderDetailRequest.getMenuId();
        Menu menu = menuRepo.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", menuId));
        orderDetail.setMenu(menu);
        orderDetail.setCost(menu.getCost());
        orderDetail.setOrder(order);
        orderDetail.setSize(orderDetailRequest.getSize());
        return orderDetailRepo.save(orderDetail);
    }

    public OrderDetail update(OrderDetailRequest orderDetailRequest, Order order) {
        OrderDetail orderDetail = orderDetailRepo.getOrderDetailsByOrderIdAndMenuId(order.getId(), orderDetailRequest.getMenuId());
        if (orderDetail == null) {
            orderDetail = new OrderDetail();
            Menu menu = menuRepo.findById(orderDetailRequest.getMenuId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu", "id", orderDetailRequest.getMenuId()));
            orderDetail.setMenu(menu);
            orderDetail.setOrder(order);
        }
        orderDetail.setQuantity(orderDetailRequest.getQuantity());

        return orderDetailRepo.save(orderDetail);
    }
}
