package main.patterns.singleton;

import main.models.CartItem;
import main.patterns.decorator.ProductComponent;
import main.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-User Scoped Shopping Cart
 * 
 * NOT a GoF Singleton Pattern! Bu sınıf per-user scoped'dır:
 * Her buyer kendi ayrı cart instance'ına sahiptir.
 * Gerçek Singleton için bkz: AppConfig.java
 * 
 * Session-scoped object: her buyer oturumu için bir instance.
 * getInstance(buyerId) ile factory-like erişim sağlanır.
 * 
 * - Each buyer maintains a single cart throughout their session
 * - Different buyers have completely separate carts
 * - All UI components for a given buyer reference the same cart state
 * - Logout clears the buyer's cart instance
 * 
 * SOLID Principles:
 * - SRP: Only responsible for cart item management
 * - OCP: Works with any ProductComponent (decorated or not)
 */
public class ShoppingCart {
    /** Per-user cart instances: buyerId → ShoppingCart */
    private static final Map<Integer, ShoppingCart> instances = new ConcurrentHashMap<>();

    private final List<CartItem> items;
    private final List<CartChangeListener> listeners;

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
     * Returns the shopping cart instance for a specific buyer.
     * Creates a new instance if one doesn't exist for this buyer.
     * 
     * @param buyerId The buyer's unique ID
     * @return The buyer's ShoppingCart instance
     */
    public static ShoppingCart getInstance(int buyerId) {
        return instances.computeIfAbsent(buyerId, id -> new ShoppingCart());
    }

    /**
     * Clear a buyer's cart instance (called on logout).
     */
    public static void clearInstance(int buyerId) {
        ShoppingCart cart = instances.remove(buyerId);
        if (cart != null) {
            cart.items.clear();
            cart.listeners.clear();
        }
    }

    /**
     * Reset all instances (for testing purposes only).
     */
    public static synchronized void resetAllInstances() {
        instances.clear();
    }

    /**
     * Check if two buyer IDs share the same cart instance (they shouldn't).
     * Useful for demonstrating Singleton per-user in tests.
     */
    public static boolean isSameInstance(int buyerId1, int buyerId2) {
        return instances.get(buyerId1) == instances.get(buyerId2);
    }

    // --- Cart Operations ---

    /**
     * Add a product to the cart. If product already exists, increases quantity.
     */
    public synchronized void addItem(ProductComponent decoratedProduct, Product originalProduct) {
        addItem(decoratedProduct, originalProduct, 1);
    }

    /**
     * Add a product with specific quantity to the cart.
     */
    public synchronized void addItem(ProductComponent decoratedProduct, Product originalProduct, int quantity) {
        // Check if product already in cart (by original product ID)
        for (CartItem item : items) {
            if (item.getOriginalProduct().equals(originalProduct)) {
                item.setQuantity(item.getQuantity() + quantity);
                notifyListeners();
                return;
            }
        }
        items.add(new CartItem(decoratedProduct, originalProduct, quantity));
        notifyListeners();
    }

    /**
     * Remove an item from the cart by index.
     */
    public synchronized void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            notifyListeners();
        }
    }

    /**
     * Remove a specific product from the cart.
     */
    public synchronized void removeProduct(Product product) {
        items.removeIf(item -> item.getOriginalProduct().equals(product));
        notifyListeners();
    }

    /**
     * Update quantity of an item. Removes item if quantity <= 0.
     */
    public synchronized void updateQuantity(int index, int newQuantity) {
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
     * Update the decorated product for a cart item (when buyer applies coupon/discount).
     */
    public synchronized void updateDecoratedProduct(int index, ProductComponent newDecorated) {
        if (index >= 0 && index < items.size()) {
            items.get(index).setDecoratedProduct(newDecorated);
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
     * Calculate shipping cost. Free if any item has free shipping, 
     * or if the cart total exceeds the buyer's personal free shipping threshold.
     */
    public double getShippingCost() {
        for (CartItem item : items) {
            if (item.getDecoratedProduct().hasFreeShipping()) {
                return 0; // Explicitly free via decorator
            }
        }
        if (items.isEmpty()) return 0;
        
        main.models.Buyer buyer = main.utils.SessionManager.getCurrentBuyer();
        if (buyer != null) {
            double threshold = buyer.getFreeShippingThreshold();
            if (getTotal() >= threshold) {
                return 0; // Free due to cart total exceeding threshold
            }
        }
        
        return AppConfig.getInstance().getDefaultShippingCost();
    }

    /**
     * Get grand total including shipping.
     */
    public double getGrandTotal() {
        return getTotal() + getShippingCost();
    }

    /**
     * Get the total quantity of a specific product currently in the cart.
     */
    public synchronized int getQuantity(Product product) {
        int count = 0;
        for (CartItem item : items) {
            if (item.getOriginalProduct().equals(product)) {
                count += item.getQuantity();
            }
        }
        return count;
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
    public synchronized void clear() {
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
