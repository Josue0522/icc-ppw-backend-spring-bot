package ec.edu.ups.icc.fundamentos01.products.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponseDto {

    private Long id;

    private String name;

    private Double price;

    private Integer stock;

    private UserSummaryDto owner;

    private List<CategorySummaryDto> categories;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public ProductResponseDto() {
    }

    public ProductResponseDto(
            Long id,
            String name,
            Double price,
            Integer stock,
            UserSummaryDto owner,
            List<CategorySummaryDto> categories,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.owner = owner;
        this.categories = categories;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public UserSummaryDto getOwner() {
        return owner;
    }

    public List<CategorySummaryDto> getCategories() {
        return categories;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
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

    public void setOwner(UserSummaryDto owner) {
        this.owner = owner;
    }

    public void setCategories(List<CategorySummaryDto> categories) {
        this.categories = categories;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public static class UserSummaryDto {

        private Long id;

        private String name;

        private String email;

        public UserSummaryDto() {
        }

        public UserSummaryDto(Long id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class CategorySummaryDto {

        private Long id;

        private String name;

        private String description;

        public CategorySummaryDto() {
        }

        public CategorySummaryDto(Long id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        public Long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}