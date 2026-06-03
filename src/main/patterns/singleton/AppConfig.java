package main.patterns.singleton;

/**
 * Singleton Pattern (Creational) — Application Configuration Manager
 * 
 * Uygulama genelinde TEK BİR instance olarak var olan konfigürasyon yöneticisi.
 * Tüm sınıflar, tüm kullanıcılar, tüm paneller AYNI konfigürasyonu kullanır.
 * 
 * Bu GERÇEK bir GoF Singleton'dır çünkü:
 *   - Uygulama boyunca TEK instance vardır
 *   - Tüm kullanıcılar AYNI objeyi paylaşır
 *   - Global erişim sağlanır
 * 
 * ShoppingCart ise per-user scoped bir objedir ve Singleton DEĞİLDİR.
 * Çünkü her kullanıcının kendi ayrı sepeti vardır.
 * 
 * Singleton Yapısı:
 *   1. Private static instance
 *   2. Private constructor
 *   3. Public static getInstance() metodu
 * 
 * SOLID:
 *   - SRP: Sadece uygulama ayarlarını yönetir
 *   - OCP: Yeni ayar eklemek mevcut kodu bozmaz
 */
public class AppConfig {

    // 1. Private static instance — TEK instance
    private static AppConfig instance;

    // --- Uygulama Ayarları ---
    private String language;
    private String theme;
    private String currency;
    private String appName;
    private double defaultShippingCost;
    private int lowStockThreshold;
    private String dateFormat;
    private boolean notificationsEnabled;

    // 2. Private constructor — dışarıdan new AppConfig() YAPILAMAZ!
    private AppConfig() {
        // Default değerler
        language = "Türkçe";
        theme = "Dark";
        currency = "₺";
        appName = "E-Commerce System";
        defaultShippingCost = 29.99;
        lowStockThreshold = 5;
        dateFormat = "dd/MM/yyyy HH:mm";
        notificationsEnabled = true;
    }

    // 3. Public static method — TEK erişim noktası (thread-safe)
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    /**
     * Reset instance (yalnızca test amaçlı).
     */
    public static synchronized void resetInstance() {
        instance = null;
    }

    // --- Getters ---
    public String getLanguage() { return language; }
    public String getTheme() { return theme; }
    public String getCurrency() { return currency; }
    public String getAppName() { return appName; }
    public double getDefaultShippingCost() { return defaultShippingCost; }
    public int getLowStockThreshold() { return lowStockThreshold; }
    public String getDateFormat() { return dateFormat; }
    public boolean isNotificationsEnabled() { return notificationsEnabled; }

    // --- Setters ---
    public void setLanguage(String language) {
        this.language = language;
        System.out.println("[AppConfig] Dil değiştirildi: " + language);
    }

    public void setTheme(String theme) {
        this.theme = theme;
        System.out.println("[AppConfig] Tema değiştirildi: " + theme);
    }

    public void setCurrency(String currency) {
        this.currency = currency;
        System.out.println("[AppConfig] Para birimi değiştirildi: " + currency);
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setDefaultShippingCost(double defaultShippingCost) {
        this.defaultShippingCost = defaultShippingCost;
        System.out.println("[AppConfig] Varsayılan kargo: " + defaultShippingCost + currency);
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
        System.out.println("[AppConfig] Düşük stok eşiği: " + lowStockThreshold);
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
        System.out.println("[AppConfig] Bildirimler: " + (notificationsEnabled ? "Açık" : "Kapalı"));
    }

    /**
     * Fiyat formatlama — tüm uygulama bu metodu kullanır.
     */
    public String formatPrice(double price) {
        return String.format("%.2f%s", price, currency);
    }

    /**
     * Konfigürasyonu göster.
     */
    public void showConfig() {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║      APPLICATION CONFIGURATION     ║");
        System.out.println("╠════════════════════════════════════╣");
        System.out.println("║  Uygulama: " + padRight(appName, 23) + "║");
        System.out.println("║  Dil:      " + padRight(language, 23) + "║");
        System.out.println("║  Tema:     " + padRight(theme, 23) + "║");
        System.out.println("║  Para:     " + padRight(currency, 23) + "║");
        System.out.println("║  Kargo:    " + padRight(defaultShippingCost + currency, 23) + "║");
        System.out.println("║  Stok Eşik:" + padRight(String.valueOf(lowStockThreshold), 23) + "║");
        System.out.println("║  Tarih Fmt:" + padRight(dateFormat, 23) + "║");
        System.out.println("║  Bildirim: " + padRight(notificationsEnabled ? "Açık" : "Kapalı", 23) + "║");
        System.out.println("╚════════════════════════════════════╝");
    }

    private String padRight(String text, int length) {
        if (text.length() >= length) return text.substring(0, length);
        return text + " ".repeat(length - text.length());
    }

    @Override
    public String toString() {
        return "AppConfig{" +
                "language='" + language + '\'' +
                ", theme='" + theme + '\'' +
                ", currency='" + currency + '\'' +
                ", shipping=" + defaultShippingCost +
                ", lowStock=" + lowStockThreshold +
                '}';
    }
}
