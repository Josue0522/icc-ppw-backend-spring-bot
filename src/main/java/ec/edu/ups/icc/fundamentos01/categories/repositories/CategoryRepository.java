package ec.edu.ups.icc.fundamentos01.categories.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;

/*
 * Repositorio encargado de gestionar la persistencia
 * de categorías usando Spring Data JPA.
 */
@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    Optional<CategoryEntity> findByNameIgnoreCaseAndDeletedFalse(String name);

    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);

    boolean existsByIdAndDeletedFalse(Long id);

    List<CategoryEntity> findByDeletedFalse();
}
