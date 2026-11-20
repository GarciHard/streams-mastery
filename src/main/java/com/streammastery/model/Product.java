package com.streammastery.model;

import java.util.Objects;

/**
 * Represents a product in an e-commerce system.
 * Used for stream exercises involving business logic.
 */
public class Product {

    private final Integer id;
    private final String name;
    private final String category;
    private final Double price;
    private final Integer stock;
    private final Double rating;
    private final String brand;

    public Product(Integer id, String name, String category, Double price,
                   Integer stock, Double rating, String brand) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.rating = rating;
        this.brand = brand;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public Double getPrice() { return price; }
    public Integer getStock() { return stock; }
    public Double getRating() { return rating; }
    public String getBrand() { return brand; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', category='" + category +
               "', price=" + price + ", stock=" + stock +
               ", rating=" + rating + ", brand='" + brand + "'}";
    }
}
