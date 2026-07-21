package ec.edu.ups.icc.fundamentos01.core.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/*
 * DTO utilizado para recibir parámetros de paginación
 * desde query params.
 *
 * Ejemplo:
 * /api/products/page?page=0&size=10&sortBy=name&direction=asc
 */
@Schema(description = "Parámetros para paginación y ordenamiento")
public class PaginationDto {

    @Schema(description = "Número de página, iniciando en 0", example = "0")
    @Min(value = 0, message = "La página debe ser mayor o igual a 0")
    private int page = 0;

    @Schema(description = "Cantidad de registros por página", example = "10")
    @Min(value = 1, message = "El tamaño debe ser mayor o igual a 1")
    @Max(value = 100, message = "El tamaño no debe superar 100 registros")
    private int size = 10;

    @Schema(description = "Campo por el cual se ordenan los resultados", example = "price")
    private String sortBy = "id";

    @Schema(description = "Dirección de ordenamiento (asc o desc)", example = "desc")
    private String direction = "asc";

    public PaginationDto() {
    }

    public PaginationDto(
            int page,
            int size,
            String sortBy,
            String direction
    ) {
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.direction = direction;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getDirection() {
        return direction;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}