package main.patterns.decorator;

import main.patterns.observer.DemandPricingObserver;

/**
 * Concrete Decorator - Dynamic Pricing (Arz-Talep / Supply-Demand)
 * 
 * Dynamically adjusts the price of a product based on supply and demand.
 * It queries the DemandPricingObserver for the current modifier at runtime.
 */
public class DynamicPricingDecorator extends ProductDecorator {
    private final String productName;

    /**
     * @param product      The product to decorate
     * @param productName  The name of the product to query the observer
     */
    public DynamicPricingDecorator(ProductComponent product, String productName) {
        super(product);
        this.productName = productName;
    }

    @Override
    public String getDescription() {
        double modifier = DemandPricingObserver.getModifier(productName);
        String suffix = "";
        if (modifier > 0) {
            suffix = " [🔥 Yüksek Talep]";
        } else if (modifier < 0) {
            suffix = " [📉 Stok Fazlası]";
        }
        return super.getDescription() + suffix;
    }

    @Override
    public double getPrice() {
        double originalPrice = super.getPrice();
        double modifier = DemandPricingObserver.getModifier(productName);
        double newPrice = originalPrice * (1 + modifier / 100.0);
        
        // Ensure price never drops below 0
        return Math.max(0, Math.round(newPrice * 100.0) / 100.0);
    }
}
