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
import ec.edu.ups.icc.fundamentos01.security.config.OpenApiConfig;
import ec.edu.ups.icc.fundamentos01.security.services.UserDetailsImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/*
 * Controlador REST encargado de exponer endpoints HTTP
 * para la gestión de productos.
 *
 * Todos los endpoints de este controlador requieren JWT,
 * porque el proyecto usa .anyRequest().authenticated().
 */
@Tag(
        name = "Productos",
        description = "Gestión de productos con paginación, roles y ownership"
)
@SecurityRequirement(name = OpenApiConfig.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/products")
public class ProductsController {

    private final ProductService service;

    public ProductsController(ProductService service) {
        this.service = service;
    }

    /*
     * Endpoint administrativo.
     *
     * GET /api/products
     *
     * Solo ADMIN puede acceder: muestra todos los productos de todos los
     * usuarios sin paginación, lo que expone más información de la necesaria
     * para un usuario común.
     */
    @Operation(
            summary = "Listar todos los productos",
            description = """
                    Devuelve todos los productos activos sin paginación.

                    Este endpoint es administrativo y requiere ROLE_ADMIN.
                    Para consultas normales se recomienda usar /products/page o /products/slice.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado completo de productos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no tiene ROLE_ADMIN")
    })
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
    @Operation(
            summary = "Listar productos con Page",
            description = """
                    Devuelve productos activos usando Page.

                    Incluye metadatos como totalElements, totalPages, number, size, first y last.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de productos obtenida correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
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
    @Operation(
            summary = "Listar productos propios con Slice",
            description = """
                    Devuelve, usando Slice, solo los productos del usuario autenticado.

                    No calcula totalElements ni totalPages. Útil para scroll infinito.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Slice de productos obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/slice")
    public Slice<ProductResponseDto> findAllSlice(
            @Valid @ModelAttribute PaginationDto pagination,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        return service.findAllSlice(pagination, currentUser);
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = """
                    Busca un producto activo utilizando su identificador.

                    Requiere autenticación JWT. El usuario debe enviar un access token válido.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Producto encontrado correctamente"),
            @ApiResponse(responseCode = "401", description = "No se proporcionó un access token válido"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
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
    @Operation(
            summary = "Crear producto",
            description = """
                    Crea un producto asociado al usuario autenticado.

                    El cliente no debe enviar userId: el owner se obtiene desde el JWT
                    mediante @AuthenticationPrincipal.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "409", description = "Nombre de producto ya registrado")
    })
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
    @Operation(
            summary = "Actualizar producto",
            description = """
                    Actualiza completamente un producto.

                    ROLE_USER solo puede actualizar productos propios.
                    ROLE_ADMIN puede actualizar cualquier producto.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no es propietario del producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
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
    @Operation(
            summary = "Actualizar parcialmente un producto",
            description = """
                    Actualiza solo los campos enviados en el body.

                    Misma regla de ownership que la actualización completa.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no es propietario del producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
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
    @Operation(
            summary = "Eliminar producto",
            description = """
                    Elimina lógicamente un producto (soft delete).

                    ROLE_USER solo puede eliminar productos propios.
                    ROLE_ADMIN puede eliminar cualquier producto.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido"),
            @ApiResponse(responseCode = "403", description = "El usuario no es propietario del producto"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl currentUser
    ) {
        service.delete(id, currentUser);
    }

    @Operation(
            summary = "Listar productos de un usuario",
            description = "Devuelve todos los productos activos de un usuario específico, dado su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/user/{userId}")
    public List<ProductResponseDto> findByUserId(@PathVariable Long userId) {
        return service.findByUserId(userId);
    }

    @Operation(
            summary = "Listar productos de una categoría",
            description = "Devuelve todos los productos activos que pertenecen a una categoría específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/category/{categoryId}")
    public List<ProductResponseDto> findByCategoryId(@PathVariable Long categoryId) {
        return service.findByCategoryId(categoryId);
    }

    @Operation(
            summary = "Validar disponibilidad de nombre",
            description = "Verifica si ya existe un producto activo registrado con ese nombre."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Consulta realizada correctamente"),
            @ApiResponse(responseCode = "401", description = "Token ausente o inválido")
    })
    @GetMapping("/validate-name")
    public boolean validateName(@RequestParam String name) {
        return service.validateName(name);
    }
}