package main.ui.panels;

import main.database.UserDatabase;
import main.models.Buyer;
import main.ui.utils.UIUtils;

import javax.swing.*;
import java.awt.*;

/**
 * Register panel - allows new buyers to create an account.
 * Seller registration is not available (mock data only).
 */
public class RegisterPanel extends JPanel {
    private final Runnable onBackToLogin;
    private final Runnable onRegisterSuccess;

    private JTextField nameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel errorLabel;

    public RegisterPanel(Runnable onBackToLogin, Runnable onRegisterSuccess) {
        this.onBackToLogin = onBackToLogin;
        this.onRegisterSuccess = onRegisterSuccess;

        setBackground(UIUtils.BG_COLOR);
        setLayout(new GridBagLayout());
        add(createRegisterCard());
    }

    private JPanel createRegisterCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UIUtils.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(40, 50, 40, 50)
        ));
        card.setPreferredSize(new Dimension(420, 520));

        // Title
        JLabel title = UIUtils.createLabel("📝 Kayıt Ol", UIUtils.TITLE_FONT, UIUtils.TEXT_PRIMARY);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = UIUtils.createLabel("Yeni alıcı hesabı oluştur", UIUtils.SMALL_FONT, UIUtils.TEXT_SECONDARY);
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        // Name
        JLabel nameLabel = UIUtils.createLabel("İsim:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);
        nameField = UIUtils.createStyledTextField(20);
        nameField.setMaximumSize(new Dimension(300, 38));
        nameField.setAlignmentX(CENTER_ALIGNMENT);

        // Email
        JLabel emailLabel = UIUtils.createLabel("E-posta:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        emailLabel.setAlignmentX(CENTER_ALIGNMENT);
        emailField = UIUtils.createStyledTextField(20);
        emailField.setMaximumSize(new Dimension(300, 38));
        emailField.setAlignmentX(CENTER_ALIGNMENT);

        // Password
        JLabel passLabel = UIUtils.createLabel("Şifre:", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        passLabel.setAlignmentX(CENTER_ALIGNMENT);
        passwordField = UIUtils.createStyledPasswordField(20);
        passwordField.setMaximumSize(new Dimension(300, 38));
        passwordField.setAlignmentX(CENTER_ALIGNMENT);

        // Confirm password
        JLabel confirmLabel = UIUtils.createLabel("Şifre (Tekrar):", UIUtils.BODY_FONT, UIUtils.TEXT_SECONDARY);
        confirmLabel.setAlignmentX(CENTER_ALIGNMENT);
        confirmField = UIUtils.createStyledPasswordField(20);
        confirmField.setMaximumSize(new Dimension(300, 38));
        confirmField.setAlignmentX(CENTER_ALIGNMENT);

        // Error
        errorLabel = UIUtils.createLabel("", UIUtils.SMALL_FONT, UIUtils.DANGER);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);

        // Register button
        JButton registerBtn = UIUtils.createStyledButton("✅ Kayıt Ol", UIUtils.SUCCESS, 300);
        registerBtn.setMaximumSize(new Dimension(300, 38));
        registerBtn.setAlignmentX(CENTER_ALIGNMENT);
        registerBtn.addActionListener(e -> performRegister());

        // Back link
        JButton backBtn = new JButton("← Giriş Yap sayfasına dön");
        backBtn.setFont(UIUtils.SMALL_FONT);
        backBtn.setForeground(UIUtils.ACCENT);
        backBtn.setBackground(UIUtils.CARD_BG);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setAlignmentX(CENTER_ALIGNMENT);
        backBtn.addActionListener(e -> onBackToLogin.run());

        // Layout
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));
        card.add(nameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(nameField);
        card.add(Box.createVerticalStrut(12));
        card.add(emailLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(emailField);
        card.add(Box.createVerticalStrut(12));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(12));
        card.add(confirmLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(confirmField);
        card.add(Box.createVerticalStrut(8));
        card.add(errorLabel);
        card.add(Box.createVerticalStrut(16));
        card.add(registerBtn);
        card.add(Box.createVerticalStrut(12));
        card.add(backBtn);

        return card;
    }

    private void performRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String confirm = new String(confirmField.getPassword()).trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Tüm alanları doldurun!");
            return;
        }

        if (!password.equals(confirm)) {
            errorLabel.setText("Şifreler eşleşmiyor!");
            return;
        }

        if (password.length() < 3) {
            errorLabel.setText("Şifre en az 3 karakter olmalı!");
            return;
        }

        Buyer buyer = UserDatabase.registerBuyer(name, email, password);
        if (buyer == null) {
            errorLabel.setText("Bu e-posta zaten kayıtlı!");
            return;
        }

        errorLabel.setText("");
        JOptionPane.showMessageDialog(this,
                "Hesabınız oluşturuldu! Giriş yapabilirsiniz.\nE-posta: " + email,
                "Kayıt Başarılı", JOptionPane.INFORMATION_MESSAGE);

        // Clear fields
        nameField.setText("");
        emailField.setText("");
        passwordField.setText("");
        confirmField.setText("");

        onRegisterSuccess.run();
    }
}
