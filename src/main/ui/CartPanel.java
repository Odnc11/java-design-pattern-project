package main.ui;

import main.patterns.singleton.ShoppingCart;
import main.patterns.singleton.ShoppingCart.CartItem;
import main.patterns.decorator.*;
import main.patterns.observer.StockSubject;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Shopping cart panel displaying cart items with discount controls.
 * Demonstrates Decorator pattern through coupon/discount/shipping application.
 */
public class CartPanel extends JPanel implements ShoppingCart.CartChangeListener {
    private final ShoppingCart cart;
    private final StockSubject stockSubject;
    private final ProductPanel productPanel;

    private JPanel itemsPanel;
    private JLabel subtotalLabel;
    private JLabel discountLabel;
    private JLabel shippingLabel;
    private JLabel totalLabel;
    private JLabel itemCountLabel;
    private JTextField couponField;

    // Track applied decorators per cart item index
    private final Map<Integer, Boolean> appliedDiscounts;
    private final Map<Integer, Boolean> appliedCoupons;
    private final Map<Integer, Boolean> appliedFreeShipping;

    // Valid coupon codes
    private static final Map<String, Double> VALID_COUPONS = new HashMap<>();
    static {
        VALID_COUPONS.put("WELCOME10", 10.0);
        VALID_COUPONS.put("INDIRIM20", 20.0);
        VALID_COUPONS.put("SUPER50", 50.0);
        VALID_COUPONS.put("KARGO", 0.0); // Special: free shipping only
    }

