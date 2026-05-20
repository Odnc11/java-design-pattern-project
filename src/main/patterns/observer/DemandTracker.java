package main.patterns.observer;

import main.models.Bid;
import main.models.Product;
import main.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete Subject - Demand-Driven Auction Manager (Observer Pattern)
 * 
 * Manages product demand tracking, dynamic pricing, and the auction
 * bidding system. This is the core of the demand-driven e-commerce model:
 * 
 * PRICING ALGORITHM:
 *   demandRatio  = clickCount / DEMAND_THRESHOLD
 *   scarcityRatio = 1.0 / (currentStock + 1)
 *   priceMultiplier = 1 + (demandRatio * scarcityRatio * SCALE_FACTOR)
 * 
 * When a product gets many clicks AND has low stock, its price increases.
 * Example: 20 clicks + 2 stock → ~33% price increase
 * 
 * OBSERVER PATTERN APPLICATION:
 *   - Registers AuctionObservers for price/bid events
 *   - Notifies all observers when demand changes prices
 *   - Notifies all observers when new bids are placed
 * 
 * SOLID Principles:
 *   - SRP: Manages demand tracking and auction logic only
 *   - OCP: New observers can be added without modification
 *   - DIP: Depends on AuctionObserver/AuctionSubject abstractions
 */
public class DemandTracker implements AuctionSubject {
    
    // --- Configuration Constants ---
    /** Number of clicks before demand starts affecting price */
    private static final int DEMAND_THRESHOLD = 5;
    /** Maximum price multiplier (e.g., 3.0 = max 3x the original price) */
    private static final double MAX_MULTIPLIER = 3.0;
    /** How aggressively demand affects price (0.0 - 1.0) */
    private static final double SCALE_FACTOR = 0.5;

    // --- State ---
    private final List<AuctionObserver> observers;
    /** Product name → click/view count */
    private final Map<String, Integer> demandCounts;
    /** Product name → original base price (before any demand adjustment) */
    private final Map<String, Double> basePrices;
    /** Product name → current dynamic price */
    private final Map<String, Double> currentPrices;
    /** Product name → list of bids */
    private final Map<String, List<Bid>> productBids;
    /** Product name → auction active flag */
    private final Map<String, Boolean> auctionActive;

    private final StockSubject stockSubject;
    private int bidCounter = 0;

    public DemandTracker(StockSubject stockSubject) {
        this.stockSubject = stockSubject;
        this.observers = new ArrayList<>();
        this.demandCounts = new HashMap<>();
        this.basePrices = new HashMap<>();
        this.currentPrices = new HashMap<>();
        this.productBids = new HashMap<>();
        this.auctionActive = new HashMap<>();
    }

    // === AuctionSubject Implementation ===

