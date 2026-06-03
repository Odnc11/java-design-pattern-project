package main.patterns.decorator;

/**
 * Concrete Decorator - Flash Sale
 * 
 * Applies a flash sale discount of 10% to the product price.
 * Represents a time-limited special offer.
 * 
 * Example: Flash sale on a 12000₺ GPU → 10800₺
 */
public class FlashSaleDecorator extends ProductDecorator {
    private static final double FLASH_SALE_DISCOUNT = 10.0; // %10

    public FlashSaleDecorator(ProductComponent product) {
        super(product);
    }

    @Override
    public String getDescription() {
        return wrappedProduct.getDescription() + " [⚡ Flash Sale %" + (int) FLASH_SALE_DISCOUNT + "]";
    }

    @Override
    public double getPrice() {
        double originalPrice = wrappedProduct.getPrice();
        return Math.round(originalPrice * (1 - FLASH_SALE_DISCOUNT / 100.0) * 100.0) / 100.0;
    }

    public double getDiscountPercentage() {
        return FLASH_SALE_DISCOUNT;
    }
}
