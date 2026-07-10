package ec.edu.ups.icc.fundamentos01.products.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.core.dtos.PaginationDto;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.BadRequestException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductFilterByCategoryDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductFilterByUserDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dto.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.entity.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.mappers.ProductMapper;
import ec.edu.ups.icc.fundamentos01.products.repository.ProductRepository;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import ec.edu.ups.icc.fundamentos01.users.entity.UserEntity;
import ec.edu.ups.icc.fundamentos01.users.repository.UserRepository;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    public ProductServiceImpl(
            ProductRepository productRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAll() {
        List<ProductEntity> list = productRepository.findByDeletedFalse();
        return ProductMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto findOne(Long id) {
        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        return ProductMapper.toResponse(entity);
    }

    @Override
    @Transactional
    public ProductResponseDto create(CreateProductDto dto, UserDetailsImpl currentUser) {

        UserEntity owner = findCurrentUserEntity(currentUser);

        Set<CategoryEntity> categories = validateAndGetCategories(dto.getCategoryIds());

        if (productRepository.findByNameIgnoreCaseAndDeletedFalse(dto.getName()).isPresent()) {
            throw new ConflictException("Product name already registered");
        }

        ProductEntity entity = new ProductEntity();

        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setOwner(owner);
        entity.setCategories(categories);

        ProductEntity savedEntity = productRepository.save(entity);

        return ProductMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional
    public ProductResponseDto update(Long id, UpdateProductDto dto, UserDetailsImpl currentUser) {

        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        validateOwnership(entity, currentUser);

        Set<CategoryEntity> categories = validateAndGetCategories(dto.getCategoryIds());

        entity.setName(dto.getName());
        entity.setPrice(dto.getPrice());
        entity.setStock(dto.getStock());
        entity.setCategories(categories);

        ProductEntity savedEntity = productRepository.save(entity);

        return ProductMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional
    public ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto, UserDetailsImpl currentUser) {

        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        validateOwnership(entity, currentUser);

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }

        if (dto.getPrice() != null) {
            entity.setPrice(dto.getPrice());
        }

        if (dto.getStock() != null) {
            entity.setStock(dto.getStock());
        }

        if (dto.getCategoryIds() != null) {
            Set<CategoryEntity> categories = validateAndGetCategories(dto.getCategoryIds());
            entity.setCategories(categories);
        }

        ProductEntity savedEntity = productRepository.save(entity);

        return ProductMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional
    public void delete(Long id, UserDetailsImpl currentUser) {
        ProductEntity entity = productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Product not found"));

        validateOwnership(entity, currentUser);

        entity.setDeleted(true);
        productRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findByUserId(Long userId) {
        if (!userRepository.existsByIdAndDeletedFalse(userId)) {
            throw new NotFoundException("User not found");
        }

        List<ProductEntity> list = productRepository.findByOwner_IdAndDeletedFalse(userId);

        return ProductMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findByCategoryId(Long categoryId) {
        if (!categoryRepository.existsByIdAndDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        List<ProductEntity> list = productRepository.findByCategoryId(categoryId);

        return ProductMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findByUserIdWithFilters(
            Long userId,
            ProductFilterByUserDto filters
    ) {
        if (!userRepository.existsByIdAndDeletedFalse(userId)) {
            throw new NotFoundException("User not found");
        }

        validateUserFilters(filters);

        String name = normalizeName(filters.getName());

        List<ProductEntity> list = productRepository.findByOwnerIdWithFilters(
                userId,
                name,
                filters.getMinPrice(),
                filters.getMaxPrice(),
                filters.getCategoryId()
        );

        return ProductMapper.toResponseList(list);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findByCategoryIdWithFilters(
            Long categoryId,
            ProductFilterByCategoryDto filters
    ) {
        if (!categoryRepository.existsByIdAndDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        validateCategoryFilters(filters);

        String name = normalizeName(filters.getName());

        List<ProductEntity> list = productRepository.findByCategoryIdWithFilters(
                categoryId,
                name,
                filters.getMinPrice(),
                filters.getMaxPrice(),
                filters.getUserId()
        );

        return ProductMapper.toResponseList(list);
    }

    /*
     * Retorna productos activos usando Page.
     *
     * Incluye metadatos completos:
     * totalElements, totalPages, number, size, first, last.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAllPage(PaginationDto pagination) {

        Pageable pageable = createPageable(pagination);

        return productRepository.findActivePage(pageable)
                .map(ProductMapper::toResponse);
    }

    /*
     * Retorna productos activos usando Slice.
     *
     * Solo los del usuario autenticado (owner = token JWT).
     */
    @Override
    @Transactional(readOnly = true)
    public Slice<ProductResponseDto> findAllSlice(PaginationDto pagination, UserDetailsImpl currentUser) {

        Pageable pageable = createPageable(pagination);

        return productRepository.findActiveSliceByOwner(currentUser.getId(), pageable)
                .map(ProductMapper::toResponse);
    }

    /*
     * Retorna productos activos de una categoría usando Page.
     *
     * Mantiene los filtros de la práctica anterior y agrega paginación.
     */
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findByCategoryIdWithFiltersPage(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    ) {
        if (!categoryRepository.existsByIdAndDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        validateCategoryFilters(filters);

        String name = normalizeName(filters.getName());

        Pageable pageable = createPageable(pagination);

        return productRepository.findByCategoryIdWithFiltersPage(
                        categoryId,
                        name,
                        filters.getMinPrice(),
                        filters.getMaxPrice(),
                        filters.getUserId(),
                        pageable
                )
                .map(ProductMapper::toResponse);
    }

    /*
     * Retorna productos activos de una categoría usando Slice.
     *
     * No calcula totalElements ni totalPages.
     */
    @Override
    @Transactional(readOnly = true)
    public Slice<ProductResponseDto> findByCategoryIdWithFiltersSlice(
            Long categoryId,
            ProductFilterByCategoryDto filters,
            PaginationDto pagination
    ) {
        if (!categoryRepository.existsByIdAndDeletedFalse(categoryId)) {
            throw new NotFoundException("Category not found");
        }

        validateCategoryFilters(filters);

        String name = normalizeName(filters.getName());

        Pageable pageable = createPageable(pagination);

        return productRepository.findByCategoryIdWithFiltersSlice(
                        categoryId,
                        name,
                        filters.getMinPrice(),
                        filters.getMaxPrice(),
                        filters.getUserId(),
                        pageable
                )
                .map(ProductMapper::toResponse);
    }

    private void validateUserFilters(ProductFilterByUserDto filters) {
        if (filters == null) {
            return;
        }

        if (!filters.hasValidPriceRange()) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }

        if (filters.getCategoryId() != null &&
                !categoryRepository.existsByIdAndDeletedFalse(filters.getCategoryId())) {
            throw new NotFoundException("Category not found");
        }
    }

    private void validateCategoryFilters(ProductFilterByCategoryDto filters) {
        if (filters == null) {
            return;
        }

        if (!filters.hasValidPriceRange()) {
            throw new BadRequestException("El precio máximo debe ser mayor o igual al precio mínimo");
        }

        if (filters.getUserId() != null &&
                !userRepository.existsByIdAndDeletedFalse(filters.getUserId())) {
            throw new NotFoundException("User not found");
        }
    }

    private Set<CategoryEntity> validateAndGetCategories(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new BadRequestException("Debe seleccionar al menos una categoría");
        }

        Set<CategoryEntity> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
            CategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category not found"));

            if (category.isDeleted()) {
                throw new NotFoundException("Category not found");
            }

            categories.add(category);
        }

        return categories;
    }

    /*
     * Construye el objeto Pageable validando:
     * página, tamaño, campo de ordenamiento y dirección.
     */
    private Pageable createPageable(PaginationDto pagination) {

        if (pagination == null) {
            pagination = new PaginationDto();
        }

        String sortBy = normalizeSortBy(pagination.getSortBy());

        Sort.Direction direction = normalizeDirection(pagination.getDirection());

        Sort sort = Sort.by(direction, sortBy);

        return PageRequest.of(
                pagination.getPage(),
                pagination.getSize(),
                sort
        );
    }

    /*
     * Valida que el campo de ordenamiento exista y esté permitido.
     */
    private String normalizeSortBy(String sortBy) {

        if (sortBy == null || sortBy.isBlank()) {
            return "id";
        }

        Set<String> allowedFields = Set.of(
                "id",
                "name",
                "price",
                "stock",
                "createdAt",
                "updatedAt"
        );

        if (!allowedFields.contains(sortBy)) {
            throw new BadRequestException("Campo de ordenamiento no permitido: " + sortBy);
        }

        return sortBy;
    }

    /*
     * Convierte la dirección recibida por query param
     * en Sort.Direction.
     */
    private Sort.Direction normalizeDirection(String direction) {

        if (direction == null || direction.isBlank()) {
            return Sort.Direction.ASC;
        }

        if (direction.equalsIgnoreCase("asc")) {
            return Sort.Direction.ASC;
        }

        if (direction.equalsIgnoreCase("desc")) {
            return Sort.Direction.DESC;
        }

        throw new BadRequestException("Dirección de ordenamiento no válida: " + direction);
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }

        return name.trim();
    }

    @Override
    public boolean validateName(String name) {
        return productRepository.findByNameIgnoreCaseAndDeletedFalse(name).isPresent();
    }

    // ============== OWNERSHIP (PRÁCTICA 13) ==============

    /*
     * Obtiene el usuario autenticado como entidad JPA.
     *
     * currentUser viene desde el token JWT. Se vuelve a consultar en BD
     * para asegurar que el usuario siga existiendo y no esté eliminado.
     */
    private UserEntity findCurrentUserEntity(UserDetailsImpl currentUser) {

        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        return userRepository.findByIdAndDeletedFalse(currentUser.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuario no autorizado"));
    }

    /*
     * Valida si el usuario autenticado puede modificar o eliminar el producto.
     *
     * Reglas:
     * 1. ROLE_ADMIN puede modificar cualquier producto.
     * 2. ROLE_USER solo puede modificar sus propios productos.
     */
    private void validateOwnership(ProductEntity product, UserDetailsImpl currentUser) {

        if (currentUser == null) {
            throw new AccessDeniedException("Usuario no autenticado");
        }

        if (hasRole(currentUser, "ROLE_ADMIN")) {
            return;
        }

        if (product.getOwner() == null || product.getOwner().getId() == null) {
            throw new AccessDeniedException("El producto no tiene propietario válido");
        }

        if (!product.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No puedes modificar productos ajenos");
        }
    }

    /*
     * Verifica si el usuario autenticado tiene un rol específico.
     */
    private boolean hasRole(UserDetailsImpl user, String role) {
        return user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals(role));
    }
}
