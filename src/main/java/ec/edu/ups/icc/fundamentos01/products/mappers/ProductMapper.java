package ec.edu.ups.icc.fundamentos01.products.mappers;

import java.time.LocalDateTime;

import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.models.ProductModel;

public class ProductMapper {

    public static ProductModel toModel(CreateProductDto dto) {

        ProductModel model = new ProductModel();

        model.setName(dto.getName());
        model.setPrice(dto.getPrice());
        model.setStock(dto.getStock());
        model.setCreatedAt(LocalDateTime.now());

        return model;
    }

    public static ProductResponseDto toResponse(ProductModel model) {

        ProductResponseDto response = new ProductResponseDto();

        response.setId(model.getId());
        response.setName(model.getName());
        response.setPrice(model.getPrice());
        response.setStock(model.getStock());

        return response;
    }
}