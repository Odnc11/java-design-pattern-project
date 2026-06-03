package main.ui.panels;

import main.database.ProductDatabase;
import main.database.OrderDatabase;
import main.models.Product;
import main.models.Seller;
import main.patterns.observer.StockSubject;
import main.patterns.observer.AnalyticsObserver;
import main.utils.SessionManager;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Seller Dashboard - main interface after seller login.
 * 
 * Layout:
 * ┌────────────────────────────────────────────┐
 * │ Header: Store name + Seller info + Logout   │
 * ├────────────────────────────────────────────┤
 * │ Tabbed:                                     │
 * │  - Ürün Yönetimi (product list + stock)    │
 * │  - İndirim Yönetimi (Decorator Pattern)    │
 * │  - Satış Analitik (Observer analytics)      │
 * └────────────────────────────────────────────┘
 */
public class SellerDashboard extends JPanel {
    private final Runnable onLogout;
    private final StockSubject stockSubject;
    private final AnalyticsObserver analyticsObserver;

    private DiscountManagementPanel discountPanel;
    private JPanel productManagementPanel;
    private JPanel analyticsPanel;
    private JLabel userInfoLabel;
    private JTabbedPane tabbedPane;

    public SellerDashboard(Runnable onLogout, StockSubject stockSubject,
                           AnalyticsObserver analyticsObserver) {
        this.onLogout = onLogout;
        this.stockSubject = stockSubject;
        this.analyticsObserver = analyticsObserver;

        setLayout(new BorderLayout());
        setBackground(UIUtils.BG_COLOR);

        add(createHeader(), BorderLayout.NORTH);

        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(UIUtils.CARD_BG);
        tabbedPane.setForeground(UIUtils.TEXT_PRIMARY);
        tabbedPane.setFont(UIUtils.BODY_FONT);

        productManagementPanel = createProductManagementPanel();
        discountPanel = new DiscountManagementPanel(stockSubject, analyticsObserver);
        analyticsPanel = createAnalyticsPanel();

        tabbedPane.addTab("📦 Ürün Yönetimi", productManagementPanel);
        tabbedPane.addTab("🏷️ İndirim Yönetimi", discountPanel);
        tabbedPane.addTab("📊 Satış Analitik", analyticsPanel);

        tabbedPane.addChangeListener(e -> refreshCurrentTab());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel appTitle = UIUtils.createLabel("🏪 Seller Panel", UIUtils.SUBTITLE_FONT, UIUtils.SELLER_ACCENT);
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

    private JPanel createProductManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = UIUtils.createLabel("📦 Ürünlerim", UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        // Will be populated on login
        return panel;
    }

    private JPanel createAnalyticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(UIUtils.BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JLabel title = UIUtils.createLabel("📊 Satış Analitik (Observer Pattern → AnalyticsObserver)",
                UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);
        panel.add(title, BorderLayout.NORTH);

        return panel;
    }

    public void onLogin() {
        Seller seller = SessionManager.getCurrentSeller();
        if (seller == null) return;

        userInfoLabel.setText("👤 " + seller.getName() + " | 🏪 " + seller.getStoreName());

        refreshProductManagement();
        discountPanel.refresh();
        refreshAnalytics();
    }

    private void refreshCurrentTab() {
        int idx = tabbedPane.getSelectedIndex();
        switch (idx) {
            case 0: refreshProductManagement(); break;
            case 1: discountPanel.refresh(); break;
            case 2: refreshAnalytics(); break;
        }
    }

    private void refreshProductManagement() {
        Seller seller = SessionManager.getCurrentSeller();
        if (seller == null) return;

        productManagementPanel.removeAll();

        JLabel title = UIUtils.createLabel("📦 Ürünlerim (" + seller.getStoreName() + ")",
                UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);
        productManagementPanel.add(title, BorderLayout.NORTH);

        JPanel grid = new JPanel();
        grid.setLayout(new BoxLayout(grid, BoxLayout.Y_AXIS));
        grid.setBackground(UIUtils.BG_COLOR);

        List<Product> myProducts = ProductDatabase.getProductsBySeller(seller.getId());
        for (Product product : myProducts) {
            grid.add(createProductManagementCard(product));
            grid.add(Box.createVerticalStrut(8));
        }

        JScrollPane scroll = new JScrollPane(grid);
        scroll.setBackground(UIUtils.BG_COLOR);
        scroll.getViewport().setBackground(UIUtils.BG_COLOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        productManagementPanel.add(scroll, BorderLayout.CENTER);
        productManagementPanel.revalidate();
        productManagementPanel.repaint();
    }

    private JPanel createProductManagementCard(Product product) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UIUtils.CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLabel = UIUtils.createLabel(product.getName(),
                new Font("Segoe UI", Font.BOLD, 14), UIUtils.TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        int stock = stockSubject.getStock(product.getName());
        JLabel stockLabel = UIUtils.createLabel(
                String.format("Baz Fiyat: %.2f₺  |  %s  |  📁 %s",
                        product.getPrice(), UIUtils.getStockText(stock), product.getCategory()),
                UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);
        stockLabel.setAlignmentX(LEFT_ALIGNMENT);

        double decoratedPrice = ProductDatabase.getDecoratedPrice(product.getId());
        if (Math.abs(decoratedPrice - product.getPrice()) > 0.01) {
            JLabel decoPrice = UIUtils.createLabel(
                    String.format("İndirimli: %.2f₺", decoratedPrice),
                    new Font("Segoe UI", Font.BOLD, 12), UIUtils.WARNING);
            decoPrice.setAlignmentX(LEFT_ALIGNMENT);
            info.add(nameLabel);
            info.add(Box.createVerticalStrut(3));
            info.add(decoPrice);
            info.add(Box.createVerticalStrut(2));
            info.add(stockLabel);
        } else {
            info.add(nameLabel);
            info.add(Box.createVerticalStrut(3));
            info.add(stockLabel);
        }

        // Stock update button
        JButton stockBtn = UIUtils.createStyledButton("📦 Stok Güncelle", UIUtils.SELLER_ACCENT, 130);
        stockBtn.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this,
                    product.getName() + " için yeni stok:", "Stok Güncelle", JOptionPane.QUESTION_MESSAGE);
            if (input != null) {
                try {
                    int newStock = Integer.parseInt(input.trim());
                    if (newStock >= 0) {
                        stockSubject.setStock(product.getName(), newStock);
                        product.setStock(newStock);
                        refreshProductManagement();
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Geçersiz sayı!", "Hata", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        card.add(info, BorderLayout.CENTER);
        card.add(stockBtn, BorderLayout.EAST);
        return card;
    }

    private void refreshAnalytics() {
        analyticsPanel.removeAll();

        JLabel title = UIUtils.createLabel("📊 Satış Analitik (Observer Pattern → AnalyticsObserver)",
                UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);
        analyticsPanel.add(title, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIUtils.BG_COLOR);

        // Summary cards
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 12, 0));
        summaryRow.setOpaque(false);
        summaryRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        summaryRow.add(createStatCard("📦 Toplam Sipariş", String.valueOf(OrderDatabase.getAllOrders().size()), UIUtils.ACCENT));
        summaryRow.add(createStatCard("💰 Toplam Gelir", String.format("%.2f₺", OrderDatabase.getTotalRevenue()), UIUtils.SUCCESS));
        summaryRow.add(createStatCard("📊 Observer Olayları", String.valueOf(analyticsObserver.getTotalEventCount()), UIUtils.WARNING));

        content.add(summaryRow);
        content.add(Box.createVerticalStrut(12));

        // Event log
        JLabel logTitle = UIUtils.createLabel("📜 Tüm Olaylar (AnalyticsObserver)", 
                new Font("Segoe UI", Font.BOLD, 13), UIUtils.TEXT_PRIMARY);
        logTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(logTitle);
        content.add(Box.createVerticalStrut(6));

        JTextArea logArea = new JTextArea(15, 80);
        logArea.setFont(UIUtils.MONO_FONT);
        logArea.setBackground(UIUtils.CARD_BG);
        logArea.setForeground(UIUtils.TEXT_SECONDARY);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        logArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        List<String> logs = analyticsObserver.getFormattedLog();
        StringBuilder sb = new StringBuilder();
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        logArea.setText(sb.length() > 0 ? sb.toString() : "Henüz olay kaydedilmedi.");

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR));
        logScroll.setAlignmentX(LEFT_ALIGNMENT);
        content.add(logScroll);

        JScrollPane mainScroll = new JScrollPane(content);
        mainScroll.setBackground(UIUtils.BG_COLOR);
        mainScroll.getViewport().setBackground(UIUtils.BG_COLOR);
        mainScroll.setBorder(BorderFactory.createEmptyBorder());

        analyticsPanel.add(mainScroll, BorderLayout.CENTER);
        analyticsPanel.revalidate();
        analyticsPanel.repaint();
    }

    private JPanel createStatCard(String label, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIUtils.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel valueLabel = UIUtils.createLabel(value, UIUtils.SUBTITLE_FONT, color);
        valueLabel.setAlignmentX(CENTER_ALIGNMENT);

        JLabel labelComp = UIUtils.createLabel(label, UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);
        labelComp.setAlignmentX(CENTER_ALIGNMENT);

        card.add(valueLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(labelComp);

        return card;
    }
}
