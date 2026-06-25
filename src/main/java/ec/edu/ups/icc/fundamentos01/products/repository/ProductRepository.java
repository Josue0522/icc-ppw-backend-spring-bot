// ProductRepository.java
package ec.edu.ups.icc.fundamentos01.products.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.products.entity.ProductEntity;
import java.util.Optional;

@Repository
public interface ProductRepository
        extends JpaRepository<ProductEntity, Long> {

    Optional<ProductEntity> findByName(String name);
}