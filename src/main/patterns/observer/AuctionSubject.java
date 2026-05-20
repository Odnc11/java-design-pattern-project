package main.patterns.observer;

/**
 * Subject Interface for Auction/Demand system - Observer Design Pattern
 * 
 * Defines the contract for observable objects that track product demand
 * and auction events. Follows DIP - depends on AuctionObserver abstraction.
 */
public interface AuctionSubject {
    /**
     * Register an observer to receive auction/demand notifications.
     * @param observer The AuctionObserver to register
     */
    void registerAuctionObserver(AuctionObserver observer);

    /**
     * Remove an observer from auction notifications.
     * @param observer The AuctionObserver to remove
     */
    void removeAuctionObserver(AuctionObserver observer);

    /**
     * Notify all observers of a price change due to demand.
     */
    void notifyPriceChange(String productName, double oldPrice, double newPrice,
                           int demandCount, int currentStock);

    /**
     * Notify all observers of a new bid.
     */
    void notifyNewBid(String productName, String bidderName, double bidAmount, boolean isHighest);
}
