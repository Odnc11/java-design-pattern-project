package main.database;

import main.models.Buyer;
import main.models.CartItem;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * In-memory order database for the e-commerce system.
 * 
 * Stores completed orders for analytics and order history.
 * Each order captures a snapshot of the cart at checkout time.
 */
public class OrderDatabase {
    private static final List<Order> orders = new ArrayList<>();
    private static int nextOrderId = 1;

    /**
     * Represents a completed order.
     */
    public static class Order {
        private final int orderId;
        private final int buyerId;
        private final String buyerName;
        private final List<OrderItem> items;
        private final double subtotal;
        private final double shipping;
        private final double total;
        private final LocalDateTime timestamp;

        public Order(int orderId, int buyerId, String buyerName,
                     List<OrderItem> items, double subtotal, double shipping, double total) {
            this.orderId = orderId;
            this.buyerId = buyerId;
            this.buyerName = buyerName;
            this.items = Collections.unmodifiableList(new ArrayList<>(items));
            this.subtotal = subtotal;
            this.shipping = shipping;
            this.total = total;
            this.timestamp = LocalDateTime.now();
        }

        public int getOrderId() { return orderId; }
        public int getBuyerId() { return buyerId; }
        public String getBuyerName() { return buyerName; }
        public List<OrderItem> getItems() { return items; }
        public double getSubtotal() { return subtotal; }
        public double getShipping() { return shipping; }
        public double getTotal() { return total; }
        public LocalDateTime getTimestamp() { return timestamp; }

        public String getFormattedTime() {
            return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        }

        @Override
        public String toString() {
            return String.format("Order#%d [%s] %s - %.2f₺ (%d ürün)",
                    orderId, getFormattedTime(), buyerName, total, items.size());
        }
    }

    /**
     * Snapshot of a single item in an order.
     */
    public static class OrderItem {
        private final int productId;
        private final String productName;
        private final String description;
        private final double unitPrice;
        private final int quantity;
        private final double subtotal;

        public OrderItem(int productId, String productName, String description,
                         double unitPrice, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.description = description;
            this.unitPrice = unitPrice;
            this.quantity = quantity;
            this.subtotal = Math.round(unitPrice * quantity * 100.0) / 100.0;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getDescription() { return description; }
        public double getUnitPrice() { return unitPrice; }
        public int getQuantity() { return quantity; }
        public double getSubtotal() { return subtotal; }

        @Override
        public String toString() {
            return String.format("%s x%d = %.2f₺", productName, quantity, subtotal);
        }
    }

    // --- Order Operations ---

    /**
     * Create an order from cart items.
     * @return The created Order
     */
    public static synchronized Order createOrder(Buyer buyer, List<CartItem> cartItems,
                                                  double subtotal, double shipping, double total) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            orderItems.add(new OrderItem(
                    item.getOriginalProduct().getId(),
                    item.getDecoratedProduct().getName(),
                    item.getDecoratedProduct().getDescription(),
                    item.getDecoratedProduct().getPrice(),
                    item.getQuantity()
            ));
        }

        Order order = new Order(nextOrderId++, buyer.getId(), buyer.getName(),
                orderItems, subtotal, shipping, total);
        orders.add(order);
        System.out.println("📦 Sipariş oluşturuldu: " + order);
        return order;
    }

    /**
     * Get all orders for a specific buyer.
     */
    public static List<Order> getOrdersByBuyer(int buyerId) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getBuyerId() == buyerId) result.add(o);
        }
        return result;
    }

    /**
     * Get all orders (for seller analytics).
     */
    public static List<Order> getAllOrders() {
        return Collections.unmodifiableList(orders);
    }

    /**
     * Get total revenue across all orders.
     */
    public static double getTotalRevenue() {
        double total = 0;
        for (Order o : orders) {
            total += o.getTotal();
        }
        return total;
    }

    /**
     * Get total number of items sold.
     */
    public static int getTotalItemsSold() {
        int count = 0;
        for (Order o : orders) {
            for (OrderItem item : o.getItems()) {
                count += item.getQuantity();
            }
        }
        return count;
    }
}
