package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dto.UpdateProductDto;

public interface ProductService {

    List<ProductResponseDto> findAll();

    Object findOne(Long id);

    ProductResponseDto create(CreateProductDto dto);

    Object update(Long id, UpdateProductDto dto);

    Object partialUpdate(Long id, PartialUpdateProductDto dto);

    Object delete(Long id);
}