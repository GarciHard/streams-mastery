package com.streammastery.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents a financial transaction.
 * Used for advanced grouping, partitioning, and statistical exercises.
 */
public class Transaction {

    public enum Type { CREDIT, DEBIT }

    private final Integer id;
    private final String accountId;
    private final Type type;
    private final Double amount;
    private final String currency;
    private final String category; // "FOOD", "TRANSPORT", "ENTERTAINMENT", "UTILITIES", "SALARY", "INVESTMENT"
    private final LocalDate date;
    private final String merchant;

    public Transaction(Integer id, String accountId, Type type, Double amount,
                       String currency, String category, LocalDate date, String merchant) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.category = category;
        this.date = date;
        this.merchant = merchant;
    }

    public Integer getId() { return id; }
    public String getAccountId() { return accountId; }
    public Type getType() { return type; }
    public Double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getCategory() { return category; }
    public LocalDate getDate() { return date; }
    public String getMerchant() { return merchant; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Transaction{id=" + id + ", account='" + accountId +
               "', type=" + type + ", amount=" + amount +
               ", currency='" + currency + "', category='" + category +
               "', date=" + date + ", merchant='" + merchant + "'}";
    }
}
