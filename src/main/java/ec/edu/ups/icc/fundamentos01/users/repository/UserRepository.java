package ec.edu.ups.icc.fundamentos01.users.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ec.edu.ups.icc.fundamentos01.users.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByIdAndDeletedFalse(Long id);

    Optional<UserEntity> findByEmailAndDeletedFalse(String email);

    Optional<UserEntity> findByIdAndDeletedFalse(Long id);

    boolean existsByEmail(String email);
}