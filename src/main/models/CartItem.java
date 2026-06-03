package main.models;

import main.patterns.decorator.ProductComponent;

/**
 * Represents an item in a shopping cart with quantity tracking.
 * 
 * Holds both the decorated product (with buyer-applied decorators)
 * and a reference to the original Product for stock management.
 * 
 * Decorator Architecture:
 *   - originalProduct: base product from catalog (with seller's global decorators already applied)
 *   - decoratedProduct: buyer's personal decorator chain (coupons, free shipping applied in cart)
 */
public class CartItem {
    private ProductComponent decoratedProduct;
    private final Product originalProduct;
    private int quantity;

    public CartItem(ProductComponent decoratedProduct, Product originalProduct, int quantity) {
        this.decoratedProduct = decoratedProduct;
        this.originalProduct = originalProduct;
        this.quantity = Math.max(1, quantity);
    }

    // --- Getters ---
    public ProductComponent getDecoratedProduct() { return decoratedProduct; }
    public Product getOriginalProduct() { return originalProduct; }
    public int getQuantity() { return quantity; }

    // --- Setters ---
    public void setDecoratedProduct(ProductComponent decoratedProduct) {
        this.decoratedProduct = decoratedProduct;
    }
    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    /**
     * Calculate subtotal: decorated price × quantity.
     */
    public double getSubtotal() {
        return Math.round(decoratedProduct.getPrice() * quantity * 100.0) / 100.0;
    }

    /**
     * Get the unit price from the decorated product.
     */
    public double getUnitPrice() {
        return decoratedProduct.getPrice();
    }

    @Override
    public String toString() {
        return String.format("%s x%d = %.2f₺",
                decoratedProduct.getName(), quantity, getSubtotal());
    }
}
