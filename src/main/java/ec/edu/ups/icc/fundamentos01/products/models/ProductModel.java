package ec.edu.ups.icc.fundamentos01.products.models;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dto.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.entity.ProductEntity;

public class ProductModel {

    private Long id;

    private String name;

    private Double price;

    private Integer stock;

    private Set<CategoryEntity> categories = new HashSet<>();

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private boolean deleted;

    public ProductModel() {
    }

    public static ProductModel fromDto(CreateProductDto dto) {
        ProductModel product = new ProductModel();

        product.name = dto.getName();
        product.price = dto.getPrice();
        product.stock = dto.getStock();

        return product;
    }

    public static ProductModel fromEntity(ProductEntity entity) {
        ProductModel product = new ProductModel();

        product.id = entity.getId();
        product.name = entity.getName();
        product.price = entity.getPrice();
        product.stock = entity.getStock();
        product.categories = entity.getCategories();
        product.createdAt = entity.getCreatedAt();
        product.updatedAt = entity.getUpdatedAt();
        product.deleted = entity.isDeleted();

        return product;
    }

    public ProductEntity toEntity() {
        ProductEntity entity = new ProductEntity();

        if (this.id != null) {
            entity.setId(this.id);
        }

        entity.setName(this.name);
        entity.setPrice(this.price);
        entity.setStock(this.stock);
        entity.setCategories(this.categories);

        return entity;
    }

    public ProductResponseDto toResponseDto() {
        ProductResponseDto response = new ProductResponseDto();

        response.setId(this.id);
        response.setName(this.name);
        response.setPrice(this.price);
        response.setStock(this.stock);
        response.setCreatedAt(this.createdAt);
        response.setUpdatedAt(this.updatedAt);

        return response;
    }

    public void update(UpdateProductDto dto) {
        this.name = dto.getName();
        this.price = dto.getPrice();
        this.stock = dto.getStock();
    }

    public void partialUpdate(PartialUpdateProductDto dto) {
        if (dto.getName() != null) {
            this.name = dto.getName();
        }

        if (dto.getPrice() != null) {
            this.price = dto.getPrice();
        }

        if (dto.getStock() != null) {
            this.stock = dto.getStock();
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public Set<CategoryEntity> getCategories() {
        return categories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public void setCategories(Set<CategoryEntity> categories) {
        this.categories = categories;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}