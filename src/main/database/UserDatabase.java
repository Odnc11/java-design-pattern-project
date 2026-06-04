package main.database;

import main.models.User;
import main.models.Seller;
import main.models.Buyer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory user database for the e-commerce system.
 * 
 * Pre-loaded with 5 sellers (mock data, no registration) and 6 buyers.
 * Buyers can register through the UI; sellers are fixed.
 * 
 * Thread-safe ID generation via AtomicInteger.
 */
public class UserDatabase {
    private static final List<Seller> sellers = new ArrayList<>();
    private static final List<Buyer> buyers = new ArrayList<>();
    private static final AtomicInteger nextId = new AtomicInteger(100);

    static {
        initializeMockData();
    }

    private static void initializeMockData() {
        // --- 1 Store Admin (Instead of multiple marketplace sellers) ---
        sellers.add(new Seller(1, "Mağaza Yöneticisi", "admin@test.com", "123", "TeknoMağaza"));

        // --- 6 Mock Buyers (with varying free shipping thresholds) ---
        buyers.add(new Buyer(10, "Ahmet", "ahmet@test.com", "123", 10000.0)); // 10,000₺ threshold
        buyers.add(new Buyer(11, "Fatma", "fatma@test.com", "123", 5000.0)); // 5,000₺ threshold
        buyers.add(new Buyer(12, "Mehmet", "mehmet@test.com", "123", 2500.0)); // 2,500₺ threshold
        buyers.add(new Buyer(13, "Ayşe", "ayse@test.com", "123", 1000.0)); // 1,000₺ threshold
        buyers.add(new Buyer(14, "Ali", "ali@test.com", "123", 500.0)); // 500₺ threshold (VIP)
        buyers.add(new Buyer(15, "Zeynep", "zeynep@test.com", "123", 100.0)); // 100₺ threshold (Super VIP)
    }

    // --- Authentication ---

    /**
     * Authenticate a user by email, password, and role.
     * 
     * @return The matching User, or null if not found
     */
    public static User authenticate(String email, String password, User.Role role) {
        if (email == null || password == null || role == null)
            return null;

        if (role == User.Role.SELLER) {
            for (Seller s : sellers) {
                if (s.authenticate(email, password))
                    return s;
            }
        } else {
            for (Buyer b : buyers) {
                if (b.authenticate(email, password))
                    return b;
            }
        }
        return null;
    }

    // --- Registration (Buyer only) ---

    /**
     * Register a new buyer account.
     * 
     * @return The created Buyer, or null if email already exists
     */
    public static synchronized Buyer registerBuyer(String name, String email, String password) {
        if (name == null || email == null || password == null)
            return null;
        if (name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty())
            return null;

        // Check for duplicate email
        if (findByEmail(email) != null)
            return null;

        Buyer buyer = new Buyer(nextId.getAndIncrement(), name.trim(), email.trim(), password);
        buyers.add(buyer);
        return buyer;
    }

    // --- Queries ---

    /**
     * Find any user (seller or buyer) by email.
     */
    public static User findByEmail(String email) {
        if (email == null)
            return null;
        for (Seller s : sellers) {
            if (s.getEmail().equalsIgnoreCase(email))
                return s;
        }
        for (Buyer b : buyers) {
            if (b.getEmail().equalsIgnoreCase(email))
                return b;
        }
        return null;
    }

    /**
     * Find a seller by ID.
     */
    public static Seller findSellerById(int id) {
        for (Seller s : sellers) {
            if (s.getId() == id)
                return s;
        }
        return null;
    }

    /**
     * Find a buyer by ID.
     */
    public static Buyer findBuyerById(int id) {
        for (Buyer b : buyers) {
            if (b.getId() == id)
                return b;
        }
        return null;
    }

    public static List<Seller> getAllSellers() {
        return Collections.unmodifiableList(sellers);
    }

    public static List<Buyer> getAllBuyers() {
        return Collections.unmodifiableList(buyers);
    }
}
