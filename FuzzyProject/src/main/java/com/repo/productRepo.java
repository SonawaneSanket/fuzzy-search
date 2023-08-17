package com.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.product.Product;

public interface productRepo extends JpaRepository<Product, Long> {
	List<Product> findByNameFuzzy(String searchTerm);

}
