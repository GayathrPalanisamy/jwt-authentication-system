package com.onedao.authentication.service;

import com.onedao.authentication.dto.ProductRequest;
import com.onedao.authentication.entity.Product;
import com.onedao.authentication.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        logger.info("ProductService initialized");
    }

    public Product create(ProductRequest request) {

        logger.info("Creating product");

        Product product = new Product();
        copyRequest(request, product);

        Product savedProduct = productRepository.save(product);

        logger.info(
                "Product created successfully with ID: {}",
                savedProduct.getId()
        );

        return savedProduct;
    }

    public List<Product> getAll() {

        logger.debug("Retrieving all products");

        List<Product> products = productRepository.findAll();

        logger.info("Retrieved {} products", products.size());

        return products;
    }

    public Product getById(Long id) {

        logger.debug("Retrieving product with ID: {}", id);

        return productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Product not found with ID: {}", id);

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Product not found"
                    );
                });
    }

    public Product update(Long id, ProductRequest request) {

        logger.info("Updating product with ID: {}", id);

        Product product = getById(id);
        copyRequest(request, product);

        Product updatedProduct = productRepository.save(product);

        logger.info("Product updated successfully with ID: {}", id);

        return updatedProduct;
    }

    public void delete(Long id) {

        logger.info("Deleting product with ID: {}", id);

        Product product = getById(id);
        productRepository.delete(product);

        logger.info("Product deleted successfully with ID: {}", id);
    }

    private void copyRequest(
            ProductRequest request,
            Product product) {

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
    }
}
