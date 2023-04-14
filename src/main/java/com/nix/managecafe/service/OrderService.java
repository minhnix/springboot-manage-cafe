package com.nix.managecafe.service;

import com.nix.managecafe.exception.AppException;
import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.exception.ForbiddenException;
import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.MenuDetail;
import com.nix.managecafe.model.Order;
import com.nix.managecafe.model.OrderDetail;
import com.nix.managecafe.model.Warehouse;
import com.nix.managecafe.model.enumname.RoleName;
import com.nix.managecafe.model.enumname.StatusName;
import com.nix.managecafe.payload.request.OrderDetailRequest;
import com.nix.managecafe.payload.request.OrderRequest;
import com.nix.managecafe.payload.response.OrderResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.repository.*;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.util.AppConstants;
import com.nix.managecafe.util.ModelMapper;
import com.nix.managecafe.util.ValidatePageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Service
public class OrderService {

    private final OrderRepo orderRepo;
    private final OrderDetailService orderDetailService;
    private final MenuDetailRepo menuDetailRepo;
    private final WarehouseRepo warehouseRepo;

    public OrderService(OrderRepo orderRepo, OrderDetailService orderDetailService, MenuDetailRepo menuDetailRepo, WarehouseRepo warehouseRepo) {
        this.orderRepo = orderRepo;
        this.orderDetailService = orderDetailService;
        this.menuDetailRepo = menuDetailRepo;
        this.warehouseRepo = warehouseRepo;
    }


