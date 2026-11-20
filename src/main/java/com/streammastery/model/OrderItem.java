package com.streammastery.model;

/**
 * Represents a single item within an Order.
 */
public class OrderItem {

    private final String productName;
    private final String category;
    private final Double price;
    private final Integer quantity;

    public OrderItem(String productName, String category, Double price, Integer quantity) {
        this.productName = productName;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
    }

    public String getProductName() { return productName; }
    public String getCategory() { return category; }
    public Double getPrice() { return price; }
    public Integer getQuantity() { return quantity; }

    public Double getSubtotal() { return price * quantity; }

    @Override
    public String toString() {
        return "OrderItem{product='" + productName + "', category='" + category +
               "', price=" + price + ", qty=" + quantity + "}";
    }
}
