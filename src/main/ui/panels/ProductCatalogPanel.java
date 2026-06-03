package main.ui.panels;

import main.database.ProductDatabase;
import main.database.UserDatabase;
import main.models.Product;
import main.models.Seller;
import main.patterns.decorator.ConcreteProduct;
import main.patterns.decorator.ProductComponent;
import main.patterns.observer.StockSubject;
import main.patterns.singleton.ShoppingCart;
import main.utils.SessionManager;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Product catalog panel for buyers.
 * Shows all products with seller info, decorator prices, and "Add to Cart" buttons.
 */
public class ProductCatalogPanel extends JPanel {
    private final StockSubject stockSubject;
    private JPanel productGrid;
    private JComboBox<String> categoryFilter;
    private JTextField searchField;
    private JLabel resultCountLabel;

    public ProductCatalogPanel(StockSubject stockSubject) {
        this.stockSubject = stockSubject;

        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 8));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createProductScrollPane(), BorderLayout.CENTER);
        refreshProducts();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setBackground(UIUtils.BG_COLOR);

        JLabel title = UIUtils.createLabel("🛍️ Ürün Kataloğu", UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);

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

        header.add(title, BorderLayout.NORTH);
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
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // Hover
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { card.setBackground(UIUtils.CARD_HOVER); }
            public void mouseExited(java.awt.event.MouseEvent e) { card.setBackground(UIUtils.CARD_BG); }
        });

        // Left: Product info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = UIUtils.createLabel(product.getName(), new Font("Segoe UI", Font.BOLD, 15), UIUtils.TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel descLabel = UIUtils.createLabel(product.getDescription(), UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);
        descLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Seller info
        JLabel metaLabel = UIUtils.createLabel("📁 " + product.getCategory(),
                new Font("Segoe UI", Font.PLAIN, 10), UIUtils.TEXT_SECONDARY);
        metaLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Stock
        int currentStock = stockSubject.getStock(product.getName());
        JLabel stockLabel = UIUtils.createLabel(UIUtils.getStockText(currentStock),
                new Font("Segoe UI", Font.BOLD, 11), UIUtils.getStockColor(currentStock));
        stockLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Show decorator chain description
        ProductComponent decorated = ProductDatabase.getDecoratedProduct(product.getId());
        String decoDesc = decorated != null ? decorated.getDescription() : "";
        JLabel decoLabel = null;
        if (decoDesc.contains("[")) {
            String badges = decoDesc.substring(decoDesc.indexOf('['));
            decoLabel = UIUtils.createLabel("🏷️ " + badges, new Font("Segoe UI", Font.ITALIC, 10), UIUtils.WARNING);
            decoLabel.setAlignmentX(LEFT_ALIGNMENT);
        }

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(descLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(metaLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(stockLabel);
        if (decoLabel != null) {
            infoPanel.add(Box.createVerticalStrut(2));
            infoPanel.add(decoLabel);
        }

        // Right: Price + Add button
        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setOpaque(false);
        actionPanel.setPreferredSize(new Dimension(150, 100));

        double displayPrice = decorated != null ? decorated.getPrice() : product.getPrice();
        boolean hasDiscount = Math.abs(displayPrice - product.getPrice()) > 0.01;

        if (hasDiscount) {
            JLabel origPrice = new JLabel("<html><s>" + String.format("%.2f ₺", product.getPrice()) + "</s></html>");
            origPrice.setFont(UIUtils.SMALL_FONT);
            origPrice.setForeground(UIUtils.TEXT_SECONDARY);
            origPrice.setAlignmentX(CENTER_ALIGNMENT);
            actionPanel.add(origPrice);
        }

        JLabel priceLabel = UIUtils.createLabel(String.format("%.2f ₺", displayPrice),
                new Font("Segoe UI", Font.BOLD, 18), hasDiscount ? UIUtils.DANGER : UIUtils.SUCCESS);
        priceLabel.setAlignmentX(CENTER_ALIGNMENT);
        actionPanel.add(priceLabel);

        if (decorated != null && decorated.hasFreeShipping()) {
            JLabel shipLabel = UIUtils.createLabel("🚚 Ücretsiz Kargo", new Font("Segoe UI", Font.BOLD, 9), new Color(56, 189, 248));
            shipLabel.setAlignmentX(CENTER_ALIGNMENT);
            actionPanel.add(shipLabel);
        }

        actionPanel.add(Box.createVerticalStrut(6));

        JButton addBtn = UIUtils.createStyledButton("🛒 Sepete Ekle", UIUtils.ACCENT, 130);
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
        if (!SessionManager.isBuyer()) return;

        int currentStock = stockSubject.getStock(product.getName());
        if (currentStock <= 0) {
            JOptionPane.showMessageDialog(this, product.getName() + " stokta kalmadı!",
                    "Stok Yetersiz", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Use the seller-decorated product as the base for cart
        ProductComponent decorated = ProductDatabase.getDecoratedProduct(product.getId());
        if (decorated == null) {
            decorated = new ConcreteProduct(product);
        }

        ShoppingCart cart = ShoppingCart.getInstance(SessionManager.getCurrentUser().getId());
        
        // Validate if adding 1 more exceeds current available stock
        if (cart.getQuantity(product) + 1 > currentStock) {
            JOptionPane.showMessageDialog(this, product.getName() + " için yeterli stok bulunmuyor (Sepetinizdeki miktar dahil)!",
                    "Stok Yetersiz", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        cart.addItem(decorated, product);

        // DO NOT decrease global stock here. Global stock decreases on checkout!
        // We only notify the UI to refresh.
        refreshProducts();

        JOptionPane.showMessageDialog(this,
                product.getName() + " sepete eklendi!",
                "Başarılı", JOptionPane.INFORMATION_MESSAGE);
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
