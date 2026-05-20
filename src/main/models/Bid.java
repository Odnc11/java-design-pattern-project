package main.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a bid placed by a user on a product in the auction system.
 * Part of the demand-driven auction model where prices change based on interest.
 * 
 * Follows SRP - only handles bid data.
 */
public class Bid {
    private final int id;
    private final User user;
    private final Product product;
    private final double amount;
    private final LocalDateTime timestamp;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    public Bid(int id, User user, Product product, double amount) {
        this.id = id;
        this.user = user;
        this.product = product;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }

    public int getId() { return id; }
    public User getUser() { return user; }
    public Product getProduct() { return product; }
    public double getAmount() { return amount; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public String getFormattedTime() {
        return timestamp.format(FORMATTER);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s → %s: %.2f₺",
                getFormattedTime(), user.getName(), product.getName(), amount);
    }
}
