package main.patterns.decorator;

/**
 * Concrete Decorator - Fixed Amount Discount
 * 
 * Applies a fixed monetary amount discount to the product price.
 * Example: 500₺ discount on a 10000₺ product → 9500₺
 * Price cannot go below 0.
 * 
 * DECORATOR ORDER MATTERS:
 *   %15 then 500₺: 10000 * 0.85 = 8500 - 500 = 8000₺
 *   500₺ then %15: (10000 - 500) = 9500 * 0.85 = 8075₺
 *   DIFFERENCE: 75₺!
 */
public class FixedAmountDiscountDecorator extends ProductDecorator {
    private final double discountAmount;

    /**
     * @param product        The product to decorate
     * @param discountAmount Fixed discount amount in ₺
     */
    public FixedAmountDiscountDecorator(ProductComponent product, double discountAmount) {
        super(product);
        if (discountAmount < 0) {
            throw new IllegalArgumentException("Discount amount cannot be negative");
        }
        this.discountAmount = discountAmount;
    }

    @Override
    public String getDescription() {
        return wrappedProduct.getDescription() + " [-" + (int) discountAmount + "₺ indirim]";
    }

    @Override
    public double getPrice() {
        double price = wrappedProduct.getPrice() - discountAmount;
        return Math.max(Math.round(price * 100.0) / 100.0, 0);
    }

    public double getDiscountAmount() {
        return discountAmount;
    }
}
