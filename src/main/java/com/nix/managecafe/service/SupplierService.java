package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Address;
import com.nix.managecafe.model.Supplier;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.repository.AddressRepo;
import com.nix.managecafe.repository.SupplierRepo;
import com.nix.managecafe.util.ValidatePageable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = {ResourceNotFoundException.class})
public class SupplierService {
    private final SupplierRepo supplierRepo;
    private final AddressRepo addressRepo;
    private final EntityManager entityManager;

    public SupplierService(SupplierRepo supplierRepo, AddressRepo addressRepo, EntityManager entityManager) {
        this.supplierRepo = supplierRepo;
        this.addressRepo = addressRepo;
        this.entityManager = entityManager;
    }

    public Supplier create(Supplier supplier) {
        Address newAddress = addressRepo.save(supplier.getAddress());
        Supplier supplier1 = new Supplier();
        supplier1.setAddress(newAddress);
        supplier1.setName(supplier.getName());
        supplier1.setEmail(supplier.getEmail());
        supplier1.setPhoneNumber(supplier.getPhoneNumber());
        supplier1.setTaxCode(supplier.getTaxCode());

        return supplierRepo.save(supplier);
    }

    @CachePut("supplier")
    public Supplier update(Supplier supplier) {
        if (supplierRepo.findById(supplier.getId()).isEmpty()) {
            throw new ResourceNotFoundException("Supplier", "id", supplier.getId());
        }
        return supplierRepo.save(supplier);
    }

    @CachePut("supplier")
    public Supplier partialUpdate(Supplier supplier) {

        Supplier existingSupplier = supplierRepo.findById(supplier.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplier.getId()));

        if (supplier.getEmail() != null) {
            existingSupplier.setEmail(supplier.getEmail());
        }
        if (supplier.getName() != null) {
            existingSupplier.setName(supplier.getName());
        }
        if (supplier.getTaxCode() != null) {
            existingSupplier.setTaxCode(supplier.getTaxCode());
        }
        if (supplier.getPhoneNumber() != null) {
            existingSupplier.setPhoneNumber(supplier.getPhoneNumber());
        }
        Address address = existingSupplier.getAddress();

        if (supplier.getAddress() != null) {
            Address address1 = supplier.getAddress();
            if (address1.getCity() != null) {
                address.setCity(address1.getCity());
            }
            if (address1.getWard() != null) {
                address.setWard(address1.getWard());
            }
            if (address1.getRoad() != null) {
                address.setRoad(address1.getRoad());
            }
            if (address1.getDistrict() != null) {
                address.setDistrict(address1.getDistrict());
            }
            existingSupplier.setAddress(address);
        }
        return supplierRepo.save(existingSupplier);
    }

    @CacheEvict("supplier")
    public void delete(Long id) {
        Supplier existingSupplier = supplierRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        existingSupplier.getPurchaseOrders().forEach(purchaseOrder -> purchaseOrder.setSupplier(null));
        supplierRepo.delete(existingSupplier);
    }

    public Supplier getOne(Long supplierId) {
        Supplier supplier = supplierRepo.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));
        return supplier;
    }

    public PagedResponse<Supplier> getAll(int page, int size, String sortBy, String sortDir, String keyword) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Supplier> suppliers;
        if (keyword == null)
            suppliers = supplierRepo.findAll(pageable);
        else
            suppliers = supplierRepo.findByNameContains(pageable, keyword);

        return new PagedResponse<>(suppliers.getContent(), suppliers.getNumber(),
                suppliers.getSize(), suppliers.getTotalElements(), suppliers.getTotalPages(), suppliers.isLast());
    }

    public List<Supplier> searchByName(String name) {
        String[] names = name.split(" ");
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Supplier> cq = cb.createQuery(Supplier.class);
        Root<Supplier> root = cq.from(Supplier.class);

        Predicate[] predicates = new Predicate[names.length];
        for (int i = 0; i < names.length; i++) {
            predicates[i] = cb.like(root.get("name"), "%"+ names[i] +"%");
        }
        cq.select(root).where(cb.or(predicates));
        TypedQuery<Supplier> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}
