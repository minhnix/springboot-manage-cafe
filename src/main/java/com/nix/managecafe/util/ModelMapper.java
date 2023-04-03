package com.nix.managecafe.util;

import com.nix.managecafe.model.*;
import com.nix.managecafe.payload.response.UserSummary;
import com.nix.managecafe.payload.response.*;

import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ModelMapper {

    public static MenuDetailResponse mapMenuToMenuResponse(Menu menu) {
        MenuDetailResponse menuDetailResponse = new MenuDetailResponse();
        menuDetailResponse.setCost(menu.getCost());
        menuDetailResponse.setId(menu.getId());
        menuDetailResponse.setCategory(menu.getCategory());
        menuDetailResponse.setName(menu.getName());
        menuDetailResponse.setImageUrl(menu.getImageUrl());
        menuDetailResponse.setDescription(menuDetailResponse.getDescription());
        menuDetailResponse.setMenuDetails(menu.getMenuDetails());
        return menuDetailResponse;
    }

    public static UserSummary mapUserToUserSummary(User user) {
        return new UserSummary(user.getId(), user.getUsername(), user.getEmail(), user.getRoles());
    }

    public static PurchaseOrderDetailResponse mapPurchaseOrderDetailToPurchaseOrderDetailResponse(PurchaseOrderDetail detail) {
        PurchaseOrderDetailResponse response = new PurchaseOrderDetailResponse();
        response.setId(detail.getId());
        response.setQuantity(detail.getQuantity());
        response.setImageUrl(detail.getProduct().getImageUrl());
        response.setProductCost(detail.getCost());
        response.setUnit(detail.getProduct().getUnit());
        response.setProductName(detail.getProduct().getName());
        response.setTotalCost(detail.getCost() * detail.getQuantity());
        return response;
    }

    public static PurchaseOrderResponse mapPurchaseOrderToPurchaseOrderResponse(PurchaseOrder purchaseOrder) {
        PurchaseOrderResponse purchaseOrderResponse = new PurchaseOrderResponse();
        purchaseOrderResponse.setId(purchaseOrder.getId());
        purchaseOrderResponse.setCreatedAt(purchaseOrder.getCreatedAt());
        purchaseOrderResponse.setSupplier(purchaseOrder.getSupplier());
        purchaseOrderResponse.setPaymentType(purchaseOrder.getPayment());
        AtomicReference<Long> totalCost = new AtomicReference<>(0L);
        purchaseOrderResponse.setOrderDetails(purchaseOrder.getPurchaseOrderDetails().stream().map(
                purchaseOrderDetail -> {
                    PurchaseOrderDetailResponse detail = ModelMapper.mapPurchaseOrderDetailToPurchaseOrderDetailResponse(purchaseOrderDetail);
                    totalCost.updateAndGet(v -> v + detail.getTotalCost());
                    return detail;
                }
        ).collect(Collectors.toList()));
        purchaseOrderResponse.setTotalCost(totalCost.get());
        return purchaseOrderResponse;
    }

    public static OrderDetailResponse mapOrderDetailToOrderDetailResponse(OrderDetail orderDetail) {
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
        orderDetailResponse.setId(orderDetail.getId());
        orderDetailResponse.setQuantity(orderDetail.getQuantity());
        orderDetailResponse.setMenu(orderDetail.getMenu().getName());
        orderDetailResponse.setImageUrl(orderDetail.getMenu().getImageUrl());
        orderDetailResponse.setMenuSize(orderDetail.getSize());
        orderDetailResponse.setMenuCost(orderDetail.getCost());
        orderDetailResponse.setTotalCost(orderDetail.getQuantity() * getCostBySize(orderDetail.getCost(), orderDetail.getSize()));
        return orderDetailResponse;
    }

    private static Long getCostBySize(Long cost, String size) {
        return switch (size) {
            case "SIZE_M" -> cost + AppConstants.PRICE_SIZE_M;
            case "SIZE_L" -> cost + AppConstants.PRICE_SIZE_L;
            case "SIZE_XL" -> cost + AppConstants.PRICE_SIZE_XL;
            default -> cost;
        };
    }

    public static OrderResponse mapOrderToOrderResponse(Order order) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setId(order.getId());
        orderResponse.setAddress(order.getAddress());
        orderResponse.setNote(order.getNote());
        orderResponse.setStatus(order.getStatus());
        orderResponse.setCustomer(new UserResponse(order.getCustomer().getId(), order.getCustomer().getUsername(), order.getCustomer().getFirstname(), order.getCustomer().getLastname()));
        if (order.getStaff() != null)
            orderResponse.setStaff(new UserResponse(order.getStaff().getId(), order.getStaff().getUsername(), order.getStaff().getFirstname(), order.getStaff().getLastname()));
        AtomicReference<Long> subTotalCost = new AtomicReference<>(0L);
        orderResponse.setDeliveryCost(order.getDeliveryCost());
        orderResponse.setAmountDiscount(order.getAmountDiscount());
        orderResponse.setOrderDetails(order.getOrderDetails().stream().map(
                orderDetail -> {
                    OrderDetailResponse orderDetailResponse = ModelMapper.mapOrderDetailToOrderDetailResponse(orderDetail);
                    subTotalCost.updateAndGet(v -> v + orderDetailResponse.getTotalCost());
                    return orderDetailResponse;
                }
        ).collect(Collectors.toList()));
        orderResponse.setSubTotalCost(subTotalCost.get());
        orderResponse.setTotalCost(subTotalCost.get() + order.getDeliveryCost() - order.getAmountDiscount());
        orderResponse.setCreatedAt(order.getCreatedAt());
        return orderResponse;
    }

    public static CartResponse mapCartToCartResponse(Cart cart) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setMenu(cart.getMenu());
        cartResponse.setQuantity(cart.getQuantity());
        cartResponse.setId(cart.getId());
        return cartResponse;
    }
}
