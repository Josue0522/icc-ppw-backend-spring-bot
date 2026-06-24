package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dto.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.models.ProductModel;
import ec.edu.ups.icc.fundamentos01.products.dto.core.*;;

@Service
public class ProductServiceImpl implements ProductService {

    private List<ProductModel> products = new ArrayList<>();
    private Long currentId = 1L;

    @Override
    public List<ProductResponseDto> findAll() {
        return products.stream()
                .map(ProductMapper::toResponse)
                .toList();
    }

    @Override
    public Object findOne(Long id) {

        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .map(product -> (Object) ProductMapper.toResponse(product))
                .orElseGet(() -> new ErrorResponseDto("Product not found"));
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {

        ProductModel product = ProductMapper.toModel(dto);

        product.setId(currentId);
        currentId++;

        products.add(product);

        return ProductMapper.toResponse(product);
    }

    @Override
    public Object update(Long id, UpdateProductDto dto) {

        ProductModel product = products.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (product == null) {
            return new ErrorResponseDto("Product not found");
        }

        product.setName(dto.getName());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());

        return ProductMapper.toResponse(product);
    }

    @Override
    public Object partialUpdate(Long id, PartialUpdateProductDto dto) {

        ProductModel product = products.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (product == null) {
            return new ErrorResponseDto("Product not found");
        }

        if (dto.getName() != null) {
            product.setName(dto.getName());
        }

        if (dto.getPrice() != null) {
            product.setPrice(dto.getPrice());
        }

        if (dto.getStock() != null) {
            product.setStock(dto.getStock());
        }

        return ProductMapper.toResponse(product);
    }

    @Override
    public Object delete(Long id) {

        boolean removed = products.removeIf(product -> product.getId().equals(id));

        if (!removed) {
            return new ErrorResponseDto("Product not found");
        }

        return new Object() {
            public String message = "Deleted successfully";
        };
    }
}