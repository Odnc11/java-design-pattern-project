package main.patterns.decorator;

/**
 * Abstract Decorator - Decorator Design Pattern
 * 
 * Base class for all concrete decorators. Implements ProductComponent
 * and delegates all calls to the wrapped component by default.
 * Concrete decorators override specific methods to add behavior.
 * 
 * SOLID Principles:
 * - OCP: New decorators can be created without modifying existing code
 * - LSP: All decorators are substitutable for ProductComponent
 * - DIP: Depends on ProductComponent abstraction
 */
public abstract class ProductDecorator implements ProductComponent {
    protected final ProductComponent wrappedProduct;

    public ProductDecorator(ProductComponent product) {
        if (product == null) {
            throw new IllegalArgumentException("Wrapped product cannot be null");
        }
        this.wrappedProduct = product;
    }

    @Override
    public String getName() {
        return wrappedProduct.getName();
    }

    @Override
    public String getDescription() {
        return wrappedProduct.getDescription();
    }

    @Override
    public double getPrice() {
        return wrappedProduct.getPrice();
    }

    @Override
    public boolean hasFreeShipping() {
        return wrappedProduct.hasFreeShipping();
    }

    @Override
    public double getShippingCost() {
        return wrappedProduct.getShippingCost();
    }

    /**
     * @return The wrapped ProductComponent
     */
    public ProductComponent getWrappedProduct() {
        return wrappedProduct;
    }
}
