package main.ui.panels;

import main.database.OrderDatabase;
import main.models.Buyer;
import main.models.CartItem;
import main.models.Product;
import main.patterns.singleton.ShoppingCart;
import main.patterns.decorator.*;
import main.patterns.observer.StockSubject;
import main.utils.SessionManager;
import main.utils.CouponValidator;
import main.utils.CouponValidator.CouponInfo;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Shopping cart panel for buyers.
 * Displays cart items with buyer-applied decorator controls (coupons, discounts, free shipping).
 * Supports checkout which creates an order in OrderDatabase.
 */
public class ShoppingCartPanel extends JPanel implements ShoppingCart.CartChangeListener {
    private final StockSubject stockSubject;
    private final ProductCatalogPanel catalogPanel;
    private ShoppingCart cart;

    private JPanel itemsPanel;
    private JLabel subtotalLabel;
    private JLabel shippingLabel;
    private JLabel totalLabel;
    private JLabel itemCountLabel;
    private JTextField couponField;

    public ShoppingCartPanel(StockSubject stockSubject, ProductCatalogPanel catalogPanel) {
        this.stockSubject = stockSubject;
        this.catalogPanel = catalogPanel;

        setLayout(new BorderLayout(0, 12));
        setBackground(UIUtils.BG_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 8, 16, 16));
        setPreferredSize(new Dimension(380, 0));