    // Colors
    private static final Color BG_COLOR = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color ACCENT = new Color(99, 102, 241);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color WARNING = new Color(245, 158, 11);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);

    public CartPanel(StockSubject stockSubject, ProductPanel productPanel) {
        this.cart = ShoppingCart.getInstance();
        this.stockSubject = stockSubject;
        this.productPanel = productPanel;
        this.appliedDiscounts = new HashMap<>();
        this.appliedCoupons = new HashMap<>();
        this.appliedFreeShipping = new HashMap<>();

        cart.addChangeListener(this);

        setLayout(new BorderLayout(0, 12));
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));
        setPreferredSize(new Dimension(380, 0));

        add(createHeader(), BorderLayout.NORTH);
        add(createCartItemsScroll(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        refreshCart();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG_COLOR);

        JLabel title = new JLabel("🛒 Sepetim");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT_PRIMARY);

        itemCountLabel = new JLabel("0 ürün");
        itemCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        itemCountLabel.setForeground(TEXT_SECONDARY);

        header.add(title, BorderLayout.WEST);
        header.add(itemCountLabel, BorderLayout.EAST);

        return header;
    }

    private JScrollPane createCartItemsScroll() {
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(BG_COLOR);

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBackground(BG_COLOR);
        scroll.getViewport().setBackground(BG_COLOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        return scroll;
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(BG_COLOR);

        // Coupon section
        JPanel couponPanel = new JPanel(new BorderLayout(8, 0));
        couponPanel.setBackground(CARD_BG);
        couponPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        couponPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        couponField = new JTextField();
        couponField.setBackground(new Color(15, 23, 42));
        couponField.setForeground(TEXT_PRIMARY);
        couponField.setCaretColor(TEXT_PRIMARY);
        couponField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        couponField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        couponField.setToolTipText("Kupon kodu girin (WELCOME10, INDIRIM20, SUPER50, KARGO)");

        JButton couponBtn = createStyledButton("Kupon Uygula", ACCENT, 120);
        couponBtn.addActionListener(e -> applyCoupon());

        couponPanel.add(couponField, BorderLayout.CENTER);
        couponPanel.add(couponBtn, BorderLayout.EAST);

        // Totals section
        JPanel totalsPanel = new JPanel();
        totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));
        totalsPanel.setBackground(CARD_BG);
        totalsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        totalsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        subtotalLabel = createTotalLine("Ara Toplam:", "0.00 ₺", TEXT_SECONDARY);
        discountLabel = createTotalLine("İndirimler:", "-0.00 ₺", WARNING);
        shippingLabel = createTotalLine("Kargo:", "29.99 ₺", TEXT_SECONDARY);
        totalLabel = createTotalLine("TOPLAM:", "0.00 ₺", SUCCESS);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        totalsPanel.add(subtotalLabel);
        totalsPanel.add(Box.createVerticalStrut(4));
        totalsPanel.add(discountLabel);
        totalsPanel.add(Box.createVerticalStrut(4));
        totalsPanel.add(shippingLabel);
        totalsPanel.add(Box.createVerticalStrut(8));
        totalsPanel.add(new JSeparator());
        totalsPanel.add(Box.createVerticalStrut(8));
        totalsPanel.add(totalLabel);

        // Action buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        actionPanel.setBackground(BG_COLOR);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton clearBtn = createStyledButton("🗑️ Sepeti Temizle", DANGER, 150);
        clearBtn.addActionListener(e -> clearCart());

        JButton checkoutBtn = createStyledButton("💳 Ödeme Yap", SUCCESS, 150);
        checkoutBtn.addActionListener(e -> checkout());

        actionPanel.add(clearBtn);
        actionPanel.add(checkoutBtn);

        bottom.add(couponPanel);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(totalsPanel);
        bottom.add(Box.createVerticalStrut(8));
        bottom.add(actionPanel);

        return bottom;
    }

    @Override
    public void onCartChanged() {
        SwingUtilities.invokeLater(this::refreshCart);
    }

    /**
     * Refresh cart display with current items and totals.
     */
    public void refreshCart() {
        itemsPanel.removeAll();
        List<CartItem> items = cart.getItems();

        if (items.isEmpty()) {
            JLabel emptyLabel = new JLabel("Sepetiniz boş");
            emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            emptyLabel.setForeground(TEXT_SECONDARY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setAlignmentX(CENTER_ALIGNMENT);
            itemsPanel.add(Box.createVerticalGlue());
            itemsPanel.add(emptyLabel);
            itemsPanel.add(Box.createVerticalGlue());
        } else {
            for (int i = 0; i < items.size(); i++) {
                itemsPanel.add(createCartItemCard(items.get(i), i));
                itemsPanel.add(Box.createVerticalStrut(6));
            }
        }

        // Update totals
        itemCountLabel.setText(cart.getItemCount() + " ürün");
        updateTotals();

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createCartItemCard(CartItem item, int index) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        // Product info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLabel = new JLabel(item.getProduct().getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);

        // Show applied decorators
        String desc = item.getProduct().getDescription();
        if (desc.contains("[")) {
            JLabel decoLabel = new JLabel(desc.substring(desc.indexOf('[')));
            decoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
            decoLabel.setForeground(WARNING);
            decoLabel.setAlignmentX(LEFT_ALIGNMENT);
            info.add(nameLabel);
            info.add(Box.createVerticalStrut(2));
            info.add(decoLabel);
        } else {
            info.add(nameLabel);
        }

        JLabel priceLabel = new JLabel(String.format("%.2f ₺ x %d = %.2f ₺",
                item.getProduct().getPrice(), item.getQuantity(), item.getSubtotal()));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        priceLabel.setForeground(SUCCESS);
        priceLabel.setAlignmentX(LEFT_ALIGNMENT);

        if (item.getProduct().hasFreeShipping()) {
            JLabel shipLabel = new JLabel("🚚 Ücretsiz Kargo");
            shipLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
            shipLabel.setForeground(new Color(56, 189, 248));
            shipLabel.setAlignmentX(LEFT_ALIGNMENT);
            info.add(Box.createVerticalStrut(2));
            info.add(shipLabel);
        }

        info.add(Box.createVerticalStrut(4));
        info.add(priceLabel);

        // Action buttons
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(90, 80));

        // Quantity controls
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        qtyPanel.setOpaque(false);

        JButton minusBtn = createSmallButton("-");
        JLabel qtyLabel = new JLabel(String.valueOf(item.getQuantity()));
        qtyLabel.setForeground(TEXT_PRIMARY);
        qtyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JButton plusBtn = createSmallButton("+");

        final int idx = index;
        minusBtn.addActionListener(e -> {
            cart.updateQuantity(idx, item.getQuantity() - 1);
            if (item.getQuantity() - 1 <= 0) {
                stockSubject.increaseStock(item.getOriginalProduct().getName(), item.getQuantity());
                item.getOriginalProduct().setStock(stockSubject.getStock(item.getOriginalProduct().getName()));
            } else {
                stockSubject.increaseStock(item.getOriginalProduct().getName(), 1);
                item.getOriginalProduct().setStock(stockSubject.getStock(item.getOriginalProduct().getName()));
            }
            productPanel.refreshProducts();
        });
        plusBtn.addActionListener(e -> {
            int stock = stockSubject.getStock(item.getOriginalProduct().getName());
            if (stock > 0) {
                cart.updateQuantity(idx, item.getQuantity() + 1);
                stockSubject.decreaseStock(item.getOriginalProduct().getName(), 1);
                item.getOriginalProduct().setStock(stockSubject.getStock(item.getOriginalProduct().getName()));
                productPanel.refreshProducts();
            } else {
                JOptionPane.showMessageDialog(CartPanel.this, "Stok yetersiz!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            }
        });

        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);

        JButton removeBtn = createStyledButton("🗑️", DANGER, 60);
        removeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        removeBtn.setAlignmentX(CENTER_ALIGNMENT);
        removeBtn.addActionListener(e -> {
            stockSubject.increaseStock(item.getOriginalProduct().getName(), item.getQuantity());
            item.getOriginalProduct().setStock(stockSubject.getStock(item.getOriginalProduct().getName()));
            cart.removeItem(idx);
            productPanel.refreshProducts();
        });

        // Decorator buttons
        JPanel decoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        decoPanel.setOpaque(false);

        JButton discBtn = createSmallButton("%");
        discBtn.setToolTipText("10% İndirim Uygula");
        discBtn.addActionListener(e -> applyDiscountToItem(idx));

        JButton shipBtn = createSmallButton("🚚");
        shipBtn.setToolTipText("Ücretsiz Kargo");
        shipBtn.addActionListener(e -> applyFreeShippingToItem(idx));

        decoPanel.add(discBtn);
        decoPanel.add(shipBtn);

        actions.add(qtyPanel);
        actions.add(Box.createVerticalStrut(2));
        actions.add(decoPanel);
        actions.add(Box.createVerticalStrut(2));
        actions.add(removeBtn);

        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);

        return card;
    }

    /**
     * Apply percentage discount decorator to a cart item.
     */
    private void applyDiscountToItem(int index) {
        List<CartItem> items = cart.getItems();
        if (index < 0 || index >= items.size()) return;

        CartItem item = items.get(index);
        ProductComponent current = item.getProduct();

        // Apply 10% discount decorator
        ProductComponent decorated = new DiscountDecorator(current, 10);

        // Replace in cart
        cart.removeItem(index);
        cart.addItem(decorated, item.getOriginalProduct(), item.getQuantity());

        JOptionPane.showMessageDialog(this,
                "%10 indirim uygulandı: " + item.getProduct().getName(),
                "İndirim", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Apply free shipping decorator to a cart item.
     */
    private void applyFreeShippingToItem(int index) {
        List<CartItem> items = cart.getItems();
        if (index < 0 || index >= items.size()) return;

        CartItem item = items.get(index);
        if (item.getProduct().hasFreeShipping()) {
            JOptionPane.showMessageDialog(this,
                    "Bu ürün zaten ücretsiz kargolu!",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ProductComponent decorated = new FreeShippingDecorator(item.getProduct());

        cart.removeItem(index);
        cart.addItem(decorated, item.getOriginalProduct(), item.getQuantity());

        JOptionPane.showMessageDialog(this,
                "Ücretsiz kargo uygulandı: " + item.getProduct().getName(),
                "Kargo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Apply coupon code to all items in the cart.
     */
    private void applyCoupon() {
        String code = couponField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Lütfen bir kupon kodu girin!",
                    "Kupon Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!VALID_COUPONS.containsKey(code)) {
            JOptionPane.showMessageDialog(this,
                    "Geçersiz kupon kodu: " + code,
                    "Kupon Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Sepetiniz boş!",
                    "Kupon Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double amount = VALID_COUPONS.get(code);
        List<CartItem> items = cart.getItems();

        if ("KARGO".equals(code)) {
            // Apply free shipping to first item
            CartItem firstItem = items.get(0);
            if (!firstItem.getProduct().hasFreeShipping()) {
                ProductComponent decorated = new FreeShippingDecorator(firstItem.getProduct());
                int qty = firstItem.getQuantity();
                cart.removeItem(0);
                cart.addItem(decorated, firstItem.getOriginalProduct(), qty);
            }
            JOptionPane.showMessageDialog(this,
                    "🚚 Ücretsiz kargo kuponu uygulandı!",
                    "Kupon Başarılı", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Apply coupon to first item
            CartItem firstItem = items.get(0);
            ProductComponent decorated = new CouponDecorator(firstItem.getProduct(), amount, code);
            int qty = firstItem.getQuantity();
            cart.removeItem(0);
            cart.addItem(decorated, firstItem.getOriginalProduct(), qty);
            JOptionPane.showMessageDialog(this,
                    "🎫 Kupon uygulandı: -" + (int) amount + "₺ (" + code + ")",
                    "Kupon Başarılı", JOptionPane.INFORMATION_MESSAGE);
        }

        couponField.setText("");
    }

    private void updateTotals() {
        double subtotal = cart.getTotal();
        double shipping = cart.getShippingCost();
        double total = cart.getGrandTotal();

        subtotalLabel.setText(String.format("Ara Toplam:          %.2f ₺", subtotal));
        shippingLabel.setText(String.format("Kargo:               %s",
                shipping == 0 ? "Ücretsiz 🎉" : String.format("%.2f ₺", shipping)));
        totalLabel.setText(String.format("TOPLAM:              %.2f ₺", total));
    }

    private void clearCart() {
        if (cart.isEmpty()) return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Sepeti temizlemek istediğinize emin misiniz?",
                "Sepeti Temizle", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            // Restore stock for all items
            for (CartItem item : cart.getItems()) {
                stockSubject.increaseStock(item.getOriginalProduct().getName(), item.getQuantity());
                item.getOriginalProduct().setStock(stockSubject.getStock(item.getOriginalProduct().getName()));
            }
            cart.clear();
            productPanel.refreshProducts();
        }
    }

    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Sepetiniz boş!",
                    "Ödeme", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StringBuilder receipt = new StringBuilder();
        receipt.append("═══════════════════════════════\n");
        receipt.append("        SİPARİŞ ÖZETİ\n");
        receipt.append("═══════════════════════════════\n\n");

        for (CartItem item : cart.getItems()) {
            receipt.append(String.format("  %s\n", item.getProduct().getName()));
            receipt.append(String.format("    %d x %.2f₺ = %.2f₺\n",
                    item.getQuantity(), item.getProduct().getPrice(), item.getSubtotal()));
            if (item.getProduct().hasFreeShipping()) {
                receipt.append("    🚚 Ücretsiz Kargo\n");
            }
            String desc = item.getProduct().getDescription();
            if (desc.contains("[")) {
                receipt.append("    " + desc.substring(desc.indexOf('[')) + "\n");
            }
            receipt.append("\n");
        }

        receipt.append("───────────────────────────────\n");
        receipt.append(String.format("  Ara Toplam: %.2f₺\n", cart.getTotal()));
        receipt.append(String.format("  Kargo:      %s\n",
                cart.getShippingCost() == 0 ? "Ücretsiz" : String.format("%.2f₺", cart.getShippingCost())));
        receipt.append(String.format("  TOPLAM:     %.2f₺\n", cart.getGrandTotal()));
        receipt.append("═══════════════════════════════\n");
        receipt.append("\n✅ Siparişiniz başarıyla oluşturuldu!");

        JTextArea receiptArea = new JTextArea(receipt.toString());
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setEditable(false);

        JOptionPane.showMessageDialog(this,
                new JScrollPane(receiptArea),
                "💳 Ödeme Tamamlandı", JOptionPane.INFORMATION_MESSAGE);

        cart.clear();
        productPanel.refreshProducts();
    }

    private JLabel createTotalLine(String label, String value, Color color) {
        JLabel lbl = new JLabel(String.format("%-20s %s", label, value));
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lbl.setForeground(color);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton createStyledButton(String text, Color bgColor, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bgColor);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(width, 30));

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

    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setForeground(TEXT_PRIMARY);
        btn.setBackground(BORDER_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(30, 24));
        btn.setMargin(new Insets(0, 0, 0, 0));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(ACCENT);
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(BORDER_COLOR);
            }
        });

        return btn;
    }
}