    public OrderResponse getOne(Long orderId, UserPrincipal currentUser) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (Objects.equals(order.getStatus(), StatusName.PENDING) && !currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_CUSTOMER.name()))) {
            return ModelMapper.mapOrderToOrderResponse(order);
        }
        if (currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_ADMIN.name()))) {
            return ModelMapper.mapOrderToOrderResponse(order);
        }
        if (order.getStaff() != null && currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_STAFF.name())) && order.getStaff().getId().equals(currentUser.getId())) {
            return ModelMapper.mapOrderToOrderResponse(order);
        }
        if (currentUser.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_CUSTOMER.name())) &&  order.getCustomer().getId().equals(currentUser.getId())) {
            return ModelMapper.mapOrderToOrderResponse(order);
        }
        throw new ForbiddenException("Access Denied");
    }

    public PagedResponse<OrderResponse> getAll(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderRepo.findAll(pageable);

        List<OrderResponse> orderResponses = orders.getContent().stream().map(
                ModelMapper::mapOrderToOrderResponse
        ).toList();

        return new PagedResponse<>(orderResponses, orders.getNumber(),
                orders.getSize(), orders.getTotalElements(), orders.getTotalPages(), orders.isLast());
    }

    public PagedResponse<OrderResponse> getAllByStatus(int page, int size, String sortBy, String sortDir, String status) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderRepo.findAllByStatus(pageable, status);

        List<OrderResponse> orderResponses = orders.getContent().stream().map(
                ModelMapper::mapOrderToOrderResponse
        ).toList();

        return new PagedResponse<>(orderResponses, orders.getNumber(),
                orders.getSize(), orders.getTotalElements(), orders.getTotalPages(), orders.isLast());
    }

    public PagedResponse<OrderResponse> getAllByCreateAt(int page, int size, String sortBy, String sortDir, Long userId) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderRepo.findAllByCreatedBy(pageable, userId);

        List<OrderResponse> orderResponses = orders.getContent().stream().map(
                ModelMapper::mapOrderToOrderResponse
        ).toList();

        return new PagedResponse<>(orderResponses, orders.getNumber(),
                orders.getSize(), orders.getTotalElements(), orders.getTotalPages(), orders.isLast());
    }

    public PagedResponse<OrderResponse> getAllByStaffId(int page, int size, String sortBy, String sortDir, Long staffId) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Order> orders = orderRepo.findAllByStaffId(pageable, staffId);

        List<OrderResponse> orderResponses = orders.getContent().stream().map(
                ModelMapper::mapOrderToOrderResponse
        ).toList();

        return new PagedResponse<>(orderResponses, orders.getNumber(),
                orders.getSize(), orders.getTotalElements(), orders.getTotalPages(), orders.isLast());
    }

    @Transactional(rollbackFor = {ResourceNotFoundException.class})
    public Order create(OrderRequest orderRequest, UserPrincipal currentUser) {
        Order order = new Order();
        order.setStatus(StatusName.PENDING);
        order.setNote(orderRequest.getNote());
        order.setAddress(orderRequest.getAddress());
        order.setAmountDiscount(orderRequest.getAmountDiscount());
        order.setDeliveryCost(orderRequest.getDeliveryCost());
        order.setCustomer(currentUser.getUser());
        Order newOrder = orderRepo.save(order);
        for (OrderDetailRequest orderDetailRequest : orderRequest.getOrderDetails()) {
            newOrder.getOrderDetails().add(orderDetailService.create(orderDetailRequest, newOrder));
        }
        return newOrder;
    }

    public Order update(OrderRequest orderRequest, Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        for (OrderDetailRequest orderDetailRequest : orderRequest.getOrderDetails()) {
            orderDetailService.update(orderDetailRequest, order);
        }
        return order;
    }

    public void delete(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        orderRepo.delete(order);
    }

    public void payOrder(Long orderId, UserPrincipal currentUser) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (Objects.equals(order.getStatus(), StatusName.PAID)) {
            throw new BadRequestException("Đơn hàng này đã thanh toán");
        }
        if (Objects.equals(order.getStatus(), StatusName.PENDING)) {
            throw new BadRequestException("Bạn chưa nhận đơn hàng");
        }
        if (Objects.equals(order.getStatus(), StatusName.FAILED)) {
            throw new BadRequestException("Đơn hàng đã bị huỷ");
        }
        if (!order.getStaff().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("Đơn hàng này đã nhận bởi người khác. Bạn không thể thanh toán!");
        }
        order.setStatus(StatusName.PAID);
        orderRepo.save(order);
    }

    @Transactional(rollbackFor = {AppException.class, ResourceNotFoundException.class, Exception.class})
    public void receiveOrder(Long orderId, UserPrincipal currentUser) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        if (Objects.equals(order.getStatus(), StatusName.FAILED)) {
            throw new BadRequestException("Đơn hàng đã bị huỷ");
        }
        if (Objects.equals(order.getStatus(), StatusName.PAID)) {
            throw new BadRequestException("Đơn hàng này đã thanh toán. Không thể nhận đơn!");
        }
        if (!Objects.equals(order.getStatus(), StatusName.PENDING) && order.getStaff() != null && Objects.equals(order.getStaff().getId(), currentUser.getId())) {
            throw new BadRequestException("Đơn hàng này đã nhận");
        }
        if (!Objects.equals(order.getStatus(), StatusName.PENDING)) {
            throw new BadRequestException("Đơn hàng này đã nhận bởi người khác");
        }

        List<OrderDetail> orderDetails = order.getOrderDetails();
        HashMap<Long, Double> productQuantity = new HashMap<>();
        orderDetails.forEach(
                orderDetail -> {
                    List<MenuDetail> menuDetails = menuDetailRepo.findByMenuId(orderDetail.getMenu().getId());
                    menuDetails.forEach(
                            menuDetail -> {
                                long id = menuDetail.getProduct().getId();
                                if (productQuantity.containsKey(id)) {
                                    productQuantity.replace(id, productQuantity.get(id) + calculatorQuantity(orderDetail.getQuantity() * menuDetail.getQuantity(), orderDetail.getSize()));
                                } else {
                                    productQuantity.put(id, calculatorQuantity(orderDetail.getQuantity() * menuDetail.getQuantity(), orderDetail.getSize()));
                                }
                            }
                    );
                }
        );
        productQuantity.forEach((key, value) -> {
            Warehouse warehouse = warehouseRepo.findByProductId(key);
            if (warehouse.getQuantity() >= value) {
                warehouse.setQuantity(warehouse.getQuantity() - value);
            } else {
                order.setStatus(StatusName.FAILED);
                orderRepo.save(order);
                throw new BadRequestException("Not enough ingredients");
            }
            warehouseRepo.save(warehouse);
        });
        order.setStaff(currentUser.getUser());
        order.setStatus(StatusName.DELIVERING);
        orderRepo.save(order);
    }

    private double calculatorQuantity(double total, String size) {
        return switch (size) {
            case "SIZE_M" -> total * (1 + AppConstants.PERCENT_SIZE_M);
            case "SIZE_L" -> total * (1 + AppConstants.PERCENT_SIZE_L);
            case "SIZE_XL" -> total * (1 + AppConstants.PERCENT_SIZE_XL);
            default -> total;
        };
    }
}
