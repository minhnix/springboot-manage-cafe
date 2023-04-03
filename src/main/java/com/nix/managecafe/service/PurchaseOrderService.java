package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.PurchaseOrder;
import com.nix.managecafe.model.Supplier;
import com.nix.managecafe.model.Warehouse;
import com.nix.managecafe.payload.request.PurchaseOrderRequest;
import com.nix.managecafe.payload.response.OrderResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.payload.response.PurchaseOrderResponse;
import com.nix.managecafe.repository.PurchaseOrderRepo;
import com.nix.managecafe.repository.WarehouseRepo;
import com.nix.managecafe.util.ModelMapper;
import com.nix.managecafe.util.ValidatePageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = {ResourceNotFoundException.class})
public class PurchaseOrderService {
    private final PurchaseOrderRepo purchaseOrderRepo;
    private final PurchaseOrderDetailService purchaseOrderDetailService;
    private final WarehouseRepo warehouseRepo;

    public PurchaseOrderService(PurchaseOrderRepo purchaseOrderRepo, PurchaseOrderDetailService purchaseOrderDetailService, WarehouseRepo warehouseRepo) {
        this.purchaseOrderRepo = purchaseOrderRepo;
        this.purchaseOrderDetailService = purchaseOrderDetailService;
        this.warehouseRepo = warehouseRepo;
    }

    public PurchaseOrderResponse getOne(Long purchaseOrderId) {
        return ModelMapper.mapPurchaseOrderToPurchaseOrderResponse(purchaseOrderRepo.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order", "id", purchaseOrderId)));
    }

    public PagedResponse<PurchaseOrderResponse> getAll(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PurchaseOrder> purchaseOrders = purchaseOrderRepo.findAll(pageable);

        List<PurchaseOrderResponse> purchaseOrderResponses = purchaseOrders.getContent().stream().map(
                ModelMapper::mapPurchaseOrderToPurchaseOrderResponse
        ).toList();

        return new PagedResponse<>(purchaseOrderResponses, purchaseOrders.getNumber(),
                purchaseOrders.getSize(), purchaseOrders.getTotalElements(), purchaseOrders.getTotalPages(), purchaseOrders.isLast());
    }

    public PurchaseOrder create(PurchaseOrderRequest purchaseOrder) {
        PurchaseOrder purchaseOrder1 = new PurchaseOrder();
        Supplier supplier = new Supplier();
        supplier.setId(purchaseOrder.getSupplierId());
        purchaseOrder1.setSupplier(supplier);
        purchaseOrder1.setPayment(purchaseOrder.getPayment());
        PurchaseOrder newPurchaseOrder = purchaseOrderRepo.save(purchaseOrder1);

        purchaseOrder.getPurchaseOrderDetails().forEach(
                purchaseOrderDetail -> {
                    purchaseOrderDetailService.create(purchaseOrderDetail, newPurchaseOrder);
                }
        );
        return newPurchaseOrder;
    }

    public void delete(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = purchaseOrderRepo.findById(purchaseOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order", "id", purchaseOrderId));
        purchaseOrder.getPurchaseOrderDetails().forEach(
                purchaseOrderDetail -> {
                    Warehouse warehouse = warehouseRepo.findByProductId(purchaseOrderDetail.getProduct().getId());
                    warehouse.setQuantity(warehouse.getQuantity() - purchaseOrderDetail.getQuantity());
                    warehouseRepo.save(warehouse);
                }
        );
        purchaseOrderRepo.delete(purchaseOrder);
    }
}
