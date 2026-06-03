package main.patterns.decorator;

import main.models.Product;

/**
 * Concrete Component - Decorator Design Pattern
 * 
 * Wraps a Product model object and implements the ProductComponent interface.
 * This is the base object that decorators will wrap.
 */
public class ConcreteProduct implements ProductComponent {
    private final Product product;

    public ConcreteProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        this.product = product;
    }

    @Override
    public String getName() {
        return product.getName();
    }

    @Override
    public String getDescription() {
        return product.getDescription();
    }

    @Override
    public double getPrice() {
        return product.getPrice();
    }

    @Override
    public boolean hasFreeShipping() {
        return false; // Default: no free shipping
    }

    @Override
    public double getShippingCost() {
        return product.getShippingCost();
    }

    /**
     * @return The underlying Product model object
     */
    public Product getProduct() {
        return product;
    }
}
