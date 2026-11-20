package com.streammastery.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Represents a customer order in an e-commerce system.
 * Used for advanced stream exercises involving nested structures and flatMap.
 */
public class Order {

    private final Integer id;
    private final Integer customerId;
    private final String customerName;
    private final String status; // "PENDING", "SHIPPED", "DELIVERED", "CANCELLED"
    private final LocalDate orderDate;
    private final List<OrderItem> items;

    public Order(Integer id, Integer customerId, String customerName, String status,
                 LocalDate orderDate, List<OrderItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.status = status;
        this.orderDate = orderDate;
        this.items = items;
    }

    public Integer getId() { return id; }
    public Integer getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getStatus() { return status; }
    public LocalDate getOrderDate() { return orderDate; }
    public List<OrderItem> getItems() { return items; }

    public Double getTotalPrice() {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Order{id=" + id + ", customer='" + customerName +
               "', status='" + status + "', date=" + orderDate +
               ", items=" + items.size() + ", total=" + getTotalPrice() + "}";
    }
}
