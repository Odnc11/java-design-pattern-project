package main.ui;

import main.models.Product;
import main.patterns.singleton.ShoppingCart;
import main.patterns.decorator.*;
import main.patterns.observer.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Product catalog panel displaying available products.
 * Supports category filtering, search, and adding items to cart.
 * Shows dynamic prices from DemandTracker when available.
 */
public class ProductPanel extends JPanel {
    private final List<Product> allProducts;
    private final StockSubject stockSubject;
    private final NotificationObserver notificationObserver;

    // DemandTracker reference for showing dynamic prices (optional, set via setter)
    private DemandTracker demandTracker;

    private JPanel productGrid;
    private JComboBox<String> categoryFilter;
    private JTextField searchField;
    private JLabel resultCountLabel;

    // Color palette
    private static final Color BG_COLOR = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color CARD_HOVER = new Color(51, 65, 85);
    private static final Color ACCENT = new Color(99, 102, 241);
    private static final Color ACCENT_HOVER = new Color(129, 140, 248);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);
    private static final Color HOT_PINK = new Color(236, 72, 153);

    public ProductPanel(List<Product> products, StockSubject stockSubject,
                        NotificationObserver notificationObserver) {
        this.allProducts = products;
        this.stockSubject = stockSubject;
        this.notificationObserver = notificationObserver;

        setLayout(new BorderLayout(0, 12));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createProductScrollPane(), BorderLayout.CENTER);

        refreshProducts();
    }

    /**
     * Set the DemandTracker to display dynamic prices on product cards.
     */
    public void setDemandTracker(DemandTracker demandTracker) {
        this.demandTracker = demandTracker;
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(BG_COLOR);

        // Title
        JLabel title = new JLabel("🛍️ Ürün Kataloğu");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        // Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setBackground(BG_COLOR);

        // Category filter
        JLabel catLabel = new JLabel("Kategori:");
        catLabel.setForeground(TEXT_SECONDARY);
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        categoryFilter = new JComboBox<>(getCategoryList());
        categoryFilter.setBackground(CARD_BG);
        categoryFilter.setForeground(TEXT_PRIMARY);
        categoryFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        categoryFilter.setPreferredSize(new Dimension(140, 30));
        categoryFilter.addActionListener(e -> refreshProducts());

        // Search field
        JLabel searchLabel = new JLabel("Ara:");
        searchLabel.setForeground(TEXT_SECONDARY);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        searchField = new JTextField(14);
        searchField.setBackground(CARD_BG);
        searchField.setForeground(TEXT_PRIMARY);
        searchField.setCaretColor(TEXT_PRIMARY);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(160, 30));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        searchField.addActionListener(e -> refreshProducts());

        // Search button
        JButton searchBtn = createStyledButton("🔍 Ara", ACCENT, 80);
        searchBtn.addActionListener(e -> refreshProducts());

        resultCountLabel = new JLabel();
        resultCountLabel.setForeground(TEXT_SECONDARY);
        resultCountLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        filterRow.add(catLabel);
        filterRow.add(categoryFilter);
        filterRow.add(Box.createHorizontalStrut(12));
        filterRow.add(searchLabel);
        filterRow.add(searchField);
        filterRow.add(searchBtn);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(resultCountLabel);

        header.add(title, BorderLayout.NORTH);
        header.add(filterRow, BorderLayout.CENTER);

        return header;
    }

    private JScrollPane createProductScrollPane() {
        productGrid = new JPanel();
        productGrid.setLayout(new BoxLayout(productGrid, BoxLayout.Y_AXIS));
        productGrid.setBackground(BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(productGrid);
        scrollPane.setBackground(BG_COLOR);
        scrollPane.getViewport().setBackground(BG_COLOR);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    /**
     * Refresh the product display based on current filters.
     */
    public void refreshProducts() {
        productGrid.removeAll();
        List<Product> filtered = getFilteredProducts();
        resultCountLabel.setText(filtered.size() + " ürün bulundu");

        for (Product product : filtered) {
            productGrid.add(createProductCard(product));
            productGrid.add(Box.createVerticalStrut(8));
        }

        productGrid.revalidate();
        productGrid.repaint();
    }

    private JPanel createProductCard(Product product) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Check if this product has an active auction
        boolean hasAuction = demandTracker != null && demandTracker.isAuctionActive(product.getName());
        if (hasAuction) {
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 3, 1, 1, HOT_PINK),
                    BorderFactory.createEmptyBorder(12, 14, 12, 16)
            ));
        }

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBackground(CARD_BG);
            }
        });

        // Left: Product info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Name row with auction badge if active
        JPanel nameRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        nameRow.setOpaque(false);
        nameRow.setAlignmentX(LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(product.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameRow.add(nameLabel);

        if (hasAuction) {
            JLabel auctionBadge = new JLabel(" 🔥 MÜZ. ");
            auctionBadge.setFont(new Font("Segoe UI", Font.BOLD, 9));
            auctionBadge.setForeground(Color.WHITE);
            auctionBadge.setOpaque(true);
            auctionBadge.setBackground(HOT_PINK);
            auctionBadge.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
            nameRow.add(auctionBadge);
        }

        JLabel descLabel = new JLabel(product.getDescription());
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(TEXT_SECONDARY);
        descLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel catLabel = new JLabel("📁 " + product.getCategory());
        catLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        catLabel.setForeground(new Color(99, 102, 241));
        catLabel.setAlignmentX(LEFT_ALIGNMENT);

        int currentStock = stockSubject.getStock(product.getName());
        JLabel stockLabel = new JLabel(getStockText(currentStock));
        stockLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        stockLabel.setForeground(getStockColor(currentStock));
        stockLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Demand info if tracker is available
        JLabel demandLabel = null;
        if (demandTracker != null) {
            int demand = demandTracker.getDemandCount(product.getName());
            if (demand > 0) {
                demandLabel = new JLabel("📊 " + demandTracker.getDemandLevel(product.getName()) 
                        + " (" + demand + " tıklama)");
                demandLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                demandLabel.setForeground(WARNING);
                demandLabel.setAlignmentX(LEFT_ALIGNMENT);
            }
        }

        infoPanel.add(nameRow);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(catLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(stockLabel);
        if (demandLabel != null) {
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(demandLabel);
        }

        // Right: Price + Add button
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(150, 110));

        // Dynamic price display
        double displayPrice = product.getPrice();
        boolean priceChanged = false;
        if (demandTracker != null) {
            double dynamicPrice = demandTracker.getCurrentPrice(product.getName());
            if (Math.abs(dynamicPrice - product.getPrice()) > 0.01) {
                // Show original price strikethrough
                JLabel origPriceLabel = new JLabel(
                        "<html><s>" + String.format("%.2f ₺", product.getPrice()) + "</s></html>");
                origPriceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                origPriceLabel.setForeground(TEXT_SECONDARY);
                origPriceLabel.setAlignmentX(CENTER_ALIGNMENT);
                actionPanel.add(origPriceLabel);
                displayPrice = dynamicPrice;
                priceChanged = true;
            }
        }

        JLabel priceLabel = new JLabel(String.format("%.2f ₺", displayPrice));
        priceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        priceLabel.setForeground(priceChanged ? DANGER : SUCCESS);
        priceLabel.setAlignmentX(CENTER_ALIGNMENT);

        if (priceChanged) {
            double increasePercent = demandTracker.getPriceIncreasePercent(product.getName());
            JLabel changeLabel = new JLabel(String.format("▲ +%.1f%%", increasePercent));
            changeLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            changeLabel.setForeground(DANGER);
            changeLabel.setAlignmentX(CENTER_ALIGNMENT);
            actionPanel.add(priceLabel);
            actionPanel.add(changeLabel);
        } else {
            actionPanel.add(priceLabel);
        }

        actionPanel.add(Box.createVerticalStrut(6));

        JButton addBtn = createStyledButton("🛒 Sepete Ekle", ACCENT, 130);
        addBtn.setAlignmentX(CENTER_ALIGNMENT);
        if (currentStock <= 0) {
            addBtn.setEnabled(false);
            addBtn.setText("Stokta Yok");
        }
        addBtn.addActionListener(e -> addToCart(product));

        actionPanel.add(Box.createVerticalGlue());
        actionPanel.add(addBtn);
        actionPanel.add(Box.createVerticalGlue());

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(actionPanel, BorderLayout.EAST);

        return card;
    }

    private void addToCart(Product product) {
        int currentStock = stockSubject.getStock(product.getName());
        if (currentStock <= 0) {
            JOptionPane.showMessageDialog(this,
                    product.getName() + " stokta kalmadı!",
                    "Stok Yetersiz", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use dynamic price from DemandTracker if available
        ProductComponent decorated;
        if (demandTracker != null) {
            double dynamicPrice = demandTracker.getCurrentPrice(product.getName());
            // Create a price-adjusted product for the cart
            Product adjustedProduct = new Product(product.getId(), product.getName(),
                    product.getDescription(), dynamicPrice, product.getStock(), product.getCategory());
            decorated = new ConcreteProduct(adjustedProduct);
        } else {
            decorated = new ConcreteProduct(product);
        }

        ShoppingCart cart = ShoppingCart.getInstance();
        cart.addItem(decorated, product);

        // Decrease stock via observer subject
        stockSubject.decreaseStock(product.getName(), 1);
        product.setStock(stockSubject.getStock(product.getName()));

        // Recalculate price after stock change (lower stock = higher price)
        if (demandTracker != null) {
            demandTracker.recalculatePrice(product.getName());
        }

        refreshProducts();

        // Subtle feedback
        JOptionPane.showMessageDialog(this,
                product.getName() + " sepete eklendi!",
                "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    private String getStockText(int stock) {
        if (stock == 0) return "❌ Stokta Yok";
        if (stock <= 5) return "⚡ Son " + stock + " adet!";
        return "✅ Stok: " + stock + " adet";
    }

    private Color getStockColor(int stock) {
        if (stock == 0) return DANGER;
        if (stock <= 5) return WARNING;
        return SUCCESS;
    }

    private String[] getCategoryList() {
        List<String> categories = new ArrayList<>();
        categories.add("Tümü");
        for (Product p : allProducts) {
            if (!categories.contains(p.getCategory())) {
                categories.add(p.getCategory());
            }
        }
        return categories.toArray(new String[0]);
    }

    private List<Product> getFilteredProducts() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String searchText = searchField.getText().trim().toLowerCase();

        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            boolean matchesCategory = "Tümü".equals(selectedCategory)
                    || p.getCategory().equals(selectedCategory);
            boolean matchesSearch = searchText.isEmpty()
                    || p.getName().toLowerCase().contains(searchText)
                    || p.getDescription().toLowerCase().contains(searchText);

            if (matchesCategory && matchesSearch) {
                filtered.add(p);
            }
        }
        return filtered;
    }

    private JButton createStyledButton(String text, Color bgColor, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 32));

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
