package ec.edu.ups.icc.fundamentos01.categories.services;

import java.util.List;

import org.springframework.stereotype.Service;

import ec.edu.ups.icc.fundamentos01.categories.dtos.CategoryResponseDto;
import ec.edu.ups.icc.fundamentos01.categories.dtos.CreateCategoryDto;
import ec.edu.ups.icc.fundamentos01.categories.dtos.UpdateCategoryDto;
import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repositories.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.core.exceptions.domain.NotFoundException;

/*
 * Implementación del servicio de categorías.
 *
 * Usa CategoryRepository para persistir datos en PostgreSQL.
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryResponseDto> findAll() {
        return categoryRepository.findByDeletedFalse()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public CategoryResponseDto findOne(Long id) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (entity.isDeleted()) {
            throw new NotFoundException("Category not found");
        }

        return toResponse(entity);
    }

    @Override
    public CategoryResponseDto create(CreateCategoryDto dto) {

        if (categoryRepository.existsByNameIgnoreCaseAndDeletedFalse(dto.getName())) {
            throw new ConflictException("Category name already registered");
        }

        CategoryEntity entity = new CategoryEntity();

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        CategoryEntity saved = categoryRepository.save(entity);

        return toResponse(saved);
    }

    @Override
    public CategoryResponseDto update(Long id, UpdateCategoryDto dto) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (entity.isDeleted()) {
            throw new NotFoundException("Category not found");
        }

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        CategoryEntity saved = categoryRepository.save(entity);

        return toResponse(saved);
    }

    @Override
    public void delete(Long id) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (entity.isDeleted()) {
            throw new NotFoundException("Category not found");
        }

        entity.setDeleted(true);

        categoryRepository.save(entity);
    }


    /// Este método es privado porque solo se utiliza dentro de esta clase
    //  para convertir una entidad de categoría en un DTO de respuesta. 
    //  Puede estar en una clase Mapper
    private CategoryResponseDto toResponse(CategoryEntity entity) {
        CategoryResponseDto dto = new CategoryResponseDto();

        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());

        return dto;
    }
}
