package ec.edu.ups.icc.fundamentos01.products.dto;

public class PartialUpdateProductDto {

    private String name;
    private Double price;
    private Integer stock;

    public PartialUpdateProductDto() {
    }

    public PartialUpdateProductDto(String name, Double price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}