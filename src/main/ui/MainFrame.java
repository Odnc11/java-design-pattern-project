package main.ui;

import main.patterns.observer.*;
import main.patterns.singleton.AppConfig;
import main.ui.panels.*;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame with CardLayout for panel switching.
 * 
 * Uses AppConfig (Singleton) for application-wide settings.
 * 
 * Navigation flow:
 *   Login → BuyerDashboard (buyer login)
 *   Login → SellerDashboard (seller login)
 *   Login → Register → Login
 *   Login → GuestBrowse → Login
 *   Any Dashboard → Logout → Login
 */
public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardContainer;

    // Panels
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private GuestBrowsePanel guestBrowsePanel;
    private BuyerDashboard buyerDashboard;
    private SellerDashboard sellerDashboard;

    // Shared state
    private final StockSubject stockSubject;
    private final StockObserver stockObserver;
    private final PriceNotificationObserver priceNotifObserver;
    private final AnalyticsObserver analyticsObserver;
    private final main.patterns.observer.DemandPricingObserver demandPricingObserver;

    // Panel names for CardLayout
    private static final String LOGIN = "LOGIN";
    private static final String REGISTER = "REGISTER";
    private static final String GUEST = "GUEST";
    private static final String BUYER = "BUYER";
    private static final String SELLER = "SELLER";

    public MainFrame() {
        // AppConfig Singleton — uygulama ayarlarını oku
        AppConfig config = AppConfig.getInstance();
        super.setTitle("🛒 " + config.getAppName() + " — Design Patterns Demo");

        // Konfigürasyonu konsola yazdır
        config.showConfig();

        // Initialize shared state
        stockSubject = new StockSubject();
        stockObserver = new StockObserver();
        priceNotifObserver = new PriceNotificationObserver();
        analyticsObserver = new AnalyticsObserver();
        demandPricingObserver = new main.patterns.observer.DemandPricingObserver();

        // Register observers
        stockSubject.registerObserver(stockObserver);
        stockSubject.registerObserver(priceNotifObserver);
        stockSubject.registerObserver(analyticsObserver);
        stockSubject.registerObserver(demandPricingObserver);

        // Initialize stock levels from product database
        initializeStockLevels();

        // Setup frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIUtils.BG_COLOR);

        // CardLayout container
        cardLayout = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(UIUtils.BG_COLOR);

        // Create panels
        createPanels();

        // Add panels to card layout
        cardContainer.add(loginPanel, LOGIN);
        cardContainer.add(registerPanel, REGISTER);
        cardContainer.add(guestBrowsePanel, GUEST);
        cardContainer.add(buyerDashboard, BUYER);
        cardContainer.add(sellerDashboard, SELLER);

        setContentPane(cardContainer);

        // Start at login
        cardLayout.show(cardContainer, LOGIN);
    }

    private void createPanels() {
        loginPanel = new LoginPanel(
                this::showBuyerDashboard,
                this::showSellerDashboard,
                () -> cardLayout.show(cardContainer, REGISTER),
                () -> cardLayout.show(cardContainer, GUEST)
        );

        registerPanel = new RegisterPanel(
                () -> cardLayout.show(cardContainer, LOGIN),
                () -> cardLayout.show(cardContainer, LOGIN)
        );

        guestBrowsePanel = new GuestBrowsePanel(
                () -> cardLayout.show(cardContainer, LOGIN),
                stockSubject
        );

        buyerDashboard = new BuyerDashboard(
                this::showLogin,
                stockSubject,
                priceNotifObserver
        );

        sellerDashboard = new SellerDashboard(
                this::showLogin,
                stockSubject,
                analyticsObserver
        );
    }

    private void showBuyerDashboard() {
        buyerDashboard.onLogin();
        cardLayout.show(cardContainer, BUYER);
    }

    private void showSellerDashboard() {
        sellerDashboard.onLogin();
        cardLayout.show(cardContainer, SELLER);
    }

    private void showLogin() {
        loginPanel.reset();
        cardLayout.show(cardContainer, LOGIN);
    }

    /**
     * Initialize stock levels in StockSubject from ProductDatabase.
     */
    private void initializeStockLevels() {
        for (main.models.Product product : main.database.ProductDatabase.getAllProducts()) {
            stockSubject.initializeStock(product.getName(), product.getStock());
            stockSubject.initializePrice(product.getName(), product.getPrice());
        }
    }
}
