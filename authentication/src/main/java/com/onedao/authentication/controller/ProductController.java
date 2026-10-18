package com.onedao.authentication.controller;

import com.onedao.authentication.dto.ProductRequest;
import com.onedao.authentication.entity.Product;
import com.onedao.authentication.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
        logger.info("ProductController initialized");
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(
            @Valid @RequestBody ProductRequest request) {

        logger.info("Product creation request received");

        Product product = productService.create(request);

        logger.info("Product created successfully with ID: {}",
                product.getId());

        return product;
    }

    @GetMapping
    public List<Product> getAll() {

        logger.info("Request received to retrieve all products");

        List<Product> products = productService.getAll();

        logger.info("Retrieved {} products", products.size());

        return products;
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {

        logger.info("Request received to retrieve product ID: {}", id);

        Product product = productService.getById(id);

        logger.info("Product retrieved successfully with ID: {}", id);

        return product;
    }

    @PutMapping("/{id}")
    public Product update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {

        logger.info("Product update request received for ID: {}", id);

        Product product = productService.update(id, request);

        logger.info("Product updated successfully with ID: {}", id);

        return product;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(
            @PathVariable Long id) {

        productService.delete(id);

        return ResponseEntity.ok(
                Map.of(
                        "message", "Product deleted successfully",
                        "id", id.toString()
                )
        );
    }
}
