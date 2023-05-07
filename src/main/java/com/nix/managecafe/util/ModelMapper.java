package com.nix.managecafe.util;

import com.nix.managecafe.model.*;
import com.nix.managecafe.payload.response.UserSummary;
import com.nix.managecafe.payload.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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

    public static ProductResponse mapProductToProductResponse(Product product) {
        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(product.getId());
        productResponse.setUnit(product.getUnit());
        productResponse.setName(product.getName());
        if (product.getWarehouse() == null) {
            productResponse.setQuantity(0D);
        } else {
            productResponse.setQuantity(product.getWarehouse().getQuantity());
        }
        productResponse.setCost(product.getCost());
        productResponse.setImageUrl(product.getImageUrl());
        return productResponse;
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
        double totalCost = 0;
        List<PurchaseOrderDetailResponse> purchaseOrderDetailResponses = new ArrayList<>();
        for (PurchaseOrderDetail purchaseOrderDetail : purchaseOrder.getPurchaseOrderDetails()) {
            PurchaseOrderDetailResponse detail = ModelMapper.mapPurchaseOrderDetailToPurchaseOrderDetailResponse(purchaseOrderDetail);
            totalCost += detail.getTotalCost();
            purchaseOrderDetailResponses.add(detail);
        }
        purchaseOrderResponse.setPurchaseOrderDetails(purchaseOrderDetailResponses);
        purchaseOrderResponse.setTotalCost(totalCost);
        return purchaseOrderResponse;
    }

    public static OrderDetailResponse mapOrderDetailToOrderDetailResponse(OrderDetail orderDetail) {
        OrderDetailResponse orderDetailResponse = new OrderDetailResponse();
        orderDetailResponse.setId(orderDetail.getId());
        orderDetailResponse.setMenuId(orderDetail.getMenu().getId());
        orderDetailResponse.setQuantity(orderDetail.getQuantity());
        orderDetailResponse.setMenu(orderDetail.getMenu().getName());
        orderDetailResponse.setImageUrl(orderDetail.getMenu().getImageUrl());
        orderDetailResponse.setMenuSize(orderDetail.getSize());
        orderDetailResponse.setMenuCost(orderDetail.getCost());
        orderDetailResponse.setTotalCost(orderDetail.getQuantity() * orderDetail.getCost());
        return orderDetailResponse;
    }

    public static long getCostBySize(long cost, String size) {
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
        orderResponse.setStaffFullName(order.getStaffFullName());
        orderResponse.setCustomer(new UserResponse(order.getCustomer().getId(), order.getCustomer().getUsername(), order.getCustomer().getFirstname(), order.getCustomer().getLastname(), order.getCustomer().getEmail(), order.getCustomer().getPhoneNumber()));
        if (order.getStaff() != null)
            orderResponse.setStaff(new UserResponse(order.getStaff().getId(), order.getStaff().getUsername(), order.getStaff().getFirstname(), order.getStaff().getLastname(), order.getStaff().getEmail(), order.getStaff().getPhoneNumber()));
        orderResponse.setDeliveryCost(order.getDeliveryCost());
        orderResponse.setAmountDiscount(order.getAmountDiscount());
        long subTotalCost = 0;
        List<OrderDetailResponse> orderDetailResponses = new ArrayList<>();
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            OrderDetailResponse orderDetailResponse = ModelMapper.mapOrderDetailToOrderDetailResponse(orderDetail);
            subTotalCost += orderDetailResponse.getTotalCost();
            orderDetailResponses.add(orderDetailResponse);
        }
        orderResponse.setOrderDetails(orderDetailResponses);
        orderResponse.setSubTotalCost(subTotalCost);
        orderResponse.setTotalCost(subTotalCost + order.getDeliveryCost() - order.getAmountDiscount());
        orderResponse.setCreatedAt(order.getCreatedAt());
        return orderResponse;
    }

    public static CartResponse mapCartToCartResponse(Cart cart) {
        CartResponse cartResponse = new CartResponse();
        cartResponse.setName(cart.getMenu().getName());
        cartResponse.setQuantity(cart.getQuantity());
        cartResponse.setId(cart.getId());
        cartResponse.setSize(cart.getSize());
        cartResponse.setDeleted(cart.getMenu().isDeleted());
        cartResponse.setImageUrl(cart.getMenu().getImageUrl());
        cartResponse.setMenuId(cart.getMenu().getId());
        cartResponse.setMenuCost(getCostBySize(cart.getMenu().getCost(), cartResponse.getSize()));
        cartResponse.setTotalCost(cart.getQuantity() * getCostBySize(cart.getMenu().getCost(), cartResponse.getSize()));
        return cartResponse;
    }

    public static StaffResponse mapTimeSheetToStaffResponse(TimeSheet timeSheet) {
        StaffResponse staffResponse = new StaffResponse();
        staffResponse.setEmail(timeSheet.getUser().getEmail());
        staffResponse.setLastname(timeSheet.getUser().getLastname());
        staffResponse.setFirstname(timeSheet.getUser().getFirstname());
        staffResponse.setUsername(timeSheet.getUser().getUsername());
        staffResponse.setUserId(timeSheet.getUser().getId());
        staffResponse.setPhoneNumber(timeSheet.getUser().getPhoneNumber());
        staffResponse.setSalary(timeSheet.getSalary());
        staffResponse.setShiftId(timeSheet.getShift().getId());
        staffResponse.setShift(timeSheet.getShift().getName());
        staffResponse.setStartDate(timeSheet.getStartDate());
        return staffResponse;
    }
}
