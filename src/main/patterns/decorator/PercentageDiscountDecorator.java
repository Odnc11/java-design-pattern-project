package main.patterns.decorator;

/**
 * Concrete Decorator - Percentage Discount
 * 
 * Applies a percentage-based discount to the product price.
 * Example: 15% discount on a 10000₺ product → 8500₺
 * 
 * DECORATOR ORDER MATTERS:
 *   %15 then 500₺ fixed: 10000 * 0.85 = 8500 - 500 = 8000₺
 *   500₺ fixed then %15: (10000 - 500) = 9500 * 0.85 = 8075₺
 */
public class PercentageDiscountDecorator extends ProductDecorator {
    private final double discountPercentage;

    /**
     * @param product            The product to decorate
     * @param discountPercentage Discount percentage (e.g., 15 for 15%)
     */
    public PercentageDiscountDecorator(ProductComponent product, double discountPercentage) {
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
