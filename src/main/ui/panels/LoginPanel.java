package main.ui.panels;

import main.database.UserDatabase;
import main.models.User;
import main.utils.SessionManager;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Login panel - entry point for the application.
 * 
 * Supports:
 * - Buyer login (mock accounts or registered)
 * - Seller login (mock accounts only)
 * - Navigation to Register and Guest Browse
 */
public class LoginPanel extends JPanel {
    private final Runnable onBuyerLogin;
    private final Runnable onSellerLogin;
    private final Runnable onRegister;
    private final Runnable onGuestBrowse;

    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleSelector;
    private JLabel errorLabel;

    public LoginPanel(Runnable onBuyerLogin, Runnable onSellerLogin,
                      Runnable onRegister, Runnable onGuestBrowse) {
        this.onBuyerLogin = onBuyerLogin;
        this.onSellerLogin = onSellerLogin;
        this.onRegister = onRegister;
        this.onGuestBrowse = onGuestBrowse;

        setBackground(UIUtils.BG_COLOR);
        setLayout(new GridBagLayout());
        add(createLoginCard());
    }

    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIUtils.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        card.setPreferredSize(new Dimension(420, 520));

        // Logo
        JLabel logo = new JLabel("🛒", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        // Title
        JLabel title = UIUtils.createLabel("E-Commerce System", UIUtils.TITLE_FONT, UIUtils.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = UIUtils.createLabel("Design Patterns Demo", UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Role selector
        JLabel roleLabel = UIUtils.createLabel("Rol:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        roleLabel.setAlignmentX(CENTER_ALIGNMENT);

        roleSelector = new JComboBox<>(new String[]{"Buyer (Müşteri)", "Admin (Mağaza Yönetimi)"});
        roleSelector.setBackground(UIUtils.CARD_BG);
        roleSelector.setForeground(UIUtils.TEXT_PRIMARY);
        roleSelector.setFont(UIUtils.BODY_FONT);
        roleSelector.setMaximumSize(new Dimension(300, 35));
        roleSelector.setAlignmentX(CENTER_ALIGNMENT);

        // Email
        JLabel emailLabel = UIUtils.createLabel("E-posta:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        emailLabel.setAlignmentX(CENTER_ALIGNMENT);

        emailField = UIUtils.createStyledTextField(20);
        emailField.setMaximumSize(new Dimension(300, 38));
        emailField.setAlignmentX(CENTER_ALIGNMENT);
        emailField.setText("ahmet@test.com");

        // Password
        JLabel passLabel = UIUtils.createLabel("Şifre:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        passLabel.setAlignmentX(CENTER_ALIGNMENT);

        passwordField = UIUtils.createStyledPasswordField(20);
        passwordField.setMaximumSize(new Dimension(300, 38));
        passwordField.setAlignmentX(CENTER_ALIGNMENT);
        passwordField.setText("123");

        // Error label
        errorLabel = UIUtils.createLabel("", UIUtils.SMALL_FONT, UIUtils.DANGER);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Login button
        JButton loginBtn = UIUtils.createStyledButton("🔑 Giriş Yap", UIUtils.ACCENT, 300);
        loginBtn.setMaximumSize(new Dimension(300, 38));
        loginBtn.setAlignmentX(CENTER_ALIGNMENT);
        loginBtn.addActionListener(e -> performLogin());

        // Enter key triggers login
        passwordField.addActionListener(e -> performLogin());

        // Register link
        JButton registerBtn = new JButton("Hesabın yok mu? Kayıt Ol");
        registerBtn.setFont(UIUtils.SMALL_FONT);
        registerBtn.setForeground(UIUtils.ACCENT);
        registerBtn.setBackground(UIUtils.CARD_BG);
        registerBtn.setBorderPainted(false);
        registerBtn.setFocusPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> onRegister.run());

        // Guest browse link
        JButton guestBtn = new JButton("👀 Misafir Olarak Göz At");
        guestBtn.setFont(UIUtils.SMALL_FONT);
        guestBtn.setForeground(UIUtils.TEXT_SECONDARY);
        guestBtn.setBackground(UIUtils.CARD_BG);
        guestBtn.setBorderPainted(false);
        guestBtn.setFocusPainted(false);
        guestBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        guestBtn.setAlignmentX(CENTER_ALIGNMENT);
        guestBtn.addActionListener(e -> onGuestBrowse.run());



        // Layout
        card.add(logo);
        card.add(Box.createVerticalStrut(8));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(roleLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(roleSelector);
        card.add(Box.createVerticalStrut(12));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(emailField);
        card.add(Box.createVerticalStrut(12));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(6));
        card.add(guestBtn);
        card.add(Box.createVerticalStrut(12));

        return card;
    }

    private void performLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("E-posta ve şifre gerekli!");
            return;
        }

        boolean isSeller = roleSelector.getSelectedIndex() == 1;
        User.Role role = isSeller ? User.Role.SELLER : User.Role.BUYER;

        User user = UserDatabase.authenticate(email, password, role);

        if (user == null) {
            errorLabel.setText("Geçersiz e-posta veya şifre!");
            return;
        }

        errorLabel.setText("");
        SessionManager.login(user);

        if (isSeller) {
            onSellerLogin.run();
        } else {
            onBuyerLogin.run();
        }
    }

    /**
     * Reset form when returning to login screen.
     */
    public void reset() {
        emailField.setText("ahmet@test.com");
        passwordField.setText("123");
        roleSelector.setSelectedIndex(0);
        errorLabel.setText("");
    }
}
