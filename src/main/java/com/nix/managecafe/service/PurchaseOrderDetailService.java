package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Product;
import com.nix.managecafe.model.PurchaseOrder;
import com.nix.managecafe.model.PurchaseOrderDetail;
import com.nix.managecafe.model.Warehouse;
import com.nix.managecafe.payload.request.PurchaseOrderDetailRequest;
import com.nix.managecafe.repository.ProductRepo;
import com.nix.managecafe.repository.PurchaseOrderDetailRepo;
import com.nix.managecafe.repository.WarehouseRepo;
import jakarta.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = {ResourceNotFoundException.class})
public class PurchaseOrderDetailService {
    private final PurchaseOrderDetailRepo purchaseOrderDetailRepo;
    private final WarehouseRepo warehouseRepo;
    private final ProductRepo productRepo;
    private final EntityManager entityManager;
    public PurchaseOrderDetailService(PurchaseOrderDetailRepo purchaseOrderDetailRepo, WarehouseRepo warehouseRepo, ProductRepo productRepo, EntityManager entityManager) {
        this.purchaseOrderDetailRepo = purchaseOrderDetailRepo;
        this.warehouseRepo = warehouseRepo;
        this.productRepo = productRepo;
        this.entityManager = entityManager;
    }

    public PurchaseOrderDetail create(PurchaseOrderDetailRequest purchaseOrderDetail, PurchaseOrder purchaseOrder) {
        PurchaseOrderDetail purchaseOrderDetail1 = new PurchaseOrderDetail();
        purchaseOrderDetail1.setPurchaseOrder(purchaseOrder);

        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("deletedProductFilter");
        filter.setParameter("isDeleted", false);
        Product product = productRepo.findById(purchaseOrderDetail.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", purchaseOrderDetail.getProductId()));
        session.disableFilter("deletedProductFilter");

        purchaseOrderDetail1.setProduct(product);
        purchaseOrderDetail1.setQuantity(purchaseOrderDetail.getQuantity());
        purchaseOrderDetail1.setCost(product.getCost());
        // set warehouse
        Warehouse warehouse = warehouseRepo.findByProductId(product.getId());
        warehouse.setQuantity(warehouse.getQuantity() + purchaseOrderDetail.getQuantity());
        warehouseRepo.save(warehouse);
        return purchaseOrderDetailRepo.save(purchaseOrderDetail1);
    }

}
