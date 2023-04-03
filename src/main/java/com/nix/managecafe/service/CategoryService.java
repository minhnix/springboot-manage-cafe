package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Category;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.repository.CategoryRepo;
import com.nix.managecafe.util.ValidatePageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepo categoryRepo;

    public PagedResponse<Category> getAll(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Category> categories = categoryRepo.findAll(pageable);

        return new PagedResponse<>(categories.getContent(), categories.getNumber(),
                categories.getSize(), categories.getTotalElements(), categories.getTotalPages(), categories.isLast());
    }

    public Category getOne(Long id) {
        return categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    public Category create(Category menuCategory) {
        return categoryRepo.save(menuCategory);
    }

    @CachePut(value = "categories")
    public Category update(Long id, Category category) {
        Category category1 = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        category1.setName(category.getName());

        return categoryRepo.save(category1);
    }

    @CacheEvict(value = "categories")
    public void delete(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepo.delete(category);
    }
}