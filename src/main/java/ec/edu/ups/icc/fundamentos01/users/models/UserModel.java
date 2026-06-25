package ec.edu.ups.icc.fundamentos01.users.models;

import java.time.LocalDateTime;

/*
 * Modelo de dominio del recurso users.
 *
 * Representa al usuario dentro de la lógica de negocio.
 * No es una entidad de base de datos y no debe tener anotaciones JPA.
 */
public class UserModel {

    private Long id;

    private String name;

    private String email;

    private LocalDateTime createdAt;

    private String password;

    private String passwordHash;



    private LocalDateTime updatedAt;

    private boolean deleted;

    public UserModel() {
    }

    public UserModel(Long id, String name, String email, LocalDateTime createdAt, String password, String passwordHash,
            LocalDateTime updatedAt, boolean deleted) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.createdAt = createdAt;
        this.password = password;
        this.passwordHash = passwordHash;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // Constructor vacío
    

    // Constructor lleno

    // Getters y setters
}