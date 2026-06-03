package main.ui.panels;

import main.patterns.observer.PriceNotificationObserver;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Notification panel displaying real-time Observer pattern notifications.
 * Shows price changes, stock alerts, and discount updates from PriceNotificationObserver.
 */
public class NotificationPanel extends JPanel {
    private final PriceNotificationObserver notificationObserver;
    private JTextArea notificationArea;

    public NotificationPanel(PriceNotificationObserver notificationObserver) {
        this.notificationObserver = notificationObserver;

        setLayout(new BorderLayout(0, 4));
        setBackground(UIUtils.NOTIF_BG);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        setPreferredSize(new Dimension(0, 130));

        JLabel title = UIUtils.createLabel("🔔 Bildirimler (Observer Pattern)",
                new Font("Segoe UI", Font.BOLD, 13), UIUtils.TEXT_PRIMARY);

        notificationArea = new JTextArea(4, 80);
        notificationArea.setFont(UIUtils.MONO_FONT);
        notificationArea.setBackground(UIUtils.BG_COLOR);
        notificationArea.setForeground(UIUtils.TEXT_SECONDARY);
        notificationArea.setEditable(false);
        notificationArea.setLineWrap(true);
        notificationArea.setWrapStyleWord(true);
        notificationArea.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JScrollPane scrollPane = new JScrollPane(notificationArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.BORDER_COLOR));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JButton clearBtn = new JButton("Temizle");
        clearBtn.setFont(UIUtils.SMALL_FONT);
        clearBtn.setForeground(UIUtils.TEXT_SECONDARY);
        clearBtn.setBackground(UIUtils.BORDER_COLOR);
        clearBtn.setFocusPainted(false);
        clearBtn.setBorderPainted(false);
        clearBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearBtn.addActionListener(e -> notificationArea.setText(""));

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.add(title, BorderLayout.WEST);
        headerRow.add(clearBtn, BorderLayout.EAST);

        add(headerRow, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setupNotificationListener();
    }

    private void setupNotificationListener() {
        notificationObserver.addListener(message -> {
            SwingUtilities.invokeLater(() -> {
                notificationArea.append(message + "\n");
                notificationArea.setCaretPosition(notificationArea.getDocument().getLength());
            });
        });
        
        main.patterns.observer.DemandPricingObserver.addListener(message -> {
            SwingUtilities.invokeLater(() -> {
                notificationArea.append(message + "\n");
                notificationArea.setCaretPosition(notificationArea.getDocument().getLength());
            });
        });
    }
}
