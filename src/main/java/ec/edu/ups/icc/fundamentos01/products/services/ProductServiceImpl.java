package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dto.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.entity.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll()
                .stream()
                .filter(product -> !product.isDeleted())
                .map(ProductMapper::toResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto findOne(Long id) {
        ProductEntity product = findActiveProductById(id);
        return ProductMapper.toResponseDto(product);
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {
        ProductEntity product = ProductMapper.toEntity(dto);
        ProductEntity savedProduct = productRepository.save(product);
        return ProductMapper.toResponseDto(savedProduct);
    }

    @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {
        ProductEntity product = findActiveProductById(id);

        ProductMapper.updateEntity(product, dto);

        ProductEntity updatedProduct = productRepository.save(product);
        return ProductMapper.toResponseDto(updatedProduct);
    }

    @Override
    public ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto) {
        ProductEntity product = findActiveProductById(id);

        ProductMapper.partialUpdateEntity(product, dto);

        ProductEntity updatedProduct = productRepository.save(product);
        return ProductMapper.toResponseDto(updatedProduct);
    }

    @Override
    public void delete(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        if (product.isDeleted()) {
            throw new IllegalStateException("El producto ya fue eliminado");
        }

        product.setDeleted(true);
        productRepository.save(product);
    }

    private ProductEntity findActiveProductById(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        if (product.isDeleted()) {
            throw new IllegalStateException("El producto está eliminado");
        }

        return product;
    }
}