        add(createHeader(), BorderLayout.NORTH);
        add(createCartItemsScroll(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /**
     * Bind this panel to the current buyer's cart.
     */
    public void bindToCurrentUser() {
        if (cart != null) {
            cart.removeChangeListener(this);
        }
        if (SessionManager.isBuyer()) {
            cart = ShoppingCart.getInstance(SessionManager.getCurrentUser().getId());
            cart.addChangeListener(this);
        }
        refreshCart();
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIUtils.BG_COLOR);

        JLabel title = UIUtils.createLabel("🛒 Sepetim", UIUtils.SUBTITLE_FONT, UIUtils.TEXT_PRIMARY);
        itemCountLabel = UIUtils.createLabel("0 ürün", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);

        header.add(title, BorderLayout.WEST);
        header.add(itemCountLabel, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createCartItemsScroll() {
        itemsPanel = new JPanel();
        itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
        itemsPanel.setBackground(UIUtils.BG_COLOR);

        JScrollPane scroll = new JScrollPane(itemsPanel);
        scroll.setBackground(UIUtils.BG_COLOR);
        scroll.getViewport().setBackground(UIUtils.BG_COLOR);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel createBottomPanel() {
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBackground(UIUtils.BG_COLOR);

        // Coupon section
        JPanel couponPanel = new JPanel(new BorderLayout(8, 0));
        couponPanel.setBackground(UIUtils.CARD_BG);
        couponPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        couponPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        couponField = UIUtils.createStyledTextField(12);
        couponField.setToolTipText(CouponValidator.getAllCouponsDisplay());

        JButton couponBtn = UIUtils.createStyledButton("Kupon Uygula", UIUtils.ACCENT, 120);
        couponBtn.addActionListener(e -> applyCoupon());

        couponPanel.add(couponField, BorderLayout.CENTER);
        couponPanel.add(couponBtn, BorderLayout.EAST);

        // Totals section
        JPanel totalsPanel = new JPanel();
        totalsPanel.setLayout(new BoxLayout(totalsPanel, BoxLayout.Y_AXIS));
        totalsPanel.setBackground(UIUtils.CARD_BG);
        totalsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));
        totalsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        subtotalLabel = UIUtils.createLabel("Ara Toplam: 0.00 ₺", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        subtotalLabel.setAlignmentX(LEFT_ALIGNMENT);
        shippingLabel = UIUtils.createLabel("Kargo: 29.99 ₺", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        shippingLabel.setAlignmentX(LEFT_ALIGNMENT);
        totalLabel = UIUtils.createLabel("TOPLAM: 0.00 ₺", UIUtils.SUBTITLE_FONT, UIUtils.SUCCESS);
        totalLabel.setAlignmentX(LEFT_ALIGNMENT);

        totalsPanel.add(subtotalLabel);
        totalsPanel.add(Box.createVerticalStrut(4));
        totalsPanel.add(shippingLabel);
        totalsPanel.add(Box.createVerticalStrut(8));
        totalsPanel.add(new JSeparator());
        totalsPanel.add(Box.createVerticalStrut(8));
        totalsPanel.add(totalLabel);

        // Action buttons
        JPanel actionPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        actionPanel.setBackground(UIUtils.BG_COLOR);
        actionPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JButton clearBtn = UIUtils.createStyledButton("🗑️ Temizle", UIUtils.DANGER, 150);
        clearBtn.addActionListener(e -> clearCart());

        JButton checkoutBtn = UIUtils.createStyledButton("💳 Ödeme Yap", UIUtils.SUCCESS, 150);
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

    public void refreshCart() {
        itemsPanel.removeAll();

        if (cart == null || cart.isEmpty()) {
            JLabel emptyLabel = UIUtils.createLabel("Sepetiniz boş", new Font("Segoe UI", Font.ITALIC, 14), UIUtils.TEXT_SECONDARY);
            emptyLabel.setHorizontalAlignment(SwingConstants.CENTER);
            emptyLabel.setAlignmentX(CENTER_ALIGNMENT);
            itemsPanel.add(Box.createVerticalGlue());
            itemsPanel.add(emptyLabel);
            itemsPanel.add(Box.createVerticalGlue());
        } else {
            List<CartItem> items = cart.getItems();
            for (int i = 0; i < items.size(); i++) {
                itemsPanel.add(createCartItemCard(items.get(i), i));
                itemsPanel.add(Box.createVerticalStrut(6));
            }
        }

        itemCountLabel.setText(cart != null ? cart.getItemCount() + " ürün" : "0 ürün");
        updateTotals();
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createCartItemCard(CartItem item, int index) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(UIUtils.CARD_BG);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        // Info
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel nameLabel = UIUtils.createLabel(item.getDecoratedProduct().getName(),
                new Font("Segoe UI", Font.BOLD, 13), UIUtils.TEXT_PRIMARY);
        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        info.add(nameLabel);

        String desc = item.getDecoratedProduct().getDescription();
        if (desc.contains("[")) {
            JLabel decoLabel = UIUtils.createLabel(desc.substring(desc.indexOf('[')),
                    new Font("Segoe UI", Font.ITALIC, 10), UIUtils.WARNING);
            decoLabel.setAlignmentX(LEFT_ALIGNMENT);
            info.add(Box.createVerticalStrut(2));
            info.add(decoLabel);
        }

        if (item.getDecoratedProduct().hasFreeShipping()) {
            JLabel shipLabel = UIUtils.createLabel("🚚 Ücretsiz Kargo",
                    new Font("Segoe UI", Font.BOLD, 10), new Color(56, 189, 248));
            shipLabel.setAlignmentX(LEFT_ALIGNMENT);
            info.add(Box.createVerticalStrut(2));
            info.add(shipLabel);
        }

        JLabel priceLabel = UIUtils.createLabel(
                String.format("%.2f ₺ x %d = %.2f ₺", item.getUnitPrice(), item.getQuantity(), item.getSubtotal()),
                UIUtils.SMALL_FONT, UIUtils.SUCCESS);
        priceLabel.setAlignmentX(LEFT_ALIGNMENT);
        info.add(Box.createVerticalStrut(4));
        info.add(priceLabel);

        // Actions
        JPanel actions = new JPanel();
        actions.setLayout(new BoxLayout(actions, BoxLayout.Y_AXIS));
        actions.setOpaque(false);
        actions.setPreferredSize(new Dimension(90, 80));

        // Qty controls
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        qtyPanel.setOpaque(false);

        JButton minusBtn = UIUtils.createSmallButton("-");
        JLabel qtyLabel = UIUtils.createLabel(String.valueOf(item.getQuantity()),
                new Font("Segoe UI", Font.BOLD, 13), UIUtils.TEXT_PRIMARY);
        JButton plusBtn = UIUtils.createSmallButton("+");

        final int idx = index;
        minusBtn.addActionListener(e -> {
            cart.updateQuantity(idx, item.getQuantity() - 1);
            catalogPanel.refreshProducts();
        });

        plusBtn.addActionListener(e -> {
            int stock = stockSubject.getStock(item.getOriginalProduct().getName());
            int cartQty = cart.getQuantity(item.getOriginalProduct());
            if (cartQty < stock) {
                cart.updateQuantity(idx, item.getQuantity() + 1);
                catalogPanel.refreshProducts();
            } else {
                JOptionPane.showMessageDialog(this, "Yeterli stok bulunmuyor!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            }
        });

        qtyPanel.add(minusBtn);
        qtyPanel.add(qtyLabel);
        qtyPanel.add(plusBtn);

        JButton removeBtn = UIUtils.createStyledButton("🗑️", UIUtils.DANGER, 60);
        removeBtn.setFont(UIUtils.SMALL_FONT);
        removeBtn.setAlignmentX(CENTER_ALIGNMENT);
        removeBtn.addActionListener(e -> {
            cart.removeItem(idx);
            catalogPanel.refreshProducts();
        });

        actions.add(qtyPanel);
        actions.add(Box.createVerticalStrut(5));
        actions.add(removeBtn);

        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    private void applyPercentageDiscount(int index) {
        if (cart == null) return;
        List<CartItem> items = cart.getItems();
        if (index < 0 || index >= items.size()) return;

        CartItem item = items.get(index);
        ProductComponent decorated = new PercentageDiscountDecorator(item.getDecoratedProduct(), 10);

        cart.removeItem(index);
        cart.addItem(decorated, item.getOriginalProduct(), item.getQuantity());

        JOptionPane.showMessageDialog(this,
                "%10 indirim uygulandı: " + item.getDecoratedProduct().getName(),
                "İndirim", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyFreeShipping(int index) {
        if (cart == null) return;
        List<CartItem> items = cart.getItems();
        if (index < 0 || index >= items.size()) return;

        CartItem item = items.get(index);
        if (item.getDecoratedProduct().hasFreeShipping()) {
            JOptionPane.showMessageDialog(this, "Bu ürün zaten ücretsiz kargolu!",
                    "Bilgi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        ProductComponent decorated = new FreeShippingDecorator(item.getDecoratedProduct());
        cart.removeItem(index);
        cart.addItem(decorated, item.getOriginalProduct(), item.getQuantity());

        JOptionPane.showMessageDialog(this,
                "Ücretsiz kargo uygulandı: " + item.getDecoratedProduct().getName(),
                "Kargo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void applyCoupon() {
        if (cart == null || cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sepetiniz boş!", "Kupon Hatası", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String code = couponField.getText().trim().toUpperCase();
        CouponInfo coupon = CouponValidator.validate(code);

        if (coupon == null) {
            JOptionPane.showMessageDialog(this, "Geçersiz kupon kodu: " + code,
                    "Kupon Hatası", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<CartItem> items = cart.getItems();
        CartItem firstItem = items.get(0);
        int qty = firstItem.getQuantity();
        ProductComponent decorated;

        switch (coupon.getType()) {
            case PERCENTAGE:
                decorated = new PercentageDiscountDecorator(firstItem.getDecoratedProduct(), coupon.getValue());
                break;
            case FIXED:
                decorated = new CouponDecorator(firstItem.getDecoratedProduct(), coupon.getValue(), code);
                break;
            case SHIPPING:
                decorated = new FreeShippingDecorator(firstItem.getDecoratedProduct());
                break;
            case FLASH:
                decorated = new FlashSaleDecorator(firstItem.getDecoratedProduct());
                break;
            default:
                return;
        }

        cart.removeItem(0);
        cart.addItem(decorated, firstItem.getOriginalProduct(), qty);

        couponField.setText("");
        JOptionPane.showMessageDialog(this,
                "🎫 Kupon uygulandı: " + coupon.getDescription(),
                "Kupon Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateTotals() {
        if (cart == null) return;
        double subtotal = cart.getTotal();
        double total = cart.getTotal();
        double shipping = cart.getShippingCost();

        subtotalLabel.setText(String.format("Ara Toplam:  %.2f ₺", total));
        
        main.models.Buyer buyer = SessionManager.getCurrentBuyer();
        double threshold = (buyer != null) ? buyer.getFreeShippingThreshold() : 500.0;
        
        if (shipping == 0) {
            shippingLabel.setText("Kargo:        Ücretsiz 🎉");
        } else {
            double remaining = threshold - total;
            shippingLabel.setText(String.format("Kargo:        %.2f ₺ (%.2f ₺ kaldı)", shipping, remaining));
        }
        
        totalLabel.setText(String.format("TOPLAM:        %.2f ₺", cart.getGrandTotal()));
    }

    private void clearCart() {
        if (cart == null || cart.isEmpty()) return;
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sepeti temizlemek istediğinize emin misiniz?",
                "Sepeti Temizle", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            cart.clear();
            catalogPanel.refreshProducts();
        }
    }

    private void checkout() {
        if (cart == null || cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sepetiniz boş!", "Ödeme", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pre-checkout Stock Validation
        for (CartItem item : cart.getItems()) {
            String name = item.getOriginalProduct().getName();
            int requiredQty = cart.getQuantity(item.getOriginalProduct());
            int actualStock = stockSubject.getStock(name);
            if (requiredQty > actualStock) {
                JOptionPane.showMessageDialog(this,
                        name + " için yeterli stok bulunmuyor! (İstenen: " + requiredQty + ", Kalan: " + actualStock + ")\nLütfen sepetinizi güncelleyin.",
                        "Stok Hatası", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Buyer buyer = SessionManager.getCurrentBuyer();
        if (buyer == null) return;

        // Perform actual stock deduction
        for (CartItem item : cart.getItems()) {
            stockSubject.decreaseStock(item.getOriginalProduct().getName(), item.getQuantity());
        }

        // Create order
        OrderDatabase.Order order = OrderDatabase.createOrder(
                buyer, cart.getItems(), cart.getTotal(), cart.getShippingCost(), cart.getGrandTotal());

        // Show receipt
        StringBuilder receipt = new StringBuilder();
        receipt.append("═══════════════════════════════\n");
        receipt.append("        SİPARİŞ ÖZETİ\n");
        receipt.append("═══════════════════════════════\n\n");
        receipt.append("Sipariş No: #").append(order.getOrderId()).append("\n\n");

        for (CartItem item : cart.getItems()) {
            receipt.append(String.format("  %s\n", item.getDecoratedProduct().getName()));
            receipt.append(String.format("    %d x %.2f₺ = %.2f₺\n",
                    item.getQuantity(), item.getUnitPrice(), item.getSubtotal()));
            if (item.getDecoratedProduct().hasFreeShipping()) {
                receipt.append("    🚚 Ücretsiz Kargo\n");
            }
            String desc = item.getDecoratedProduct().getDescription();
            if (desc.contains("[")) {
                receipt.append("    ").append(desc.substring(desc.indexOf('['))).append("\n");
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
        catalogPanel.refreshProducts();
    }
}
