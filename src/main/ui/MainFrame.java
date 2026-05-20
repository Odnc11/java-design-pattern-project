package main.ui;

import main.models.Product;
import main.patterns.observer.*;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

/**
 * Main application window - E-Commerce Shopping System
 * 
 * Integrates all components:
 * - ProductPanel (tab 1): Product catalog with search/filter
 * - AuctionPanel (tab 2): Demand-driven auction system
 * - CartPanel (right): Shopping cart with decorator controls
 * - Notification area (bottom): Observer pattern notifications
 */
public class MainFrame extends JFrame {
    private ProductPanel productPanel;
    private CartPanel cartPanel;
    private AuctionPanel auctionPanel;
    private JTextArea notificationArea;

    private final List<Product> products;
    private final StockSubject stockSubject;
    private final StockObserver stockObserver;
    private final NotificationObserver notificationObserver;
    private final DemandTracker demandTracker;
    private final AuctionEventObserver auctionEventObserver;

    // Colors
    private static final Color BG_COLOR = new Color(15, 23, 42);
    private static final Color HEADER_BG = new Color(30, 41, 59);
    private static final Color ACCENT = new Color(99, 102, 241);
    private static final Color TEXT_PRIMARY = new Color(241, 245, 249);
    private static final Color TEXT_SECONDARY = new Color(148, 163, 184);
    private static final Color BORDER_COLOR = new Color(51, 65, 85);
    private static final Color NOTIF_BG = new Color(22, 33, 50);

    public MainFrame(List<Product> products, StockSubject stockSubject,
                     StockObserver stockObserver, NotificationObserver notificationObserver,
                     DemandTracker demandTracker, AuctionEventObserver auctionEventObserver) {
        this.products = products;
        this.stockSubject = stockSubject;
        this.stockObserver = stockObserver;
        this.notificationObserver = notificationObserver;
        this.demandTracker = demandTracker;
        this.auctionEventObserver = auctionEventObserver;

        setupFrame();
        setupComponents();
        setupNotificationListener();
    }

    private void setupFrame() {
        setTitle("🛒 E-Commerce Shopping System - Design Patterns Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG_COLOR);
        setLayout(new BorderLayout(0, 0));
    }

    private void setupComponents() {
        // Header
        add(createHeaderPanel(), BorderLayout.NORTH);

        // Create panels
        productPanel = new ProductPanel(products, stockSubject, notificationObserver);
        productPanel.setDemandTracker(demandTracker); // Connect demand tracking to product display
        cartPanel = new CartPanel(stockSubject, productPanel);
        auctionPanel = new AuctionPanel(products, demandTracker, stockSubject,
                auctionEventObserver, productPanel);

        // Tabbed pane for Product Catalog and Auction
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(HEADER_BG);
        tabbedPane.setForeground(TEXT_PRIMARY);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("🛍️ Ürün Kataloğu", productPanel);
        tabbedPane.addTab("🔥 Canlı Müzayede", auctionPanel);

        // Refresh auction panel when tab is selected
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                auctionPanel.refreshAuctions();
            }
        });

        // Main content: Tabs + Cart Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabbedPane, cartPanel);
        splitPane.setDividerLocation(720);
        splitPane.setResizeWeight(0.65);
        splitPane.setBackground(BG_COLOR);
        splitPane.setBorder(null);
        splitPane.setDividerSize(3);

        add(splitPane, BorderLayout.CENTER);

        // Notification area (bottom)
        add(createNotificationPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(HEADER_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)
        ));

        // Logo and title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titlePanel.setOpaque(false);

        JLabel logo = new JLabel("🛒");
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 28));

        JLabel title = new JLabel("E-Commerce Shopping System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("  |  Design Patterns Demo");
        subtitle.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        subtitle.setForeground(TEXT_SECONDARY);

        titlePanel.add(logo);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Pattern badges
        JPanel badgesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        badgesPanel.setOpaque(false);

        badgesPanel.add(createBadge("Singleton", new Color(139, 92, 246)));
        badgesPanel.add(createBadge("Decorator", new Color(59, 130, 246)));
        badgesPanel.add(createBadge("Observer", new Color(16, 185, 129)));
        badgesPanel.add(createBadge("Müzayede", new Color(236, 72, 153)));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(badgesPanel, BorderLayout.EAST);

        return header;
    }

    private JLabel createBadge(String text, Color color) {
        JLabel badge = new JLabel(" " + text + " ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setOpaque(true);
        badge.setBackground(color);
        badge.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        return badge;
    }

    private JPanel createNotificationPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setBackground(NOTIF_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        panel.setPreferredSize(new Dimension(0, 130));

        JLabel notifTitle = new JLabel("🔔 Bildirimler (Observer Pattern)");
        notifTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        notifTitle.setForeground(TEXT_PRIMARY);

        notificationArea = new JTextArea(4, 80);
        notificationArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        notificationArea.setBackground(new Color(15, 23, 42));
        notificationArea.setForeground(TEXT_SECONDARY);
        notificationArea.setEditable(false);
        notificationArea.setLineWrap(true);
        notificationArea.setWrapStyleWord(true);
        notificationArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scrollPane = new JScrollPane(notificationArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton clearNotifBtn = new JButton("Temizle");
        clearNotifBtn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        clearNotifBtn.setForeground(TEXT_SECONDARY);
        clearNotifBtn.setBackground(BORDER_COLOR);
        clearNotifBtn.setFocusPainted(false);
        clearNotifBtn.setBorderPainted(false);
        clearNotifBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearNotifBtn.addActionListener(e -> notificationArea.setText(""));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(notifTitle, BorderLayout.WEST);
        headerRow.add(clearNotifBtn, BorderLayout.EAST);

        panel.add(headerRow, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Connect NotificationObserver to the UI notification area.
     */
    private void setupNotificationListener() {
        notificationObserver.addListener(message -> {
            SwingUtilities.invokeLater(() -> {
                notificationArea.append(message + "\n");
                // Auto-scroll to bottom
                notificationArea.setCaretPosition(notificationArea.getDocument().getLength());
            });
        });
    }
}
