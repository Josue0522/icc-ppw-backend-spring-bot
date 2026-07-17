package ec.edu.ups.icc.fundamentos01.core.dtos;

public class ErrorResponseDto {
    private String message;

    public ErrorResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
