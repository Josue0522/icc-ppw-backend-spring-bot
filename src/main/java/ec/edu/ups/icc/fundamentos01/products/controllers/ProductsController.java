package ec.edu.ups.icc.fundamentos01.products.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ec.edu.ups.icc.fundamentos01.core.dtos.PaginationDto;
import ec.edu.ups.icc.fundamentos01.products.dto.CreateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.PartialUpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.dto.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.dto.UpdateProductDto;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductService service;

    public ProductsController(ProductService service) {
        this.service = service;
    }

    /*
     * Endpoint normal.
     *
     * GET /api/products
     *
     * Se mantiene sin paginación para comparar con los endpoints paginados.
     *
     * Solo ADMIN puede acceder: muestra todos los productos de todos los
     * usuarios sin paginación, lo que expone más información de la necesaria
     * para un usuario común.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<ProductResponseDto> findAll() {
        return service.findAll();
    }

    /*
     * Endpoint paginado usando Page.
     *
     * GET /api/products/page
     * GET /api/products/page?page=0&size=5
     * GET /api/products/page?page=0&size=5&sortBy=price&direction=desc
     */
    @GetMapping("/page")
    public Page<ProductResponseDto> findAllPage(
            @Valid @ModelAttribute PaginationDto pagination
    ) {
        return service.findAllPage(pagination);
    }

    /*
     * Endpoint paginado usando Slice.
     *
     * GET /api/products/slice
     * GET /api/products/slice?page=0&size=5
     * GET /api/products/slice?page=0&size=5&sortBy=createdAt&direction=desc
     *
     * Solo muestra los productos del usuario autenticado (owner = token JWT).
     */
    @GetMapping("/slice")
    public Slice<ProductResponseDto> findAllSlice(
            @Valid @ModelAttribute PaginationDto pagination,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.findAllSlice(pagination, currentUser);
    }

    @GetMapping("/{id}")
    public ProductResponseDto findOne(@PathVariable Long id) {
        return service.findOne(id);
    }

    /*
     * Crear producto.
     *
     * POST /api/products
     *
     * El owner ya no se toma desde el body: se obtiene desde el token JWT
     * mediante @AuthenticationPrincipal (ver Práctica 13).
     */
    @PostMapping
    public ProductResponseDto create(
            @Valid @RequestBody CreateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.create(dto, currentUser);
    }

    /*
     * Actualizar producto.
     *
     * PUT /api/products/{id}
     *
     * La validación de ownership (propietario, ADMIN o no) se hace en el
     * servicio, no aquí (ver Práctica 13).
     */
    @PutMapping("/{id}")
    public ProductResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.update(id, dto, currentUser);
    }

    /*
     * Actualizar parcialmente un producto.
     *
     * PATCH /api/products/{id}
     *
     * Misma validación de ownership que update().
     */
    @PatchMapping("/{id}")
    public ProductResponseDto partialUpdate(
            @PathVariable Long id,
            @Valid @RequestBody PartialUpdateProductDto dto,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.partialUpdate(id, dto, currentUser);
    }

    /*
     * Eliminar producto (lógicamente).
     *
     * DELETE /api/products/{id}
     *
     * Misma validación de ownership que update().
     */
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        service.delete(id, currentUser);
    }

    @GetMapping("/user/{userId}")
    public List<ProductResponseDto> findByUserId(@PathVariable Long userId) {
        return service.findByUserId(userId);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDto> findByCategoryId(@PathVariable Long categoryId) {
        return service.findByCategoryId(categoryId);
    }

    @GetMapping("/validate-name")
    public boolean validateName(@RequestParam String name) {
        return service.validateName(name);
    }
}