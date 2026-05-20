package main.ui;

import main.models.Bid;
import main.models.Product;
import main.models.User;
import main.patterns.observer.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Auction Panel - Demand-driven auction interface.
 * 
 * Displays products with their demand levels, dynamic prices,
 * and allows users to place bids. Shows real-time price changes
 * driven by the Observer pattern.
 * 
 * Key Features:
 * - Demand heat indicators (color-coded by click count)
 * - Dynamic price display (base price vs current price)
 * - Bid placement with minimum bid validation
 * - Live auction event log
 * - Auto-activating auctions when demand threshold is met
 */
public class AuctionPanel extends JPanel {
    private final List<Product> products;
    private final DemandTracker demandTracker;
    private final StockSubject stockSubject;
    private final AuctionEventObserver auctionEventObserver;
    private final ProductPanel productPanel;

    private JPanel auctionGrid;
    private JTextArea auctionLogArea;
    private JLabel activeAuctionCount;

    // Simulated users for bidding demo
    private final User[] demoUsers;
    private int currentUserIndex = 0;

    // Colors
    private static final Color BG_COLOR = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_HOVER = new Color(51, 65, 85);
    private static final Color ACCENT = new Color(99, 102, 241);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color HOT_PINK = new Color(236, 72, 153);
    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);
    private static final Color AUCTION_ACTIVE_BG = new Color(30, 27, 45);
    private static final Color BID_GOLD = new Color(234, 179, 8);

    // Demand heat colors (green → yellow → orange → red → magenta)
    private static final Color[] HEAT_COLORS = {
        new Color(100, 116, 139),  // 0: No demand (gray)
        new Color(34, 197, 94),    // 1: Low (green)
        new Color(245, 158, 11),   // 2: Medium (yellow/orange)
        new Color(239, 68, 68),    // 3: High (red)
        new Color(236, 72, 153)    // 4: Very high (pink/hot)
    };

    public AuctionPanel(List<Product> products, DemandTracker demandTracker,
                        StockSubject stockSubject, AuctionEventObserver auctionEventObserver,
                        ProductPanel productPanel) {
        this.products = products;
        this.demandTracker = demandTracker;
        this.stockSubject = stockSubject;
        this.auctionEventObserver = auctionEventObserver;
        this.productPanel = productPanel;

        // Demo users for simulating multi-user bidding
        this.demoUsers = new User[] {
            new User(1, "Ahmet Y.", "ahmet@example.com"),
            new User(2, "Elif K.", "elif@example.com"),
            new User(3, "Mehmet S.", "mehmet@example.com"),
            new User(4, "Zeynep A.", "zeynep@example.com"),
            new User(5, "Can D.", "can@example.com")
        };

        setLayout(new BorderLayout(0, 12));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);
        add(createAuctionLogPanel(), BorderLayout.SOUTH);

        setupAuctionEventListener();
        refreshAuctions();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titleRow.setOpaque(false);

        JLabel icon = new JLabel("🔥");
        icon.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        JLabel title = new JLabel("Canlı Müzayede");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("  — Talep & Stok Bazlı Dinamik Fiyatlama");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        subtitle.setForeground(TEXT_SECONDARY);

        titleRow.add(icon);
        titleRow.add(title);
        titleRow.add(subtitle);

        activeAuctionCount = new JLabel("0 aktif müzayede");
        activeAuctionCount.setFont(new Font("Segoe UI", Font.BOLD, 12));
        activeAuctionCount.setForeground(HOT_PINK);

        // Info bar about the algorithm
        JPanel infoBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        infoBar.setOpaque(false);

        infoBar.add(createHeatBadge("Düşük", HEAT_COLORS[1]));
        infoBar.add(createHeatBadge("Orta", HEAT_COLORS[2]));
        infoBar.add(createHeatBadge("Yüksek", HEAT_COLORS[3]));
        infoBar.add(createHeatBadge("Çok Yüksek", HEAT_COLORS[4]));

        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.add(titleRow, BorderLayout.WEST);
        topRow.add(activeAuctionCount, BorderLayout.EAST);

        header.add(topRow, BorderLayout.NORTH);
        header.add(infoBar, BorderLayout.CENTER);

        return header;
    }

    private JLabel createHeatBadge(String text, Color color) {
        JLabel badge = new JLabel(" ● " + text + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        badge.setForeground(color);
        return badge;
    }

    private JSplitPane createMainContent() {
        // Left: Auction product grid
        auctionGrid = new JPanel();
        auctionGrid.setLayout(new BoxLayout(auctionGrid, BoxLayout.Y_AXIS));
        auctionGrid.setBackground(BG_COLOR);

        JScrollPane gridScroll = new JScrollPane(auctionGrid);
        gridScroll.setBackground(BG_COLOR);
        gridScroll.getViewport().setBackground(BG_COLOR);
        gridScroll.setBorder(BorderFactory.createEmptyBorder());
        gridScroll.getVerticalScrollBar().setUnitIncrement(16);

        // Right: Bid details for selected product
        JPanel bidPanel = createBidInfoPanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, gridScroll, bidPanel);
        split.setDividerLocation(480);
        split.setResizeWeight(0.6);
        split.setBackground(BG_COLOR);
        split.setBorder(null);
        split.setDividerSize(3);

        return split;
    }

    private JPanel createBidInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));

        JLabel infoTitle = new JLabel("📊 Fiyat Algoritması");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        infoTitle.setForeground(TEXT_PRIMARY);

        JTextArea infoArea = new JTextArea();
        infoArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        infoArea.setBackground(CARD_BG);
        infoArea.setForeground(TEXT_SECONDARY);
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        infoArea.setText(
            "╔═══════════════════════════════╗\n" +
            "║  DİNAMİK FİYATLAMA FORMÜLÜ   ║\n" +
            "╚═══════════════════════════════╝\n\n" +
            "talepOranı = tıklama / eşik(5)\n" +
            "kıtlıkOranı = 1 / (stok + 1)\n" +
            "çarpan = 1 + (talep × kıtlık × 0.5)\n" +
            "yeniFiyat = tabFiyat × çarpan\n\n" +
            "─────────────────────────────────\n" +
            "ÖRNEK SENARYO:\n" +
            "─────────────────────────────────\n" +
            "Ürün: Laptop (15,999.99₺)\n" +
            "Tıklama: 20, Stok: 2\n\n" +
            "talepOranı = 20/5 = 4.0\n" +
            "kıtlıkOranı = 1/3 = 0.33\n" +
            "çarpan = 1 + (4.0 × 0.33 × 0.5)\n" +
            "çarpan = 1.67\n\n" +
            "yeniFiyat = 15,999.99 × 1.67\n" +
            "         = 26,719.98₺ (+67%)\n\n" +
            "─────────────────────────────────\n" +
            "📌 NOT: Max çarpan = 3.0x\n" +
            "📌 Müzayede 5+ tıklamada başlar\n" +
            "📌 Teklifler mevcut fiyatın\n" +
            "   üstünde olmalıdır"
        );

        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        panel.add(infoTitle, BorderLayout.NORTH);
        panel.add(infoScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAuctionLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(AUCTION_ACTIVE_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        panel.setPreferredSize(new Dimension(0, 120));

        JLabel logTitle = new JLabel("📜 Müzayede Olayları (Observer Pattern → AuctionEventObserver)");
        logTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logTitle.setForeground(BID_GOLD);

        auctionLogArea = new JTextArea(4, 80);
        auctionLogArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        auctionLogArea.setBackground(new Color(15, 15, 30));
        auctionLogArea.setForeground(TEXT_SECONDARY);
        auctionLogArea.setEditable(false);
        auctionLogArea.setLineWrap(true);
        auctionLogArea.setWrapStyleWord(true);
        auctionLogArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scroll = new JScrollPane(auctionLogArea);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));

        JButton clearBtn = new JButton("Temizle");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearBtn.setForeground(TEXT_SECONDARY);
        clearBtn.setBackground(BORDER_COLOR);
        clearBtn.setFocusPainted(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> auctionLogArea.setText(""));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(logTitle, BorderLayout.WEST);
        headerRow.add(clearBtn, BorderLayout.EAST);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Connect AuctionEventObserver to the UI log area.
     */
    private void setupAuctionEventListener() {
        auctionEventObserver.addListener((message, eventType) -> {
            SwingUtilities.invokeLater(() -> {
                auctionLogArea.append(message + "\n");
                auctionLogArea.setCaretPosition(auctionLogArea.getDocument().getLength());
            });
        });
    }

    /**
     * Refresh all auction product cards.
     */
    public void refreshAuctions() {
        auctionGrid.removeAll();
        int activeCount = 0;

        for (Product product : products) {
            auctionGrid.add(createAuctionCard(product));
            auctionGrid.add(Box.createVerticalStrut(8));
            if (demandTracker.isAuctionActive(product.getName())) {
                activeCount++;
            }
        }

        activeAuctionCount.setText(activeCount + " aktif müzayede");
        auctionGrid.revalidate();
        auctionGrid.repaint();
    }

    private JPanel createAuctionCard(Product product) {
        String name = product.getName();
        int demand = demandTracker.getDemandCount(name);
        int heatLevel = demandTracker.getDemandHeatLevel(name);
        double basePrice = demandTracker.getBasePrice(name);
        double currentPrice = demandTracker.getCurrentPrice(name);
        double increasePercent = demandTracker.getPriceIncreasePercent(name);
        boolean isActive = demandTracker.isAuctionActive(name);
        int stock = stockSubject.getStock(name);

        Color heatColor = HEAT_COLORS[Math.min(heatLevel, HEAT_COLORS.length - 1)];

        // Card with heat-colored left border
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(isActive ? AUCTION_ACTIVE_BG : CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 4, 1, 1, heatColor),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(isActive ? AUCTION_ACTIVE_BG : CARD_BG);
            }
        });

        // Left: Product info + demand bar
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Name + auction status
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        nameLabel.setForeground(TEXT_PRIMARY);

        if (isActive) {
            JLabel activeBadge = new JLabel(" 🔥 MÜZ. AKTİF ");
            activeBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
            activeBadge.setForeground(Color.WHITE);
            activeBadge.setOpaque(true);
            activeBadge.setBackground(HOT_PINK);
            activeBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            nameRow.add(nameLabel);
            nameRow.add(activeBadge);
        } else {
            nameRow.add(nameLabel);
        }

        // Demand info
        JLabel demandLabel = new JLabel("📊 " + demandTracker.getDemandLevel(name) 
                + " (" + demand + " tıklama)");
        demandLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        demandLabel.setForeground(heatColor);
        demandLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Stock info
        JLabel stockLabel = new JLabel("📦 Stok: " + stock + " adet");
        stockLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        stockLabel.setForeground(stock <= 5 ? WARNING : TEXT_SECONDARY);
        stockLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Demand progress bar
        JPanel demandBar = createDemandBar(demand, heatColor);
        demandBar.setAlignmentX(LEFT_ALIGNMENT);

        // Bid count
        int bidCount = demandTracker.getTotalBidCount(name);
        JLabel bidLabel = new JLabel("🏷️ " + bidCount + " teklif");
        bidLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        bidLabel.setForeground(bidCount > 0 ? BID_GOLD : TEXT_SECONDARY);
        bidLabel.setAlignmentX(LEFT_ALIGNMENT);

        infoPanel.add(nameRow);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(demandLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(stockLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(demandBar);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(bidLabel);

        // Right: Prices + Action buttons
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(160, 120));

        // Base price (strikethrough if changed)
        if (increasePercent > 0.5) {
            JLabel basePriceLabel = new JLabel(String.format("%.2f ₺", basePrice));
            basePriceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            basePriceLabel.setForeground(TEXT_SECONDARY);
            // Simulate strikethrough
            basePriceLabel.setText("<html><s>" + String.format("%.2f ₺", basePrice) + "</s></html>");
            basePriceLabel.setAlignmentX(CENTER_ALIGNMENT);
            actionPanel.add(basePriceLabel);
        }

        // Current dynamic price
        JLabel currentPriceLabel = new JLabel(String.format("%.2f ₺", currentPrice));
        currentPriceLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        currentPriceLabel.setForeground(increasePercent > 0.5 ? DANGER : SUCCESS);
        currentPriceLabel.setAlignmentX(CENTER_ALIGNMENT);
        actionPanel.add(currentPriceLabel);

        // Price change indicator
        if (increasePercent > 0.5) {
            JLabel changeLabel = new JLabel(String.format("▲ +%.1f%%", increasePercent));
            changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            changeLabel.setForeground(DANGER);
            changeLabel.setAlignmentX(CENTER_ALIGNMENT);
            actionPanel.add(changeLabel);
        }

        actionPanel.add(Box.createVerticalStrut(6));

        // Click (demand) button
        JButton clickBtn = createStyledButton("👆 İlgi Göster", ACCENT, 140);
        clickBtn.setAlignmentX(CENTER_ALIGNMENT);
        clickBtn.addActionListener(e -> {
            demandTracker.recordDemand(name);
            refreshAuctions();
            productPanel.refreshProducts();
        });
        actionPanel.add(clickBtn);

        actionPanel.add(Box.createVerticalStrut(4));

        // Bid button (only if auction is active)
        if (isActive && stock > 0) {
            JButton bidBtn = createStyledButton("🏷️ Teklif Ver", BID_GOLD, 140);
            bidBtn.setAlignmentX(CENTER_ALIGNMENT);
            bidBtn.addActionListener(e -> showBidDialog(product));
            actionPanel.add(bidBtn);
        }

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    /**
     * Create a visual demand bar showing click intensity.
     */
    private JPanel createDemandBar(int demand, Color heatColor) {
        JPanel barContainer = new JPanel(new BorderLayout());
        barContainer.setOpaque(false);
        barContainer.setMaximumSize(new Dimension(250, 10));
        barContainer.setPreferredSize(new Dimension(250, 10));

        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int width = getWidth();
                int height = getHeight();

                // Background
                g2.setColor(BORDER_COLOR);
                g2.fillRoundRect(0, 0, width, height, 6, 6);

                // Fill based on demand (max display at 20 clicks)
                int fillWidth = Math.min(width, (int)(width * (demand / 20.0)));
                if (fillWidth > 0) {
                    g2.setColor(heatColor);
                    g2.fillRoundRect(0, 0, fillWidth, height, 6, 6);
                }
            }
        };
        bar.setPreferredSize(new Dimension(250, 8));
        bar.setOpaque(false);

        barContainer.add(bar, BorderLayout.CENTER);
        return barContainer;
    }

    /**
     * Show bid dialog for a product.
     */
    private void showBidDialog(Product product) {
        String name = product.getName();
        double currentPrice = demandTracker.getCurrentPrice(name);
        double highestBid = demandTracker.getHighestBidAmount(name);
        double minimumBid = Math.max(currentPrice, highestBid) + 1;

        // Cycle through demo users
        User currentUser = demoUsers[currentUserIndex % demoUsers.length];
        currentUserIndex++;

        // Build bid history display
        List<Bid> bids = demandTracker.getBidsForProduct(name);
        StringBuilder historyText = new StringBuilder();
        historyText.append("Güncel Fiyat: ").append(String.format("%.2f₺", currentPrice)).append("\n");
        if (highestBid > 0) {
            historyText.append("En Yüksek Teklif: ").append(String.format("%.2f₺", highestBid)).append("\n");
        }
        historyText.append("Minimum Teklif: ").append(String.format("%.2f₺", minimumBid)).append("\n\n");

        if (!bids.isEmpty()) {
            historyText.append("─── Son Teklifler ───\n");
            int start = Math.max(0, bids.size() - 5);
            for (int i = start; i < bids.size(); i++) {
                historyText.append(bids.get(i).toString()).append("\n");
            }
        }

        JTextArea historyArea = new JTextArea(historyText.toString());
        historyArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        historyArea.setEditable(false);
        historyArea.setBackground(new Color(30, 41, 59));
        historyArea.setForeground(new Color(241, 245, 249));

        JPanel dialogPanel = new JPanel(new BorderLayout(0, 8));
        dialogPanel.add(new JScrollPane(historyArea), BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.add(new JLabel("Teklif (₺): "), BorderLayout.WEST);
        JTextField bidField = new JTextField(String.format("%.2f", minimumBid));
        inputRow.add(bidField, BorderLayout.CENTER);
        JLabel userLabel = new JLabel("Teklif veren: " + currentUser.getName());
        userLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        inputRow.add(userLabel, BorderLayout.SOUTH);

        dialogPanel.add(inputRow, BorderLayout.SOUTH);
        dialogPanel.setPreferredSize(new Dimension(350, 250));

        int result = JOptionPane.showConfirmDialog(this, dialogPanel,
                "🏷️ " + name + " - Teklif Ver",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                double bidAmount = Double.parseDouble(bidField.getText().replace(",", "."));
                Bid bid = demandTracker.placeBid(name, currentUser, bidAmount);
                if (bid != null) {
                    JOptionPane.showMessageDialog(this,
                            "✅ Teklif kabul edildi!\n" + bid.toString(),
                            "Teklif Başarılı", JOptionPane.INFORMATION_MESSAGE);
                    refreshAuctions();
                    productPanel.refreshProducts();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "❌ Teklif çok düşük! Minimum: " + String.format("%.2f₺", minimumBid),
                            "Teklif Reddedildi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Geçersiz tutar girdiniz!",
                        "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createStyledButton(String text, Color bgColor, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(bgColor.equals(BID_GOLD) ? Color.BLACK : Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 28));
        btn.setMaximumSize(new Dimension(width, 28));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(bgColor);
            }
        });

        return btn;
    }
}