    @Override
    public void registerAuctionObserver(AuctionObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeAuctionObserver(AuctionObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyPriceChange(String productName, double oldPrice, double newPrice,
                                  int demandCount, int currentStock) {
        for (AuctionObserver obs : new ArrayList<>(observers)) {
            obs.onPriceChanged(productName, oldPrice, newPrice, demandCount, currentStock);
        }
    }

    @Override
    public void notifyNewBid(String productName, String bidderName, 
                             double bidAmount, boolean isHighest) {
        for (AuctionObserver obs : new ArrayList<>(observers)) {
            obs.onNewBid(productName, bidderName, bidAmount, isHighest);
        }
    }

    // === Product Initialization ===

    /**
     * Initialize a product for demand tracking.
     * Must be called before tracking demand or bids.
     */
    public void initializeProduct(String productName, double basePrice) {
        basePrices.put(productName, basePrice);
        currentPrices.put(productName, basePrice);
        demandCounts.put(productName, 0);
        productBids.put(productName, new ArrayList<>());
        auctionActive.put(productName, false);
    }

    // === Demand Tracking ===

    /**
     * Record a click/view on a product.
     * This increases demand and may trigger a price change.
     * 
     * Called every time a user interacts with a product (e.g., clicks
     * to view details, hovers over the product card, etc.)
     */
    public void recordDemand(String productName) {
        int oldDemand = demandCounts.getOrDefault(productName, 0);
        int newDemand = oldDemand + 1;
        demandCounts.put(productName, newDemand);

        // Recalculate dynamic price
        recalculatePrice(productName);

        // Auto-activate auction when demand is high enough
        if (newDemand >= DEMAND_THRESHOLD && !isAuctionActive(productName)) {
            activateAuction(productName);
        }
    }

    /**
     * Recalculate the dynamic price based on demand and stock.
     * 
     * Formula:
     *   demandRatio   = clickCount / DEMAND_THRESHOLD
     *   scarcityRatio = 1.0 / (currentStock + 1)
     *   multiplier    = 1 + (demandRatio × scarcityRatio × SCALE_FACTOR)
     *   newPrice      = basePrice × min(multiplier, MAX_MULTIPLIER)
     */
    public void recalculatePrice(String productName) {
        double basePrice = basePrices.getOrDefault(productName, 0.0);
        int demand = demandCounts.getOrDefault(productName, 0);
        int stock = stockSubject.getStock(productName);
        double oldPrice = currentPrices.getOrDefault(productName, basePrice);

        // Calculate demand ratio (how much demand exceeds threshold)
        double demandRatio = (double) demand / DEMAND_THRESHOLD;

        // Calculate scarcity ratio (lower stock = higher ratio)
        double scarcityRatio = 1.0 / (stock + 1);

        // Calculate price multiplier
        double multiplier = 1.0 + (demandRatio * scarcityRatio * SCALE_FACTOR);
        multiplier = Math.min(multiplier, MAX_MULTIPLIER);

        // Apply multiplier to base price
        double newPrice = Math.round(basePrice * multiplier * 100.0) / 100.0;
        currentPrices.put(productName, newPrice);

        // Notify observers if price changed significantly (> 0.01₺)
        if (Math.abs(newPrice - oldPrice) > 0.01) {
            notifyPriceChange(productName, oldPrice, newPrice, demand, stock);
        }
    }

    // === Auction Management ===

    /**
     * Activate the auction for a product (when demand is high).
     */
    public void activateAuction(String productName) {
        auctionActive.put(productName, true);
        System.out.println("🔥 MÜZAYEDE AKTİF: " + productName + 
                " (Talep: " + getDemandCount(productName) + " tıklama)");
    }

    /**
     * Deactivate the auction for a product.
     */
    public void deactivateAuction(String productName) {
        auctionActive.put(productName, false);
    }

    /**
     * Place a bid on a product.
     * Bid must be higher than the current price AND the highest existing bid.
     * 
     * @return The created Bid if successful, null if bid was too low
     */
    public Bid placeBid(String productName, User user, double bidAmount) {
        double currentPrice = getCurrentPrice(productName);
        double highestBid = getHighestBidAmount(productName);
        double minimumBid = Math.max(currentPrice, highestBid);

        if (bidAmount <= minimumBid) {
            return null; // Bid too low
        }

        Bid bid = new Bid(++bidCounter, user, findProductByName(productName), bidAmount);
        productBids.get(productName).add(bid);

        // Update current price to highest bid
        double oldPrice = currentPrices.getOrDefault(productName, currentPrice);
        currentPrices.put(productName, bidAmount);

        // Notify observers
        boolean isHighest = true;
        notifyNewBid(productName, user.getName(), bidAmount, isHighest);
        notifyPriceChange(productName, oldPrice, bidAmount,
                getDemandCount(productName), stockSubject.getStock(productName));

        return bid;
    }

    // === Getters ===

    public int getDemandCount(String productName) {
        return demandCounts.getOrDefault(productName, 0);
    }

    public double getBasePrice(String productName) {
        return basePrices.getOrDefault(productName, 0.0);
    }

    public double getCurrentPrice(String productName) {
        return currentPrices.getOrDefault(productName, 
                basePrices.getOrDefault(productName, 0.0));
    }

    /**
     * Get the current price multiplier for a product.
     * @return multiplier (e.g., 1.33 means 33% price increase)
     */
    public double getPriceMultiplier(String productName) {
        double base = basePrices.getOrDefault(productName, 1.0);
        double current = currentPrices.getOrDefault(productName, base);
        return base > 0 ? current / base : 1.0;
    }

    /**
     * Get the percentage increase from the base price.
     * @return percentage (e.g., 33.0 for 33% increase)
     */
    public double getPriceIncreasePercent(String productName) {
        return (getPriceMultiplier(productName) - 1.0) * 100.0;
    }

    public boolean isAuctionActive(String productName) {
        return auctionActive.getOrDefault(productName, false);
    }

    public List<Bid> getBidsForProduct(String productName) {
        return Collections.unmodifiableList(
                productBids.getOrDefault(productName, new ArrayList<>()));
    }

    public double getHighestBidAmount(String productName) {
        List<Bid> bids = productBids.getOrDefault(productName, new ArrayList<>());
        double highest = 0;
        for (Bid bid : bids) {
            if (bid.getAmount() > highest) {
                highest = bid.getAmount();
            }
        }
        return highest;
    }

    public Bid getHighestBid(String productName) {
        List<Bid> bids = productBids.getOrDefault(productName, new ArrayList<>());
        Bid highest = null;
        for (Bid bid : bids) {
            if (highest == null || bid.getAmount() > highest.getAmount()) {
                highest = bid;
            }
        }
        return highest;
    }

    public int getTotalBidCount(String productName) {
        return productBids.getOrDefault(productName, new ArrayList<>()).size();
    }

    /**
     * Get a demand level label for UI display.
     */
    public String getDemandLevel(String productName) {
        int demand = getDemandCount(productName);
        if (demand == 0) return "Talep Yok";
        if (demand < 3) return "Düşük Talep";
        if (demand < DEMAND_THRESHOLD) return "Orta Talep";
        if (demand < DEMAND_THRESHOLD * 2) return "Yüksek Talep";
        return "Çok Yüksek Talep";
    }

    /**
     * Get demand heat level (0-4) for color coding in UI.
     */
    public int getDemandHeatLevel(String productName) {
        int demand = getDemandCount(productName);
        if (demand == 0) return 0;
        if (demand < 3) return 1;
        if (demand < DEMAND_THRESHOLD) return 2;
        if (demand < DEMAND_THRESHOLD * 2) return 3;
        return 4;
    }

    public Map<String, Integer> getAllDemandCounts() {
        return Collections.unmodifiableMap(demandCounts);
    }

    // --- Helper to find product by name (for bid creation) ---
    private Product findProductByName(String name) {
        // Returns a placeholder - actual product reference comes from the caller
        return new Product(0, name, "", getBasePrice(name), 0, "");
    }
}
