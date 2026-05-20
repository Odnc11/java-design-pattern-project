package main.patterns.decorator;

/**
 * Concrete Decorator - Percentage Discount
 * 
 * Applies a percentage-based discount to the product price.
 * Example: 10% discount on a 100₺ product → 90₺
 */
public class DiscountDecorator extends ProductDecorator {
    private final double discountPercentage;

    /**
     * @param product            The product to decorate
     * @param discountPercentage Discount percentage (e.g., 10 for 10%)
     */
    public DiscountDecorator(ProductComponent product, double discountPercentage) {
        super(product);
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100");
        }
        this.discountPercentage = discountPercentage;
    }

    @Override
    public String getDescription() {
        return wrappedProduct.getDescription() + " [%" + (int) discountPercentage + " indirim]";
    }

    @Override
    public double getPrice() {
        double originalPrice = wrappedProduct.getPrice();
        return Math.round(originalPrice * (1 - discountPercentage / 100.0) * 100.0) / 100.0;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }
}
