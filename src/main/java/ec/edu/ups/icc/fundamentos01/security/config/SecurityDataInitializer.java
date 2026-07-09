package ec.edu.ups.icc.fundamentos01.security.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import ec.edu.ups.icc.fundamentos01.security.entities.RoleEntity;
import ec.edu.ups.icc.fundamentos01.security.enums.RoleName;
import ec.edu.ups.icc.fundamentos01.security.repositories.RoleRepository;

@Component
public class SecurityDataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    public SecurityDataInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {
        createRoleIfNotExists(RoleName.ROLE_USER, "Usuario estándar del sistema");
        createRoleIfNotExists(RoleName.ROLE_ADMIN, "Administrador del sistema");
    }

    private void createRoleIfNotExists(RoleName roleName, String description) {
        if (!roleRepository.existsByName(roleName)) {
            roleRepository.save(new RoleEntity(roleName, description));
        }
    }
}