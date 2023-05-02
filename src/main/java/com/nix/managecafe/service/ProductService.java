package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Menu;
import com.nix.managecafe.model.Product;
import com.nix.managecafe.model.Warehouse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.payload.response.ProductResponse;
import com.nix.managecafe.repository.ProductRepo;
import com.nix.managecafe.repository.WarehouseRepo;
import com.nix.managecafe.util.ModelMapper;
import com.nix.managecafe.util.ValidatePageable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = {ResourceNotFoundException.class})
public class ProductService {
    private final ProductRepo productRepo;
    private final WarehouseRepo warehouseRepo;
    private final EntityManager entityManager;

    public ProductService(ProductRepo productRepo, WarehouseRepo warehouseRepo, EntityManager entityManager) {
        this.productRepo = productRepo;
        this.warehouseRepo = warehouseRepo;
        this.entityManager = entityManager;
    }

    public Product create(Product product) {
        Product product1 = productRepo.save(product);
        Warehouse warehouse = new Warehouse();
        warehouse.setQuantity(0D);
        warehouse.setProduct(product1);
        warehouseRepo.save(warehouse);

        return product1;
    }

    @CachePut("product")
    public Product update(Product product) {
        Product existingProduct = productRepo.findById(product.getId()).orElseThrow(() -> new ResourceNotFoundException("Product", "id", product.getId()));
        existingProduct.setName(product.getName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setCost(product.getCost());
        existingProduct.setUnit(product.getUnit());
        existingProduct.setImageUrl(product.getImageUrl());

        return productRepo.save(existingProduct);
    }
    @CachePut("product")
    public Product partialUpdate(Product product) {
        Product existingProduct = productRepo.findById(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", product.getId()));

        if (product.getName() != null) {
            existingProduct.setName(product.getName());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getCost() != null) {
            existingProduct.setCost(product.getCost());
        }
        if (product.getUnit() != null) {
            existingProduct.setUnit(product.getUnit());
        }
        if (product.getImageUrl() != null) {
            existingProduct.setImageUrl(product.getImageUrl());
        }
        return productRepo.save(existingProduct);
    }

    @CacheEvict("product")
    public void delete(Long id) {
        Product product = productRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepo.delete(product);
    }

    public ProductResponse getOne(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return ModelMapper.mapProductToProductResponse(product);
    }

    public PagedResponse<ProductResponse> getAll(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);

        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("deletedProductFilter");
        filter.setParameter("isDeleted", false);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> products = productRepo.findAll(pageable);
        session.disableFilter("deletedProductFilter");

        List<ProductResponse> productResponses = products.getContent().stream().map(ModelMapper::mapProductToProductResponse).toList();

        return new PagedResponse<>(productResponses, products.getNumber(),
                products.getSize(), products.getTotalElements(), products.getTotalPages(), products.isLast());
    }

    public long getAmountOfProduct() {
        return productRepo.count();
    }

    public List<Product> getAllDeleted() {
        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("deletedProductFilter");
        filter.setParameter("isDeleted", true);
        List<Product> products =  productRepo.findAll();
        session.disableFilter("deletedProductFilter");
        return products;
    }
    public List<Product> searchByName(String name) {
        String[] names = name.split(" ");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Product> cq = cb.createQuery(Product.class);
        Root<Product> root = cq.from(Product.class);

        Predicate[] predicates = new Predicate[names.length];
        for (int i = 0; i < names.length; i++) {
            predicates[i] = cb.like(root.get("name"), "%"+ names[i] +"%");
        }
        Predicate deleted = cb.equal(root.get("deleted"), false);
        cq.select(root).where(cb.and(deleted), cb.or(predicates));
        TypedQuery<Product> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
