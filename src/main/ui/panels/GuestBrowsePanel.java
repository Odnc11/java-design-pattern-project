package main.ui.panels;

import main.database.ProductDatabase;
import main.database.UserDatabase;
import main.models.Product;
import main.models.Seller;
import main.patterns.decorator.ProductComponent;
import main.patterns.observer.StockSubject;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Guest browse panel - read-only product catalog for non-logged-in users.
 * Shows products and prices but no "add to cart" functionality.
 */
public class GuestBrowsePanel extends JPanel {
    private final Runnable onBackToLogin;
    private final StockSubject stockSubject;

    private JPanel productGrid;
    private JComboBox<String> categoryFilter;
    private JTextField searchField;
    private JLabel resultCountLabel;

    public GuestBrowsePanel(Runnable onBackToLogin, StockSubject stockSubject) {
        this.onBackToLogin = onBackToLogin;
        this.stockSubject = stockSubject;

        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(createHeader(), BorderLayout.NORTH);
        add(createProductScrollPane(), BorderLayout.CENTER);
        refreshProducts();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(UIUtils.BG_COLOR);

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel title = UIUtils.createLabel("👀 Ürün Kataloğu (Misafir)", UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);

        JButton loginBtn = UIUtils.createStyledButton("🔑 Giriş Yap", UIUtils.ACCENT, 120);
        loginBtn.addActionListener(e -> onBackToLogin.run());

        titleRow.add(title, BorderLayout.WEST);
        titleRow.add(loginBtn, BorderLayout.EAST);

        // Filter row
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setBackground(UIUtils.BG_COLOR);

        JLabel catLabel = UIUtils.createLabel("Kategori:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        categoryFilter = new JComboBox<>(getCategoryList());
        categoryFilter.setBackground(UIUtils.CARD_BG);
        categoryFilter.setForeground(UIUtils.TEXT_PRIMARY);
        categoryFilter.setFont(UIUtils.BODY_FONT);
        categoryFilter.setPreferredSize(new Dimension(140, 30));
        categoryFilter.addActionListener(e -> refreshProducts());

        JLabel searchLabel = UIUtils.createLabel("Ara:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        searchField = UIUtils.createStyledTextField(14);
        searchField.setPreferredSize(new Dimension(160, 30));
        searchField.addActionListener(e -> refreshProducts());

        JButton searchBtn = UIUtils.createStyledButton("🔍 Ara", UIUtils.ACCENT, 80);
        searchBtn.addActionListener(e -> refreshProducts());

        resultCountLabel = UIUtils.createLabel("", UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);

        filterRow.add(catLabel);
        filterRow.add(categoryFilter);
        filterRow.add(Box.createHorizontalStrut(12));
        filterRow.add(searchLabel);
        filterRow.add(searchField);
        filterRow.add(searchBtn);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(resultCountLabel);

        header.add(titleRow, BorderLayout.NORTH);
        header.add(filterRow, BorderLayout.CENTER);

        return header;
    }

    private JScrollPane createProductScrollPane() {
        productGrid = new JPanel();
        productGrid.setLayout(new BoxLayout(productGrid, BoxLayout.Y_AXIS));
        productGrid.setBackground(UIUtils.BG_COLOR);

        JScrollPane scrollPane = new JScrollPane(productGrid);
        scrollPane.setBackground(UIUtils.BG_COLOR);
        scrollPane.getViewport().setBackground(UIUtils.BG_COLOR);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

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
        card.setBackground(UIUtils.CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Product info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = UIUtils.createLabel(product.getName(), new Font("Segoe UI", Font.BOLD, 15), UIUtils.TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLabel = UIUtils.createLabel(product.getDescription(), UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);
        descLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel metaLabel = UIUtils.createLabel("📁 " + product.getCategory(), 
                new Font("Segoe UI", Font.PLAIN, 10), UIUtils.TEXT_SECONDARY);
        metaLabel.setAlignmentX(LEFT_ALIGNMENT);

        int stock = stockSubject.getStock(product.getName());
        JLabel stockLabel = UIUtils.createLabel(UIUtils.getStockText(stock),
                new Font("Segoe UI", Font.BOLD, 11), UIUtils.getStockColor(stock));
        stockLabel.setAlignmentX(LEFT_ALIGNMENT);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(metaLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(stockLabel);

        // Price
        JPanel pricePanel = new JPanel();
        pricePanel.setLayout(new BoxLayout(pricePanel, BoxLayout.Y_AXIS));
        pricePanel.setOpaque(false);
        pricePanel.setPreferredSize(new Dimension(130, 80));

        ProductComponent decorated = ProductDatabase.getDecoratedProduct(product.getId());
        double displayPrice = decorated != null ? decorated.getPrice() : product.getPrice();
        boolean hasDiscount = Math.abs(displayPrice - product.getPrice()) > 0.01;

        if (hasDiscount) {
            JLabel origPrice = new JLabel("<html><s>" + String.format("%.2f ₺", product.getPrice()) + "</s></html>");
            origPrice.setFont(UIUtils.SMALL_FONT);
            origPrice.setForeground(UIUtils.TEXT_SECONDARY);
            origPrice.setAlignmentX(CENTER_ALIGNMENT);
            pricePanel.add(origPrice);
        }

        JLabel priceLabel = UIUtils.createLabel(String.format("%.2f ₺", displayPrice),
                new Font("Segoe UI", Font.BOLD, 18), hasDiscount ? UIUtils.DANGER : UIUtils.SUCCESS);
        priceLabel.setAlignmentX(CENTER_ALIGNMENT);
        pricePanel.add(priceLabel);

        JLabel loginMsg = UIUtils.createLabel("Giriş yapın", UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);
        loginMsg.setAlignmentX(CENTER_ALIGNMENT);
        pricePanel.add(Box.createVerticalStrut(8));
        pricePanel.add(loginMsg);

        card.add(infoPanel, BorderLayout.CENTER);
        card.add(pricePanel, BorderLayout.EAST);

        return card;
    }

    private String[] getCategoryList() {
        List<String> categories = new ArrayList<>();
        categories.add("Tümü");
        categories.addAll(ProductDatabase.getAllCategories());
        return categories.toArray(new String[0]);
    }

    private List<Product> getFilteredProducts() {
        String selectedCategory = (String) categoryFilter.getSelectedItem();
        String searchText = searchField.getText().trim().toLowerCase();

        List<Product> filtered = new ArrayList<>();
        for (Product p : ProductDatabase.getAllProducts()) {
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
}
