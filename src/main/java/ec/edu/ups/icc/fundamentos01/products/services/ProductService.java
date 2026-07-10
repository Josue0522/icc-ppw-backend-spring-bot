package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;

import ec.edu.ups.icc.fundamentos01.core.dtos.PaginationDto;
import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductFilterByCategoryDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductFilterByUserDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dto.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;

public interface ProductService {

    List<ProductResponseDto> findAll();

    ProductResponseDto findOne(Long id);

    ProductResponseDto create(CreateProductDto dto, UserDetailsImpl currentUser);

    ProductResponseDto update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser);

    ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto, UserDetailsImpl currentUser);

    void delete(Long id, UserDetailsImpl currentUser);

    List<ProductResponseDto> findByUserId(Long userId);

    List<ProductResponseDto> findByCategoryId(Long categoryId);

    List<ProductResponseDto> findByUserIdWithFilters(
            Long userId,
            ProductFilterByUserDto filters
    );

    List<ProductResponseDto> findByCategoryIdWithFilters(
            Long categoryId,
            ProductFilterByCategoryDto filters
    );

    Page<ProductResponseDto> findAllPage(PaginationDto pagination);

    Slice<ProductResponseDto> findAllSlice(PaginationDto pagination, UserDetailsImpl currentUser);

    Page<ProductResponseDto> findByCategoryIdWithFiltersPage(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    );

    Slice<ProductResponseDto> findByCategoryIdWithFiltersSlice(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    );

    boolean validateName(String name);
}