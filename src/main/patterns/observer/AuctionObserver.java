package main.patterns.observer;

/**
 * Observer Interface for Auction/Demand events - Observer Design Pattern (Behavioral)
 * 
 * Extends the Observer pattern to handle demand-driven price changes.
 * When a product's demand (click count) changes, all registered 
 * AuctionObservers are notified with the new dynamic price.
 * 
 * This is separate from the stock Observer interface to follow
 * the Interface Segregation Principle (ISP) - each observer type
 * has its own focused interface.
 */
public interface AuctionObserver {
    /**
     * Called when a product's demand or auction state changes.
     * @param productName  Name of the product
     * @param oldPrice     Previous price
     * @param newPrice     New dynamically calculated price
     * @param demandCount  Current demand (click/view count)
     * @param currentStock Current stock level
     */
    void onPriceChanged(String productName, double oldPrice, double newPrice,
                        int demandCount, int currentStock);

    /**
     * Called when a new bid is placed on a product.
     * @param productName Name of the product
     * @param bidderName  Name of the user who placed the bid
     * @param bidAmount   The bid amount
     * @param isHighest   Whether this is currently the highest bid
     */
    void onNewBid(String productName, String bidderName, double bidAmount, boolean isHighest);
}
