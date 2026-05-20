package main.patterns.decorator;

/**
 * Concrete Decorator - Coupon Discount
 * 
 * Applies a fixed-amount coupon discount to the product price.
 * Example: 20₺ coupon on a 100₺ product → 80₺
 * Price cannot go below 0.
 */
public class CouponDecorator extends ProductDecorator {
    private final double couponAmount;
    private final String couponCode;

    /**
     * @param product      The product to decorate
     * @param couponAmount Fixed discount amount in ₺
     * @param couponCode   Coupon code identifier
     */
    public CouponDecorator(ProductComponent product, double couponAmount, String couponCode) {
        super(product);
        if (couponAmount < 0) {
            throw new IllegalArgumentException("Coupon amount cannot be negative");
        }
        this.couponAmount = couponAmount;
        this.couponCode = couponCode;
    }

    @Override
    public String getDescription() {
        return wrappedProduct.getDescription() + " [Kupon: " + couponCode + " -" + (int) couponAmount + "₺]";
    }

    @Override
    public double getPrice() {
        double price = wrappedProduct.getPrice() - couponAmount;
        return Math.max(Math.round(price * 100.0) / 100.0, 0);
    }

    public String getCouponCode() {
        return couponCode;
    }

    public double getCouponAmount() {
        return couponAmount;
    }
}
