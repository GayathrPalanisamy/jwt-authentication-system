
package com.onedao.authentication.repository;

import com.onedao.authentication.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository
        extends JpaRepository<Product, Long> {
}
