package main.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Seller user - can create products, manage stock, and apply discounts.
 * 
 * Each seller has a store name and a list of their own products.
 * Seller accounts are pre-created via mock data (no registration).
 */
public class Seller extends User {
    private String storeName;
    private final List<Product> myProducts;

    public Seller(int id, String name, String email, String password, String storeName) {
        super(id, name, email, password);
        this.storeName = storeName;
        this.myProducts = new ArrayList<>();
    }

    @Override
    public Role getRole() {
        return Role.SELLER;
    }

    // --- Store Management ---

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    /**
     * Get unmodifiable list of this seller's products.
     */
    public List<Product> getMyProducts() {
        return Collections.unmodifiableList(myProducts);
    }

    /**
     * Add a product to this seller's catalog.
     */
    public void addProduct(Product product) {
        if (product != null && !myProducts.contains(product)) {
            product.setSellerId(this.getId());
            myProducts.add(product);
        }
    }

    /**
     * Remove a product from this seller's catalog.
     */
    public void removeProduct(Product product) {
        myProducts.remove(product);
    }

    /**
     * Update stock for a specific product.
     * @return true if product found and updated
     */
    public boolean updateStock(int productId, int newStock) {
        for (Product p : myProducts) {
            if (p.getId() == productId) {
                p.setStock(newStock);
                return true;
            }
        }
        return false;
    }

    /**
     * Find a product by ID from this seller's catalog.
     */
    public Product findProduct(int productId) {
        for (Product p : myProducts) {
            if (p.getId() == productId) {
                return p;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("Seller{id=%d, name='%s', store='%s', products=%d}",
                getId(), getName(), storeName, myProducts.size());
    }
}
