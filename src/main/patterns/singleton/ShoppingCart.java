package main.patterns.singleton;

import main.patterns.decorator.ProductComponent;
import main.patterns.decorator.ConcreteProduct;
import main.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton Pattern (Creational) - Shopping Cart
 * 
 * Guarantees a single shopping cart instance per application session.
 * Uses thread-safe lazy initialization with synchronized access.
 * 
 * Why Singleton?
 * - Prevents cart data inconsistency across different parts of the app
 * - Ensures all UI components reference the same cart state
 * - Single point of access for cart operations
 * 
 * SOLID Principles:
 * - SRP: Only responsible for cart item management
 * - OCP: Works with any ProductComponent (decorated or not)
 */
public class ShoppingCart {
    // Volatile ensures visibility across threads
    private static volatile ShoppingCart instance;

    private final List<CartItem> items;
    private final List<CartChangeListener> listeners;

    /**
     * Represents an item in the shopping cart with quantity tracking.
     */
    public static class CartItem {
        private final ProductComponent product;
        private final Product originalProduct;
        private int quantity;

        public CartItem(ProductComponent product, Product originalProduct, int quantity) {
            this.product = product;
            this.originalProduct = originalProduct;
            this.quantity = quantity;
        }

        public ProductComponent getProduct() { return product; }
        public Product getOriginalProduct() { return originalProduct; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getSubtotal() {
            return product.getPrice() * quantity;
        }

        @Override
        public String toString() {
            return String.format("%s x%d = %.2f₺", product.getName(), quantity, getSubtotal());
        }
    }

    /**
     * Listener interface for cart state changes (used by UI).
     */
    public interface CartChangeListener {
        void onCartChanged();
    }

    // Private constructor - prevents external instantiation
    private ShoppingCart() {
        items = new ArrayList<>();
        listeners = new ArrayList<>();
    }

    /**
     * Returns the single instance of ShoppingCart.
     * Uses double-checked locking for thread safety.
     */
    public static ShoppingCart getInstance() {
        if (instance == null) {
            synchronized (ShoppingCart.class) {
                if (instance == null) {
                    instance = new ShoppingCart();
                }
            }
        }
        return instance;
    }

    /**
     * Reset the singleton instance (for testing purposes only).
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    // --- Cart Operations ---

    /**
     * Add a product to the cart. If product already exists, increases quantity.
     */
    public void addItem(ProductComponent product, Product originalProduct) {
        addItem(product, originalProduct, 1);
    }

    /**
     * Add a product with specific quantity to the cart.
     */
    public void addItem(ProductComponent product, Product originalProduct, int quantity) {
        // Check if product already in cart (by original product ID)
        for (CartItem item : items) {
            if (item.getOriginalProduct().equals(originalProduct)) {
                item.setQuantity(item.getQuantity() + quantity);
                notifyListeners();
                return;
            }
        }
        items.add(new CartItem(product, originalProduct, quantity));
        notifyListeners();
    }

    /**
     * Remove an item from the cart by index.
     */
    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            notifyListeners();
        }
    }

    /**
     * Remove a specific product from the cart.
     */
    public void removeProduct(Product product) {
        items.removeIf(item -> item.getOriginalProduct().equals(product));
        notifyListeners();
    }

    /**
     * Update quantity of an item. Removes item if quantity <= 0.
     */
    public void updateQuantity(int index, int newQuantity) {
        if (index >= 0 && index < items.size()) {
            if (newQuantity <= 0) {
                items.remove(index);
            } else {
                items.get(index).setQuantity(newQuantity);
            }
            notifyListeners();
        }
    }

    /**
     * Calculate total price of all items in the cart.
     */
    public double getTotal() {
        double total = 0;
        for (CartItem item : items) {
            total += item.getSubtotal();
        }
        return Math.round(total * 100.0) / 100.0;
    }

    /**
     * Calculate shipping cost. Free if any item has free shipping.
     */
    public double getShippingCost() {
        for (CartItem item : items) {
            if (item.getProduct().hasFreeShipping()) {
                return 0;
            }
        }
        return items.isEmpty() ? 0 : 29.99; // Standard shipping
    }

    /**
     * Get grand total including shipping.
     */
    public double getGrandTotal() {
        return getTotal() + getShippingCost();
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) {
            count += item.getQuantity();
        }
        return count;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Clear all items from the cart.
     */
    public void clear() {
        items.clear();
        notifyListeners();
    }

    // --- Listener Management ---

    public void addChangeListener(CartChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeChangeListener(CartChangeListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        for (CartChangeListener listener : new ArrayList<>(listeners)) {
            listener.onCartChanged();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== Alışveriş Sepeti ===\n");
        for (int i = 0; i < items.size(); i++) {
            sb.append((i + 1)).append(". ").append(items.get(i)).append("\n");
        }
        sb.append("──────────────────────\n");
        sb.append(String.format("Ara Toplam: %.2f₺\n", getTotal()));
        sb.append(String.format("Kargo: %.2f₺\n", getShippingCost()));
        sb.append(String.format("TOPLAM: %.2f₺\n", getGrandTotal()));
        return sb.toString();
    }
}
