package main.patterns.decorator;

/**
 * Concrete Decorator - Free Shipping
 * 
 * Adds free shipping to a product. Does not affect the price,
 * but marks the product as having free shipping (shipping cost = 0).
 */
public class FreeShippingDecorator extends ProductDecorator {

    public FreeShippingDecorator(ProductComponent product) {
        super(product);
    }

    @Override
    public String getDescription() {
        return wrappedProduct.getDescription() + " [Ücretsiz Kargo]";
    }

    @Override
    public boolean hasFreeShipping() {
        return true;
    }

    @Override
    public double getShippingCost() {
        return 0; // Free!
    }
}
