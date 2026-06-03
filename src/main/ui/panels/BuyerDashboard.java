package main.ui.panels;

import main.patterns.observer.PriceNotificationObserver;
import main.patterns.observer.StockSubject;
import main.utils.SessionManager;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Buyer Dashboard - main interface after buyer login.
 * 
 * Layout:
 * ┌────────────────────────────────────────────────────────────┐
 * │ Header: User info + Logout                                 │
 * ├──────────────────────────────┬─────────────────────────────┤
 * │ Left (Tabbed):               │ Right:                      │
 * │  - Ürün Kataloğu            │  ShoppingCartPanel           │
 * │  - Bildirimler              │                              │
 * ├──────────────────────────────┴─────────────────────────────┤
 * │ Bottom: NotificationPanel (Observer events)                 │
 * └────────────────────────────────────────────────────────────┘
 */
public class BuyerDashboard extends JPanel {
    private final Runnable onLogout;
    private final StockSubject stockSubject;
    private final PriceNotificationObserver notifObserver;

    private ProductCatalogPanel catalogPanel;
    private ShoppingCartPanel cartPanel;
    private NotificationPanel notificationPanel;
    private JLabel userInfoLabel;

    public BuyerDashboard(Runnable onLogout, StockSubject stockSubject,
                          PriceNotificationObserver notifObserver) {
        this.onLogout = onLogout;
        this.stockSubject = stockSubject;
        this.notifObserver = notifObserver;

        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_COLOR);

        // Create panels
        catalogPanel = new ProductCatalogPanel(stockSubject);
        cartPanel = new ShoppingCartPanel(stockSubject, catalogPanel);
        notificationPanel = new NotificationPanel(notifObserver);

        add(createHeader(), BorderLayout.NORTH);

        // Main content: catalog + cart
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                catalogPanel, cartPanel);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.65);
        splitPane.setBackground(UIUtils.BG_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(3);

        add(splitPane, BorderLayout.CENTER);
        add(notificationPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel appTitle = UIUtils.createLabel("🛒 E-Commerce", UIUtils.SUBTITLE_FONT, UIUtils.BUYER_ACCENT);
        userInfoLabel = UIUtils.createLabel("", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);

        left.add(appTitle);
        left.add(UIUtils.createLabel("  |  ", UIUtils.BODY_FONT, UIUtils.BORDER_COLOR));
        left.add(userInfoLabel);

        JButton logoutBtn = UIUtils.createStyledButton("🚪 Çıkış Yap", UIUtils.DANGER, 120);
        logoutBtn.addActionListener(e -> {
            SessionManager.logout();
            onLogout.run();
        });

        header.add(left, BorderLayout.WEST);
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    /**
     * Refresh all panels when user logs in.
     */
    public void onLogin() {
        if (SessionManager.isBuyer()) {
            main.models.Buyer buyer = SessionManager.getCurrentBuyer();
            userInfoLabel.setText("👤 " + buyer.getName() + " (Alıcı)");
            cartPanel.bindToCurrentUser();
            catalogPanel.refreshProducts();
            
            showWelcomePopup(buyer);
        }
    }
    
    private void showWelcomePopup(main.models.Buyer buyer) {
        StringBuilder msg = new StringBuilder();
        msg.append("👋 Hoş geldin, ").append(buyer.getName()).append("!\n\n");
        
        msg.append("🚚 Kargo Fırsatın:\n");
        msg.append("Sepet tutarın ").append(buyer.getFreeShippingThreshold()).append(" ₺'yi geçtiğinde kargo BEDAVA!\n\n");
        
        msg.append("🔥 GÜNÜN FIRSATLARI:\n");
        boolean hasDiscount = false;
        
        for (main.models.Product p : main.database.ProductDatabase.getAllProducts()) {
            main.patterns.decorator.ProductComponent decorated = main.database.ProductDatabase.getDecoratedProduct(p.getId());
            double basePrice = p.getPrice();
            double newPrice = decorated.getPrice();
            
            if (newPrice < basePrice) {
                hasDiscount = true;
                double discountPercent = ((basePrice - newPrice) / basePrice) * 100;
                
                msg.append("• ").append(p.getName())
                   .append(" -> %").append((int)Math.round(discountPercent)).append(" İndirim! ")
                   .append(String.format("(%.2f ₺ yerine %.2f ₺)", basePrice, newPrice));
                   
                if (decorated.hasFreeShipping()) {
                    msg.append(" 📦 [Ücretsiz Kargo]");
                }
                msg.append("\n");
            }
        }
        
        if (!hasDiscount) {
            msg.append("Şu an için özel bir fırsat bulunmuyor, kataloga göz at!\n");
        }
        
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, msg.toString(), "🔔 Özel Bildiriminiz Var!", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}
