package main.models;

/**
 * Represents a product in the e-commerce system.
 * 
 * Each product belongs to a seller (sellerId) and has a base shipping cost.
 * Seller-applied decorators (discounts) modify the displayed price globally.
 * 
 * Follows Single Responsibility Principle - only handles product data.
 */
public class Product {
    private int id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String category;
    private int sellerId;
    private double shippingCost;

    /** Default shipping cost in ₺ */
    public static final double DEFAULT_SHIPPING = 29.99;

    public Product(int id, String name, String description, double price,
                   int stock, String category) {
        this(id, name, description, price, stock, category, 0, DEFAULT_SHIPPING);
    }

    public Product(int id, String name, String description, double price,
                   int stock, String category, int sellerId) {
        this(id, name, description, price, stock, category, sellerId, DEFAULT_SHIPPING);
    }

    public Product(int id, String name, String description, double price,
                   int stock, String category, int sellerId, double shippingCost) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.sellerId = sellerId;
        this.shippingCost = shippingCost;
    }

    // --- Getters ---
    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public String getCategory() { return category; }
    public int getSellerId() { return sellerId; }
    public double getShippingCost() { return shippingCost; }

    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setCategory(String category) { this.category = category; }
    public void setSellerId(int sellerId) { this.sellerId = sellerId; }
    public void setShippingCost(double shippingCost) { this.shippingCost = shippingCost; }

    @Override
    public String toString() {
        return String.format("%s - %.2f₺ (Stok: %d)", name, price, stock);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Product product = (Product) obj;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
