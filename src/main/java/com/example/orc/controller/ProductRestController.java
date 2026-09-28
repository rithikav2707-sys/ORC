package com.example.orc.controller;

import com.example.orc.exception.ResourceNotFoundException;
import com.example.orc.model.Product;
import com.example.orc.repository.ProductRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductRestController {
    private final ProductRepository products;
    public ProductRestController(ProductRepository products) { this.products = products; }
    @GetMapping public List<Product> list() { return products.findAll(); }
    @GetMapping("/{id}") public Product get(@PathVariable Long id) { return require(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Product create(@Valid @RequestBody Product product) {
        product.setType(product.getType().toUpperCase());
        return products.save(product);
    }
    @PutMapping("/{id}") public Product update(@PathVariable Long id, @Valid @RequestBody Product input) {
        Product current = require(id);
        current.setProductCode(input.getProductCode()); current.setProductName(input.getProductName());
        current.setType(input.getType().toUpperCase()); current.setUnit(input.getUnit()); current.setPrice(input.getPrice());
        current.setStockQuantity(input.getStockQuantity()); current.setDescription(input.getDescription()); current.setActive(input.isActive());
        return products.save(current);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { products.delete(require(id)); }
    private Product require(Long id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product " + id + " was not found"));
    }
}