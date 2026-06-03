package main.models;

import main.patterns.singleton.ShoppingCart;

/**
 * Buyer user - can browse products, add to cart, apply coupons, and checkout.
 * 
 * Each buyer gets their own ShoppingCart instance (Singleton per-user).
 * Buyers can register through the UI or use pre-created mock accounts.
 */
public class Buyer extends User {
    private final double freeShippingThreshold;

    public Buyer(int id, String name, String email, String password, double freeShippingThreshold) {
        super(id, name, email, password);
        this.freeShippingThreshold = freeShippingThreshold;
    }

    // Default constructor for backward compatibility
    public Buyer(int id, String name, String email, String password) {
        this(id, name, email, password, 500.0); // Default 500 TL
    }

    public double getFreeShippingThreshold() {
        return freeShippingThreshold;
    }

    @Override
    public Role getRole() {
        return Role.BUYER;
    }

    /**
     * Get this buyer's shopping cart (Singleton per-user).
     * Always returns the same cart instance for this buyer.
     */
    public ShoppingCart getShoppingCart() {
        return ShoppingCart.getInstance(this.getId());
    }

    @Override
    public String toString() {
        return String.format("Buyer{id=%d, name='%s', email='%s'}",
                getId(), getName(), getEmail());
    }
}
