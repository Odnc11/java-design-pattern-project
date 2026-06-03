package main.ui.panels;

import main.database.ProductDatabase;
import main.models.Product;
import main.models.Seller;
import main.patterns.decorator.*;
import main.patterns.observer.StockSubject;
import main.patterns.observer.AnalyticsObserver;
import main.utils.SessionManager;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Discount management panel for sellers.
 * Allows applying and removing decorators on seller's own products.
 * Demonstrates Decorator pattern with order-dependent pricing.
 */
public class DiscountManagementPanel extends JPanel {
    private final StockSubject stockSubject;
    private final AnalyticsObserver analyticsObserver;
    private JPanel productListPanel;
    private JTextArea logArea;

    public DiscountManagementPanel(StockSubject stockSubject, AnalyticsObserver analyticsObserver) {
        this.stockSubject = stockSubject;
        this.analyticsObserver = analyticsObserver;

        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createProductListScroll(), createInfoPanel());
        split.setDividerLocation(500);
        split.setResizeWeight(0.6);
        split.setBackground(UIUtils.BG_COLOR);
        split.setBorder(null);
        split.setDividerSize(3);
        add(split, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.BG_COLOR);

        JLabel title = UIUtils.createLabel("🏷️ İndirim Yönetimi (Decorator Pattern)",
                UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);
        JLabel subtitle = UIUtils.createLabel("Ürünlerinize indirim, kargo ve flash sale uygulayın",
                UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setOpaque(false);
        left.add(title);
        left.add(subtitle);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private JScrollPane createProductListScroll() {
        productListPanel = new JPanel();
        productListPanel.setLayout(new BoxLayout(productListPanel, BoxLayout.Y_AXIS));
        productListPanel.setBackground(UIUtils.BG_COLOR);

        JScrollPane scroll = new JScrollPane(productListPanel);
        scroll.setBackground(UIUtils.BG_COLOR);
        scroll.getViewport().setBackground(UIUtils.BG_COLOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UIUtils.BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JLabel infoTitle = UIUtils.createLabel("📊 Decorator Sırası Önemli!",
                new Font("Segoe UI", Font.BOLD, 14), UIUtils.TEXT_PRIMARY);

        JTextArea infoArea = new JTextArea();
        infoArea.setFont(UIUtils.MONO_FONT);
        infoArea.setBackground(UIUtils.CARD_BG);
        infoArea.setForeground(UIUtils.TEXT_SECONDARY);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        infoArea.setText(
            "╔═══════════════════════════════╗\n" +
            "║  DECORATOR SIRASI FARKI       ║\n" +
            "╚═══════════════════════════════╝\n\n" +
            "ÖRNEK: Laptop 10000₺\n\n" +
            "SIRA 1: %15 → 500₺\n" +
            "  10000 × 0.85 = 8500₺\n" +
            "  8500 - 500 = 8000₺\n\n" +
            "SIRA 2: 500₺ → %15\n" +
            "  10000 - 500 = 9500₺\n" +
            "  9500 × 0.85 = 8075₺\n\n" +
            "FARK: 75₺!\n\n" +
            "─────────────────────────────────\n" +
            "Decorator Türleri:\n" +
            "  %  → PercentageDiscountDecorator\n" +
            "  ₺  → FixedAmountDiscountDecorator\n" +
            "  ⚡ → FlashSaleDecorator (%10)\n" +
            "  🚚 → FreeShippingDecorator\n\n" +
            "─────────────────────────────────\n" +
            "📌 Seller'ın uyguladığı indirim\n" +
            "   TÜM BUYER'lar tarafından\n" +
            "   görülür (GLOBAL)\n" +
            "📌 Buyer'ın kuponu SADECE\n" +
            "   kendi sepetini etkiler"
        );

        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR));

        // Analytics log
        JLabel logTitle = UIUtils.createLabel("📜 Analytics Log (Observer)",
                new Font("Segoe UI", Font.BOLD, 12), UIUtils.WARNING);

        logArea = new JTextArea(5, 30);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 10));
        logArea.setBackground(new Color(15, 15, 30));
        logArea.setForeground(UIUtils.TEXT_SECONDARY);
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR));
        logScroll.setPreferredSize(new Dimension(0, 120));

        panel.add(infoTitle, BorderLayout.NORTH);
        panel.add(infoScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(0, 4));
        bottomPanel.setOpaque(false);
        bottomPanel.add(logTitle, BorderLayout.NORTH);
        bottomPanel.add(logScroll, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    public void refresh() {
        productListPanel.removeAll();

        Seller seller = SessionManager.getCurrentSeller();
        if (seller == null) return;

        List<Product> myProducts = ProductDatabase.getProductsBySeller(seller.getId());
        for (Product product : myProducts) {
            productListPanel.add(createProductDiscountCard(product));
            productListPanel.add(Box.createVerticalStrut(8));
        }

        // Update analytics log
        List<String> logs = analyticsObserver.getFormattedLog();
        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, logs.size() - 20);
        for (int i = start; i < logs.size(); i++) {
            sb.append(logs.get(i)).append("\n");
        }
        logArea.setText(sb.toString());

        productListPanel.revalidate();
        productListPanel.repaint();
    }

    private JPanel createProductDiscountCard(Product product) {
        ProductComponent decorated = ProductDatabase.getDecoratedProduct(product.getId());
        double decoratedPrice = decorated != null ? decorated.getPrice() : product.getPrice();
        boolean hasDiscount = Math.abs(decoratedPrice - product.getPrice()) > 0.01;
        boolean hasFreeShip = decorated != null && decorated.hasFreeShipping();

        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(UIUtils.CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(hasDiscount ? UIUtils.WARNING : UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = UIUtils.createLabel(product.getName(),
                new Font("Segoe UI", Font.BOLD, 14), UIUtils.TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel priceInfo = UIUtils.createLabel(
                String.format("Baz: %.2f₺ → Güncel: %.2f₺", product.getPrice(), decoratedPrice),
                UIUtils.BODY_FONT, hasDiscount ? UIUtils.WARNING : UIUtils.TEXT_SECONDARY);
        priceInfo.setAlignmentX(LEFT_ALIGNMENT);

        int stock = stockSubject.getStock(product.getName());
        JLabel stockLabel = UIUtils.createLabel(UIUtils.getStockText(stock),
                UIUtils.SMALL_FONT, UIUtils.getStockColor(stock));
        stockLabel.setAlignmentX(LEFT_ALIGNMENT);

        String desc = decorated != null ? decorated.getDescription() : "";
        if (desc.contains("[")) {
            JLabel decoLabel = UIUtils.createLabel("🏷️ " + desc.substring(desc.indexOf('[')),
                    new Font("Segoe UI", Font.ITALIC, 10), UIUtils.WARNING);
            decoLabel.setAlignmentX(LEFT_ALIGNMENT);
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(priceInfo);
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(decoLabel);
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(stockLabel);
        } else {
            infoPanel.add(nameLabel);
            infoPanel.add(Box.createVerticalStrut(3));
            infoPanel.add(priceInfo);
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(stockLabel);
        }

        // Buttons
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);
        btnPanel.setPreferredSize(new Dimension(180, 100));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row1.setOpaque(false);

        JButton pctBtn = UIUtils.createStyledButton("%15", UIUtils.ACCENT, 55);
        pctBtn.setToolTipText("%15 İndirim");
        pctBtn.addActionListener(e -> applyDecorator(product, "PERCENTAGE"));

        JButton fixBtn = UIUtils.createStyledButton("500₺", UIUtils.ACCENT, 55);
        fixBtn.setToolTipText("500₺ Sabit İndirim");
        fixBtn.addActionListener(e -> applyDecorator(product, "FIXED"));

        JButton flashBtn = UIUtils.createStyledButton("⚡", UIUtils.WARNING, 40);
        flashBtn.setToolTipText("Flash Sale %10");
        flashBtn.addActionListener(e -> applyDecorator(product, "FLASH"));

        JButton shipBtn = UIUtils.createStyledButton("🚚", new Color(56, 189, 248), 40);
        shipBtn.setToolTipText("Ücretsiz Kargo");
        shipBtn.addActionListener(e -> applyDecorator(product, "SHIPPING"));

        row1.add(pctBtn);
        row1.add(fixBtn);
        row1.add(flashBtn);
        row1.add(shipBtn);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        row2.setOpaque(false);

        JButton clearBtn = UIUtils.createStyledButton("🗑️ Temizle", UIUtils.DANGER, 100);
        clearBtn.addActionListener(e -> clearDecorators(product));

        JButton stockBtn = UIUtils.createStyledButton("📦 Stok", UIUtils.SELLER_ACCENT, 80);
        stockBtn.addActionListener(e -> updateStock(product));

        row2.add(clearBtn);
        row2.add(stockBtn);

        btnPanel.add(Box.createVerticalGlue());
        btnPanel.add(row1);
        btnPanel.add(Box.createVerticalStrut(4));
        btnPanel.add(row2);
        btnPanel.add(Box.createVerticalGlue());

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnPanel, BorderLayout.EAST);
        return card;
    }

    private void applyDecorator(Product product, String type) {
        ProductComponent current = ProductDatabase.getDecoratedProduct(product.getId());
        if (current == null) current = new ConcreteProduct(product);

        ProductComponent decorated;
        String description;

        switch (type) {
            case "PERCENTAGE":
                decorated = new PercentageDiscountDecorator(current, 15);
                description = "%15 indirim";
                break;
            case "FIXED":
                decorated = new FixedAmountDiscountDecorator(current, 500);
                description = "500₺ sabit indirim";
                break;
            case "FLASH":
                decorated = new FlashSaleDecorator(current);
                description = "⚡ Flash Sale %10";
                break;
            case "SHIPPING":
                if (current.hasFreeShipping()) {
                    JOptionPane.showMessageDialog(this, "Zaten ücretsiz kargo aktif!",
                            "Bilgi", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                decorated = new FreeShippingDecorator(current);
                description = "Ücretsiz Kargo";
                break;
            default:
                return;
        }

        ProductDatabase.addSellerDecorator(product.getId(), decorated);

        // Notify observers
        double newPrice = decorated.getPrice();
        stockSubject.setPrice(product.getName(), newPrice);
        stockSubject.notifyDiscountAdded(product.getName(), description);

        refresh();

        JOptionPane.showMessageDialog(this,
                product.getName() + " → " + description + " uygulandı!\n" +
                        String.format("Yeni fiyat: %.2f₺", newPrice),
                "İndirim Eklendi", JOptionPane.INFORMATION_MESSAGE);
    }

    private void clearDecorators(Product product) {
        int confirm = JOptionPane.showConfirmDialog(this,
                product.getName() + " üzerindeki tüm indirimleri kaldırmak istiyor musunuz?",
                "İndirimleri Kaldır", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            ProductDatabase.clearSellerDecorators(product.getId());
            stockSubject.setPrice(product.getName(), product.getPrice());
            stockSubject.notifyDiscountRemoved(product.getName());
            refresh();
        }
    }

    private void updateStock(Product product) {
        String input = JOptionPane.showInputDialog(this,
                product.getName() + " için yeni stok miktarı:",
                "Stok Güncelle", JOptionPane.QUESTION_MESSAGE);

        if (input != null) {
            try {
                int newStock = Integer.parseInt(input.trim());
                if (newStock < 0) {
                    JOptionPane.showMessageDialog(this, "Stok negatif olamaz!",
                            "Hata", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                stockSubject.setStock(product.getName(), newStock);
                product.setStock(newStock);
                refresh();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Geçersiz sayı!",
                        "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
