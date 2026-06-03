package main.utils;

import main.models.User;
import main.models.Seller;
import main.models.Buyer;
import main.patterns.singleton.ShoppingCart;

/**
 * Manages user session state (login/logout).
 * 
 * Static utility class - tracks the currently logged in user.
 * When a user logs out, their shopping cart is cleared.
 */
public class SessionManager {
    private static User currentUser = null;

    private SessionManager() {} // Prevent instantiation

    /**
     * Log in a user.
     */
    public static void login(User user) {
        currentUser = user;
        System.out.println("✅ Giriş yapıldı: " + user.getName() + " (" + user.getRole() + ")");
    }

    /**
     * Log out the current user.
     * Clears the buyer's shopping cart if applicable.
     */
    public static void logout() {
        if (currentUser != null) {
            System.out.println("🚪 Çıkış yapıldı: " + currentUser.getName());
            // Sepeti silme (clearInstance() kaldırıldı), böylece kullanıcı geri döndüğünde sepeti durur.
            currentUser = null;
        }
    }

    /**
     * @return The currently logged in user, or null
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * @return true if a user is logged in
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * @return true if the current user is a Seller
     */
    public static boolean isSeller() {
        return currentUser instanceof Seller;
    }

    /**
     * @return true if the current user is a Buyer
     */
    public static boolean isBuyer() {
        return currentUser instanceof Buyer;
    }

    /**
     * @return The current user as Seller, or null
     */
    public static Seller getCurrentSeller() {
        return currentUser instanceof Seller ? (Seller) currentUser : null;
    }

    /**
     * @return The current user as Buyer, or null
     */
    public static Buyer getCurrentBuyer() {
        return currentUser instanceof Buyer ? (Buyer) currentUser : null;
    }
}
