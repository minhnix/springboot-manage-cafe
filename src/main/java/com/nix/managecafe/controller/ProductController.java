package com.nix.managecafe.controller;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.model.Menu;
import com.nix.managecafe.model.Product;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.payload.response.ProductResponse;
import com.nix.managecafe.service.ProductService;
import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/products")
@PreAuthorize("hasRole('ADMIN')")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) throws URISyntaxException {
        if (product.getId() != null) {
            throw new BadRequestException("A new product cannot already have an ID");
        }

        Product result = productService.create(product);
        return ResponseEntity
                .created(new URI("/api/v1/products/" + result.getId()))
                .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable(value = "id") final Long id, @Valid @RequestBody Product product) {
        if (product.getId() == null) {
            throw new BadRequestException("Id null");
        }
        if (!Objects.equals(id, product.getId())) {
            throw new BadRequestException("Invalid Id");
        }

        Product result = productService.update(product);
        return ResponseEntity
                .ok()
                .header("Id", product.getId().toString())
                .body(result);
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<Product> partialUpdateProduct(@PathVariable(value = "id") final Long id,@Valid @RequestBody Product product) {
        if (product.getId() == null) {
            throw new BadRequestException("Id null");
        }
        if (!Objects.equals(id, product.getId())) {
            throw new BadRequestException("Invalid Id");
        }

        Product result = productService.partialUpdate(product);

        return ResponseEntity.ok().header("Id", product.getId().toString()).body(result);
    }
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable(value = "id") final Long id) {
        productService.delete(id);
    }

    @GetMapping("/{id}")
    public ProductResponse getOne(@PathVariable("id") Long id) {
        return productService.getOne(id);
    }

    @GetMapping
    public PagedResponse<ProductResponse> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return productService.getAll(page, size, sortBy, sortDir, keyword);
    }

    @GetMapping("/amount")
    public long getAmountOfProduct() {
        return productService.getAmountOfProduct();
    }
}
