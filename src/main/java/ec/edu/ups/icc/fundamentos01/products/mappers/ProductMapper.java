package ec.edu.ups.icc.fundamentos01.products.mappers;

import java.util.List;

import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.entity.ProductEntity;

public class ProductMapper {

    public static ProductResponseDto toResponse(ProductEntity entity) {
        if (entity == null) {
            return null;
        }

        ProductResponseDto dto = new ProductResponseDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setPrice(entity.getPrice());
        dto.setStock(entity.getStock());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        if (entity.getOwner() != null) {
            ProductResponseDto.UserSummaryDto ownerDto = new ProductResponseDto.UserSummaryDto();

            ownerDto.setId(entity.getOwner().getId());
            ownerDto.setName(entity.getOwner().getName());
            ownerDto.setEmail(entity.getOwner().getEmail());

            dto.setOwner(ownerDto);
        }

        if (entity.getCategories() != null) {
            List<ProductResponseDto.CategorySummaryDto> categories = entity.getCategories()
                    .stream()
                    .map(ProductMapper::toCategorySummary)
                    .toList();

            dto.setCategories(categories);
        }

        return dto;
    }

    private static ProductResponseDto.CategorySummaryDto toCategorySummary(CategoryEntity category) {
        ProductResponseDto.CategorySummaryDto categoryDto = new ProductResponseDto.CategorySummaryDto();

        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());
        categoryDto.setDescription(category.getDescription());

        return categoryDto;
    }

    public static List<ProductResponseDto> toResponseList(List<ProductEntity> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(ProductMapper::toResponse)
                .toList();
    }
}