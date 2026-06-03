package main.ui.utils;

import javax.swing.*;
import java.awt.*;

/**
 * Shared UI utilities, colors, and component factories.
 * Centralizes the dark theme design system for consistency.
 */
public class UIUtils {

    // --- Color Palette ---
    public static final Color BG_COLOR = new Color(15, 23, 42);
    public static final Color CARD_BG = new Color(30, 41, 59);
    public static final Color CARD_HOVER = new Color(51, 65, 85);
    public static final Color HEADER_BG = new Color(30, 41, 59);
    public static final Color ACCENT = new Color(99, 102, 241);
    public static final Color ACCENT_HOVER = new Color(129, 140, 248);
    public static final Color SUCCESS = new Color(34, 197, 94);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = new Color(239, 68, 68);
    public static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    public static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    public static final Color BORDER_COLOR = new Color(51, 65, 85);
    public static final Color NOTIF_BG = new Color(22, 33, 50);
    public static final Color SELLER_ACCENT = new Color(16, 185, 129);
    public static final Color BUYER_ACCENT = new Color(99, 102, 241);

    // --- Fonts ---
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font MONO_FONT = new Font("Consolas", Font.PLAIN, 12);
    public static final Font BUTTON_FONT = new Font("Segoe UI", Font.BOLD, 12);

    private UIUtils() {} // Prevent instantiation

    // --- Component Factories ---

    /**
     * Create a styled button with hover effect.
     */
    public static JButton createStyledButton(String text, Color bgColor, int width) {
        JButton btn = new JButton(text);
        btn.setFont(BUTTON_FONT);
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

    /**
     * Create a small square button (for +/- controls).
     */
    public static JButton createSmallButton(String text) {
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

    /**
     * Create a badge label.
     */
    public static JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(color);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return badge;
    }

    /**
     * Create a styled text field.
     */
    public static JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        field.setBackground(CARD_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    /**
     * Create a styled password field.
     */
    public static JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        field.setBackground(CARD_BG);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(TEXT_PRIMARY);
        field.setFont(BODY_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return field;
    }

    /**
     * Create a styled label.
     */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    /**
     * Get stock display text with emoji.
     */
    public static String getStockText(int stock) {
        if (stock == 0) return "❌ Stokta Yok";
        if (stock <= 5) return "⚡ Son " + stock + " adet!";
        return "✅ Stok: " + stock + " adet";
    }

    /**
     * Get stock color based on level.
     */
    public static Color getStockColor(int stock) {
        if (stock == 0) return DANGER;
        if (stock <= 5) return WARNING;
        return SUCCESS;
    }
}
