# 🎓 E-Commerce Shopping System — Sunum Dokümanı
## Design Patterns & SOLID Principles in Practice

> **Proje:** Çoklu kullanıcı destekli lokal e-ticaret sistemi  
> **Dil:** Java 17 | **UI:** Java Swing | **Veri:** In-Memory  
> **Kullanılan Patternler:** Singleton, Decorator, Observer  
> **Tarih:** Haziran 2026

---

## 📑 İçindekiler

1. [Sistem Genel Bakış](#-1-sistem-genel-bakış)
2. [Singleton Pattern — AppConfig](#-2-singleton-pattern--appconfig)
3. [Decorator Pattern — Dinamik Fiyatlandırma](#-3-decorator-pattern--dinamik-fiyatlandırma)
4. [Observer Pattern — Gerçek Zamanlı Bildirimler](#-4-observer-pattern--gerçek-zamanlı-bildirimler)
5. [Arz & Talep Dinamik Fiyatlandırma Sistemi](#-5-arz--talep-dinamik-fiyatlandırma-sistemi)
6. [SOLID Prensipleri](#-6-solid-prensipleri)
7. [Kod Haritası](#-7-kod-haritası)
8. [Test Sonuçları](#-8-test-sonuçları)
9. [Çalıştırma & Demo](#-9-çalıştırma--demo)

---

## 🔍 1. Sistem Genel Bakış

```
┌─────────────────────────────────────────────────────────────┐
│                    E-Commerce System                         │
│                                                              │
│  ┌──────────┐    ┌──────────┐    ┌────────────────────────┐ │
│  │  SELLER   │    │  BUYER   │    │       GUEST            │ │
│  │ • Ürün    │    │ • Katalog│    │ • Read-only            │ │
│  │   Yönetimi│    │ • Sepet  │    │   göz atma             │ │
│  │ • İndirim │    │ • Kupon  │    │                        │ │
│  │ • Stok    │    │ • Ödeme  │    │                        │ │
│  │ • Analitik│    │          │    │                        │ │
│  └──────┬───┘    └────┬─────┘    └────────────────────────┘ │
│         │             │                                      │
│         ▼             ▼                                      │
│  ┌──────────────────────────────────────────────────────────┐│
│  │              DESIGN PATTERNS                              ││
│  │  🔹 Singleton: AppConfig (uygulama konfigürasyonu)       ││
│  │  🔹 Decorator: 5 tür dinamik fiyat/kargo dekoratörü      ││
│  │  🔹 Observer:  3 observer (Stok, Bildirim, Analitik)     ││
│  └──────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
```

---

## 🔒 2. Singleton Pattern — AppConfig

### 2.1 Pattern'in Amacı

> Singleton Pattern ensures that **only one object** of a class is created,
> this object is **globally accessible**, and all parts of the system use the **same instance**.

### 2.2 Neden Singleton Gerekli? — Problem Senaryosu

Uygulama ayarları (dil, tema, para birimi, kargo ücreti, stok eşiği) tüm sistem tarafından
paylaşılmalıdır. Her sınıf kendi konfigürasyon objesi yaratırsa **veri tutarsızlığı** oluşur:

```java
// ❌ BAD DESIGN — Singleton kullanılmazsa
class StockObserver {
    AppConfig config = new AppConfig();   // Yeni instance!
    int threshold = config.getLowStockThreshold();  // → 5
}

class ShoppingCart {
    AppConfig config = new AppConfig();   // FARKLI instance!
    double shipping = config.getDefaultShippingCost(); // → 29.99
}

class MainFrame {
    AppConfig config = new AppConfig();   // FARKLI instance!
    config.setLowStockThreshold(3);       // Bu değişiklik sadece burda geçerli!
    config.setDefaultShippingCost(19.99); // Diğerleri hala 29.99 kullanıyor!
}
```

**Sorun:** `MainFrame`'de threshold 3'e düşürüldü ama `StockObserver` hala 5 kullanıyor.
Her sınıf farklı AppConfig objesi yaratıyor → **veri tutarsızlığı** (inconsistency).

Bu, lab manual'deki AppConfig örneğinin **birebir aynısı**:
```
config1.setLanguage("Turkish");
config2.showConfig();  → Hala "English" gösterir!
```

### 2.3 Singleton ile Çözüm

```java
// ✅ GOOD DESIGN — Singleton Pattern
// Dosya: src/main/patterns/singleton/AppConfig.java

public class AppConfig {
    // 1. Private static instance — TEK instance
    private static AppConfig instance;

    // Uygulama ayarları
    private String language;
    private String theme;
    private String currency;
    private double defaultShippingCost;
    private int lowStockThreshold;
    private boolean notificationsEnabled;

    // 2. Private constructor — dışarıdan new AppConfig() YAPILAMAZ!
    private AppConfig() {
        language = "Türkçe";
        theme = "Dark";
        currency = "₺";
        defaultShippingCost = 29.99;
        lowStockThreshold = 5;
        notificationsEnabled = true;
    }

    // 3. Public static method — TEK erişim noktası (thread-safe)
    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }
}
```

**Şimdi TÜM sınıflar AYNI konfigürasyonu kullanır:**
```java
// MainFrame.java — Uygulama başlığı
AppConfig config = AppConfig.getInstance();
setTitle("🛒 " + config.getAppName());

// StockObserver.java — Düşük stok eşiği
int threshold = AppConfig.getInstance().getLowStockThreshold();  // → 5

// PriceNotificationObserver.java — Bildirim kontrolü
if (!AppConfig.getInstance().isNotificationsEnabled()) return;

// ShoppingCart.java — Kargo ücreti
return AppConfig.getInstance().getDefaultShippingCost();  // → 29.99₺

// → TÜMÜ AYNI obje! Biri değiştirdiğinde hepsi güncellenir ✅
```

### 2.4 UML Sınıf Diyagramı

```
+--------------------------------------------+
|              AppConfig                      |
+--------------------------------------------+
| - instance: AppConfig                      |  ← private static
| - language: String                         |
| - theme: String                            |
| - currency: String                         |
| - defaultShippingCost: double              |
| - lowStockThreshold: int                   |
| - notificationsEnabled: boolean            |
+--------------------------------------------+
| - AppConfig()                              |  ← private constructor
| + getInstance(): AppConfig                 |  ← public static (synchronized)
| + getLanguage(): String                    |
| + setLanguage(String): void                |
| + getTheme(): String                       |
| + setTheme(String): void                   |
| + getCurrency(): String                    |
| + getDefaultShippingCost(): double         |
| + getLowStockThreshold(): int              |
| + isNotificationsEnabled(): boolean        |
| + formatPrice(double): String              |
| + showConfig(): void                       |
+--------------------------------------------+
```

### 2.5 Kanıt: Aynı Instance Kontrolü (== operatörü)

Lab manual'deki `config1 == config2 → true` kontrolünün **birebir aynısı**:

```java
// TestScenarios.java — Test 1

AppConfig config1 = AppConfig.getInstance();
AppConfig config2 = AppConfig.getInstance();

// ✅ AYNI obje mi? (== referans karşılaştırması)
System.out.println(config1 == config2);  // → true ✅

// ✅ config1 üzerinden değişiklik → config2'de de görünür
config1.setLanguage("English");
config1.setTheme("Light");

config2.getLanguage();  // → "English" ✅ (Aynı obje!)
config2.getTheme();     // → "Light"   ✅ (Aynı obje!)

// ✅ Üçüncü referans da AYNI obje
AppConfig config3 = AppConfig.getInstance();
System.out.println(config1 == config3);  // → true ✅
config3.getLanguage();  // → "English" ✅
```

### 2.6 Thread-Safe Singleton

Lab manual'deki `synchronized` keyword'ünü kullandık:

```java
// synchronized → İki thread aynı anda yeni instance yaratamaz
public static synchronized AppConfig getInstance() {
    if (instance == null) {
        instance = new AppConfig();
    }
    return instance;
}
```

### 2.7 Singleton Kullanıldığı Dosyalar (Tüm Sistem Paylaşır)

| Dosya | `AppConfig.getInstance()` Kullanımı | Okunan Ayar |
|-------|-------------------------------------|-------------|
| `MainFrame.java` | Uygulama başlığı ve başlangıç konfigürasyonu | `getAppName()`, `showConfig()` |
| `StockObserver.java` | Düşük stok eşik değeri | `getLowStockThreshold()` |
| `PriceNotificationObserver.java` | Bildirim açık/kapalı + stok eşiği | `isNotificationsEnabled()`, `getLowStockThreshold()` |
| `ShoppingCart.java` | Varsayılan kargo ücreti | `getDefaultShippingCost()` |
| `TestScenarios.java` | Tüm ayarların test edilmesi | Tüm getter/setter'lar |

### 2.8 Avantajlar ve Dezavantajlar

| Avantaj | Dezavantaj |
|---------|------------|
| Tüm uygulama TEK konfigürasyon objesi kullanır | Global state oluşturur |
| Veri tutarsızlığı önlenir | Test yazmayı zorlaştırabilir (resetInstance() ile çözdük) |
| Gereksiz obje yaratımı engellenir | Sınıflar arası bağımlılıkları gizleyebilir |
| Ayar değişiklikleri anında tüm sisteme yansır | Çok thread'li ortamda dikkat gerekir (synchronized ile çözdük) |
| Konfigürasyon, loglama, DB bağlantısı için ideal | Singleton'ın aşırı kullanımı esnekliği azaltabilir |

### 2.9 Karşılaştırma Tablosu

| | Singleton OLMADAN | Singleton İLE |
|---|---|---|
| Obje sayısı | Her sınıf yeni AppConfig yaratır | Tüm uygulama TEK instance kullanır |
| Veri tutarlılığı | ❌ Tutarsız (farklı ayarlar) | ✅ Tutarlı (aynı ayarlar) |
| Obje kontrolü | ❌ Kontrolsüz (`new` ile sınırsız) | ✅ Kontrollü (`getInstance()`) |
| Global konfigürasyon | ❌ Her sınıf farklı değer görebilir | ✅ Tüm sınıflar aynı değeri görür |
| Memory | ❌ Gereksiz obje çoğalması | ✅ Tek obje, minimum bellek |

### 2.10 ShoppingCart Neden Singleton DEĞİL?

> ⚠️ **Önemli Ayrım:** ShoppingCart per-user scoped bir objedir, GoF Singleton DEĞİLDİR.

```
Singleton (AppConfig):          Per-User Scoped (ShoppingCart):
┌─────────────────┐             ┌──────────────────┐
│  TEK instance    │             │  Buyer A → Cart_A│
│  TÜM kullanıcılar│            │  Buyer B → Cart_B│
│  AYNI objeyi     │             │  Buyer C → Cart_C│
│  paylaşır        │             │  HER biri FARKLI │
└─────────────────┘             └──────────────────┘

config1 == config2 → true ✅    cart_A == cart_B → false ✅
(Gerçek Singleton)               (Per-user scope)
```

**Singleton'daki "bir tane"** → tüm sistem için tek instance.
**ShoppingCart'taki "bir tane"** → her kullanıcı için bir instance (session scoped).

---

## 🎨 3. Decorator Pattern — Dinamik Fiyatlandırma

### 3.1 Pattern'in Amacı (Lab Manual'den)

> The Decorator pattern attaches **additional responsibilities** to an object **dynamically**.
> Instead of creating many subclasses for every possible combination, a base object is **wrapped**
> by one or more decorator objects that share the same supertype.

### 3.2 Neden Decorator Gerekli? — Problem Senaryosu

E-ticaret sistemimizde ürünlere farklı indirim kombinasyonları uygulanabilir:
- %15 indirim
- 500₺ sabit indirim
- Flash sale %10
- Ücretsiz kargo
- Kupon kodu

**Decorator kullanılmazsa** (lab manual'deki "bad design" gibi):

```java
// ❌ BAD DESIGN — Flag-based yaklaşım (Lab manual'deki Beverage gibi)
class Product {
    String name;
    double price;
    boolean hasPercentDiscount;
    boolean hasFixedDiscount;
    boolean hasFlashSale;
    boolean hasFreeShipping;
    boolean hasCoupon;

    public double getPrice() {
        double total = price;
        if (hasPercentDiscount) total *= 0.85;     // %15
        if (hasFixedDiscount)   total -= 500;       // 500₺
        if (hasFlashSale)       total *= 0.90;      // %10
        if (hasCoupon)          total -= couponAmount;
        return total;
    }
}
```

**Bu tasarımın sorunları (Lab manual ile aynı):**

| Problem | Neden Kötü? |
|---------|-------------|
| **Sınıf patlaması (Class Explosion)** | Her yeni indirim türü Product sınıfını değiştirir |
| **Sık değişiklik** | Yeni indirim eklemek mevcut kodu değiştirir → **OCP ihlali** |
| **Zayıf genişleyebilirlik** | Bazı ürünlere uygulanmaması gereken indirimler tüm ürünlere eklenir |
| **Çift indirim desteği yok** | `hasFlashSale = true` → sadece bir kere uygulanabilir. İki kez flash sale? İmkansız |
| **Sıra kontrolü yok** | if-else ile sıra sabit → Decorator sırası matematiksel fark oluşturur |

### 3.3 Decorator ile Çözüm — Loose Coupling & Runtime Composition

Lab manual'deki Beverage-Condiment yapısını, projemizde **Product-Discount** yapısına uyarladık:

| Lab Manual (Coffee) | Projemiz (E-Commerce) |
|---------------------|----------------------|
| `Beverage` (abstract) | `ProductComponent` (interface) |
| `Espresso`, `DarkRoast` | `ConcreteProduct` (Product wrapper) |
| `CondimentDecorator` | `ProductDecorator` (abstract) |
| `Mocha`, `Soy`, `Whip` | `PercentageDiscount`, `FixedAmount`, `FlashSale`, `Coupon`, `FreeShipping` |

### 3.4 UML Yapısı

```
         ┌─────────────────────────┐
         │    «interface»          │
         │   ProductComponent      │
         │─────────────────────────│
         │ + getName(): String     │
         │ + getDescription(): Str │
         │ + getPrice(): double    │
         │ + hasFreeShipping(): bool│
         │ + getShippingCost(): dbl│
         └────────────┬────────────┘
                      │ implements
          ┌───────────┴───────────┐
          │                       │
  ┌───────┴────────┐   ┌─────────┴──────────┐
  │ConcreteProduct │   │ «abstract»         │
  │                │   │ ProductDecorator   │
  │ - product:     │   │───────────────────│
  │   Product      │   │ # wrappedProduct: │
  │                │   │   ProductComponent│
  │ (Base fiyat)   │   │                   │
  └────────────────┘   └────────┬──────────┘
                                │ extends
            ┌───────┬───────┬───┴────┬──────────┐
            │       │       │        │          │
        ┌───┴───┐┌──┴──┐┌──┴───┐┌───┴───┐┌─────┴─────┐
        │Percent││Fixed ││Flash ││Coupon ││FreeShip.  │
        │Disc.  ││Amount││Sale  ││Decor. ││Decorator  │
        │(%15)  ││(-500₺)│(⚡%10)│(-100₺)││(kargo=0)  │
        └───────┘└──────┘└──────┘└───────┘└───────────┘
```

### 3.5 Kod — Component (Interface)

```java
// Dosya: src/main/patterns/decorator/ProductComponent.java
public interface ProductComponent {
    String getName();
    String getDescription();
    double getPrice();
    boolean hasFreeShipping();
    double getShippingCost();
}
```

### 3.6 Kod — ConcreteComponent

```java
// Dosya: src/main/patterns/decorator/ConcreteProduct.java
public class ConcreteProduct implements ProductComponent {
    private final Product product;

    public ConcreteProduct(Product product) {
        this.product = product;
    }

    @Override
    public double getPrice() {
        return product.getPrice();  // Base fiyat
    }
    // ... getName(), getDescription() delegate eder
}
```

### 3.7 Kod — Abstract Decorator

```java
// Dosya: src/main/patterns/decorator/ProductDecorator.java
public abstract class ProductDecorator implements ProductComponent {
    protected final ProductComponent wrappedProduct;  // ← Component referansı!

    public ProductDecorator(ProductComponent product) {
        this.wrappedProduct = product;
    }

    @Override
    public double getPrice() {
        return wrappedProduct.getPrice();  // Varsayılan: delegate et
    }
    // ... Diğer metodlar da delegate eder
}
```

### 3.8 Kod — 5 Concrete Decorator

```java
// 1️⃣ PercentageDiscountDecorator.java
public class PercentageDiscountDecorator extends ProductDecorator {
    private final double discountPercentage;
    
    @Override
    public double getPrice() {
        return wrappedProduct.getPrice() * (1 - discountPercentage / 100.0);
    }
    @Override
    public String getDescription() {
        return wrappedProduct.getDescription() + " [%" + discountPercentage + " indirim]";
    }
}

// 2️⃣ FixedAmountDiscountDecorator.java
public class FixedAmountDiscountDecorator extends ProductDecorator {
    private final double discountAmount;
    
    @Override
    public double getPrice() {
        return Math.max(wrappedProduct.getPrice() - discountAmount, 0);  // 0 altına düşmez
    }
}

// 3️⃣ FlashSaleDecorator.java — Sabit %10 flash sale
public class FlashSaleDecorator extends ProductDecorator {
    @Override
    public double getPrice() {
        return wrappedProduct.getPrice() * 0.90;  // %10 indirim
    }
}

// 4️⃣ CouponDecorator.java — Kupon kodu bazlı
public class CouponDecorator extends ProductDecorator {
    private final double couponAmount;
    private final String couponCode;
    
    @Override
    public double getPrice() {
        return Math.max(wrappedProduct.getPrice() - couponAmount, 0);
    }
}

// 5️⃣ FreeShippingDecorator.java — Kargo ücretsiz
public class FreeShippingDecorator extends ProductDecorator {
    @Override
    public boolean hasFreeShipping() { return true; }
    @Override
    public double getShippingCost() { return 0; }
}
```

### 3.9 Decorator Zincirleme — Runtime Composition

Lab manual'deki `DarkRoast → Mocha → Mocha → Whip` zincirleme gibi:

```java
// Lab Manual:
Beverage order = new DarkRoast();
order = new Mocha(order);        // DarkRoast + Mocha
order = new Mocha(order);        // DarkRoast + Mocha + Mocha
order = new Whip(order);         // DarkRoast + Mocha + Mocha + Whip
System.out.println(order.cost()); // $1.49

// ✅ Projemiz (DiscountManagementPanel.java'da Seller tarafından):
ProductComponent laptop = new ConcreteProduct(laptopProduct);  // 10000₺
laptop = new PercentageDiscountDecorator(laptop, 15);          // 10000 × 0.85 = 8500₺
laptop = new FixedAmountDiscountDecorator(laptop, 500);        // 8500 - 500 = 8000₺
laptop = new FlashSaleDecorator(laptop);                       // 8000 × 0.90 = 7200₺
laptop = new FreeShippingDecorator(laptop);                    // 7200₺ + Ücretsiz Kargo

System.out.println(laptop.getPrice());        // 7200.0
System.out.println(laptop.hasFreeShipping()); // true
System.out.println(laptop.getDescription());
// "Gaming Laptop [%15 indirim] [-500₺ indirim] [⚡ Flash Sale %10] [Ücretsiz Kargo]"
```

### 3.10 🔑 Decorator Sırası MATEMATİKSEL FARK Oluşturur

Lab manual'de: *"The order of wrapping affects behavior and output."*

**Projemizde kanıtı (TestScenarios.java — Test 5):**

```
LAPTOP: 10000₺

SIRA 1: Önce %15 İndirim → Sonra 500₺ Sabit İndirim
  10000 × 0.85 = 8500₺
  8500 - 500   = 8000₺  ✅

SIRA 2: Önce 500₺ Sabit İndirim → Sonra %15 İndirim
  10000 - 500  = 9500₺
  9500 × 0.85  = 8075₺  ✅

FARK = 8075 - 8000 = 75₺ !!
→ AYNI dekoratörler, FARKLI sırada = FARKLI sonuç!
```

Lab manual'deki "double Mocha" gibi, projemizde de decorator'lar birden fazla kez uygulanabilir:
```java
// İki kez %10 indirim:
product = new PercentageDiscountDecorator(product, 10);  // ×0.90
product = new PercentageDiscountDecorator(product, 10);  // ×0.90
// 100₺ × 0.90 × 0.90 = 81₺ (toplam %19 indirim, %20 değil!)
```

### 3.11 İki Katmanlı Decorator Mimarisi

Projemizde decorator'lar iki katmanda uygulanır:

```
┌────────────────────────────────────────────────────────────────┐
│  SELLER KATMANI (Global — TÜM Buyer'lar Görür)                 │
│                                                                 │
│  Seller "İndirim Yönetimi" panelinden uygular:                 │
│  ProductDatabase.addSellerDecorator(productId, decorated)      │
│                                                                 │
│  Laptop → PercentageDiscountDecorator(%15) → 8500₺             │
│  Bu fiyatı TÜM BUYER'lar görür                                │
├────────────────────────────────────────────────────────────────┤
│  BUYER KATMANI (Per-User — SADECE kendi sepetinde)             │
│                                                                 │
│  Buyer sepetinde kupon uygular:                                │
│  CartItem.decoratedProduct'a yeni decorator sarılır            │
│                                                                 │
│  Ahmet: Laptop 8500₺ → CouponDecorator(-100₺) → 8400₺        │
│  Fatma: Laptop 8500₺ → (kupon yok) → 8500₺                   │
│                                                                 │
│  Farklı buyer, farklı sepet (Singleton), farklı decorator chain│
└────────────────────────────────────────────────────────────────┘
```

### 3.12 Decorator Kullanıldığı Dosyalar

| Dosya | Kullanım |
|-------|----------|
| `ProductDatabase.java` | `addSellerDecorator()` — Seller global indirim ekler |
| `DiscountManagementPanel.java` | Seller UI'da decorator ekleme/kaldırma butonları |
| `ShoppingCartPanel.java` | Buyer sepetinde %10 indirim ve free shipping butonları |
| `CouponValidator.java` | Kupon koduna göre uygun decorator oluşturur |
| `ProductCatalogPanel.java` | Decorated fiyatı gösterme (üstü çizili orijinal + indirimli fiyat) |
| `GuestBrowsePanel.java` | Misafir kullanıcıya da indirimli fiyat gösterilir |

### 3.13 Open-Closed Principle (OCP) Desteği

Lab manual'de: *"Follows the Open-Closed Principle well."*

```java
// ✅ Yeni bir decorator eklemek istesek → Mevcut kodu DEĞİŞTİRMEDEN sadece yeni sınıf:
public class BlackFridayDecorator extends ProductDecorator {
    public BlackFridayDecorator(ProductComponent product) {
        super(product);
    }
    @Override
    public double getPrice() {
        return wrappedProduct.getPrice() * 0.50;  // %50 Black Friday!
    }
    @Override
    public String getDescription() {
        return wrappedProduct.getDescription() + " [🖤 Black Friday %50]";
    }
}
// → ProductComponent değişmedi ✅
// → ConcreteProduct değişmedi ✅
// → Diğer decorator'lar değişmedi ✅
// → Sadece YENİ sınıf eklendi ✅
```

### 3.14 Avantajlar ve Dezavantajlar

| Avantaj | Dezavantaj |
|---------|------------|
| Inheritance yerine runtime composition → daha esnek | Çok sayıda küçük sınıf debugging'i zorlaştırabilir |
| Yeni indirim türleri mevcut kodu değiştirmeden eklenebilir (OCP) | Sarma sırası davranışı ve çıktıyı etkiler |
| Runtime'da farklı kombinasyonlar oluşturulabilir | Obje kimliği sezgisel değil (sarılmış obje ≠ dış decorator) |
| Çift/üçlü indirim doğal olarak desteklenir | Decorator'lar component interface'ini dikkatle korumalı |

### 3.15 Karşılaştırma Tablosu

| | Decorator OLMADAN (Flag-based) | Decorator İLE |
|---|---|---|
| Yeni indirim ekleme | ❌ Product sınıfını değiştir | ✅ Yeni decorator sınıfı ekle |
| Çift indirim | ❌ Desteklenemez (`boolean` flag) | ✅ İki kez sararak doğal |
| Sıra kontrolü | ❌ if-else sırası sabit | ✅ Sarma sırası esnek ve etkili |
| OCP uyumu | ❌ İhlal eder | ✅ Tam uyumlu |
| Genişleyebilirlik | ❌ Her ürün tüm flag'leri taşır | ✅ Sadece gerekli decorator sarılır |

---

## 👁️ 4. Observer Pattern — Gerçek Zamanlı Bildirimler

### 4.1 Pattern'in Amacı (Lab Manual'den)

> The Observer pattern defines a **one-to-many** relationship between objects.
> When the state of one object changes, all dependent objects are **notified and updated automatically**.
> The main benefit is **loose coupling**: the subject only knows that each listener
> implements the observer interface, not its concrete type.

### 4.2 Neden Observer Gerekli? — Problem Senaryosu (Tight Coupling)

Lab manual'deki **Non-Observer WeatherData** gibi, Observer kullanılmazsa:

```java
// ❌ BAD DESIGN — Direct Updates (Tight Coupling)
// StockManager doğrudan her UI panelini bilir ve çağırır:

class StockManager {
    private ProductCatalogPanel catalogPanel;       // Concrete dependency!
    private ShoppingCartPanel cartPanel;            // Concrete dependency!
    private NotificationPanel notifPanel;           // Concrete dependency!
    private SellerDashboard sellerDashboard;        // Concrete dependency!

    public StockManager(ProductCatalogPanel c, ShoppingCartPanel s,
                        NotificationPanel n, SellerDashboard d) {
        this.catalogPanel = c;
        this.cartPanel = s;
        this.notifPanel = n;
        this.sellerDashboard = d;
    }

    public void setStock(String product, int newStock) {
        // Her panel'i tek tek güncelle:
        catalogPanel.refreshProducts();
        cartPanel.refreshCart();
        notifPanel.addNotification("Stok değişti: " + product);
        sellerDashboard.refreshAnalytics();
    }
}
```

**Bu tasarımın sorunları (Lab manual ile aynı):**

| Problem (Lab Manual) | Projemizde Karşılığı |
|---------------------|---------------------|
| **Concrete dependency** — Subject her display sınıfını bilmeli | StockManager → ProductCatalogPanel, NotificationPanel vs. somut bağımlılık |
| **Modification for every new display** — Yeni display eklemek Subject'i değiştirir | AnalyticsPanel eklemek → StockManager'ı değiştirmek gerekir |
| **No runtime flexibility** — Observer'lar dinamik eklenemez/çıkarılamaz | Runtime'da yeni panel register edemezsin |
| **Poor maintainability** — Değişen kısım kapsüllenmemiş | UI değişiklikleri StockManager'ı bozabilir |

### 4.3 Observer ile Çözüm — Loose Coupling

Lab manual'deki **Observer Version** gibi, Subject sadece `Observer` interface'ini bilir:

```java
// ✅ GOOD DESIGN — Observer Pattern (Loose Coupling)

// Dosya: src/main/patterns/observer/Observer.java
public interface Observer {
    void update(String eventType, String productName, Object data);
}

// Dosya: src/main/patterns/observer/Subject.java
public interface Subject {
    void registerObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers(String eventType, String productName, Object data);
}
```

**Loose Coupling kanıtı:** `StockSubject` hiçbir somut observer sınıfını bilmez:

```java
// Dosya: src/main/patterns/observer/StockSubject.java
public class StockSubject implements Subject {
    private final List<Observer> observers;  // ← Observer INTERFACE tipi!
    // StockObserver, PriceNotificationObserver, AnalyticsObserver → BILMEZ!

    @Override
    public void notifyObservers(String eventType, String productName, Object data) {
        for (Observer observer : observers) {     // ← Interface üzerinden çağrı
            observer.update(eventType, productName, data);
        }
    }
}
```

### 4.4 UML Yapısı

```
    ┌──────────────────┐            ┌──────────────────┐
    │   «interface»    │   1    *   │   «interface»    │
    │    Subject       │───────────▶│    Observer       │
    │──────────────────│            │──────────────────│
    │+registerObserver()│           │+update(eventType,│
    │+removeObserver() │            │  productName,    │
    │+notifyObservers()│            │  data)           │
    └────────┬─────────┘            └────────┬─────────┘
             │ implements                     │ implements
             │                    ┌───────────┼───────────┐
    ┌────────┴──────────┐        │           │           │
    │   StockSubject    │   ┌────┴────┐ ┌────┴─────┐ ┌──┴──────────┐
    │──────────────────│   │ Stock   │ │ Price    │ │ Analytics  │
    │- observers: List │   │Observer │ │Notif.   │ │ Observer   │
    │- stockLevels: Map│   │         │ │Observer  │ │            │
    │- priceLevels: Map│   │STOCK_   │ │PRICE_   │ │ TÜM        │
    │                  │   │CHANGED  │ │CHANGED  │ │ OLAYLARI   │
    │+setStock()       │   │sadece   │ │DISCOUNT_│ │ LOGLAR     │
    │+setPrice()       │   │         │ │ADDED    │ │            │
    │+notifyDiscount() │   │         │ │STOCK_   │ │            │
    └──────────────────┘   └─────────┘ └─────────┘ └────────────┘
```

### 4.5 3 Concrete Observer

```java
// 1️⃣ StockObserver.java — Sadece STOCK_CHANGED olayını dinler
public class StockObserver implements Observer {
    @Override
    public void update(String eventType, String productName, Object data) {
        if (!"STOCK_CHANGED".equals(eventType)) return;  // Filtreleme
        int[] stockData = (int[]) data;
        if (stockData[1] == 0)
            log("⚠️ STOK TÜKENDI: " + productName);
        else if (stockData[1] <= 5)
            log("⚡ DÜŞÜK STOK: " + productName + " - Kalan: " + stockData[1]);
    }
}

// 2️⃣ PriceNotificationObserver.java — Fiyat, indirim ve kritik stok bildirir
public class PriceNotificationObserver implements Observer {
    @Override
    public void update(String eventType, String productName, Object data) {
        switch (eventType) {
            case "PRICE_CHANGED":   // "🔴 Laptop fiyatı düştü!"
            case "DISCOUNT_ADDED":  // "🎉 Laptop indirime girdi!"
            case "STOCK_CHANGED":   // "🟡 Laptop stok azaldı!" (sadece kritik)
        }
        // → NotificationPanel'e iletir (UI callback)
    }
}

// 3️⃣ AnalyticsObserver.java — TÜM olayları loglar
public class AnalyticsObserver implements Observer {
    @Override
    public void update(String eventType, String productName, Object data) {
        // HER olay kaydedilir — filtreleme yok
        entries.add(new AnalyticsEntry(eventType, productName, details));
        // → Seller Analytics panelinde gösterilir
    }
}
```

### 4.6 Observer Registration (Lab Manual formatında)

```java
// Dosya: src/main/ui/MainFrame.java (satır ~42-55)

// Subject oluştur
StockSubject stockSubject = new StockSubject();

// Observer'ları oluştur
StockObserver stockObserver = new StockObserver();
PriceNotificationObserver priceNotifObserver = new PriceNotificationObserver();
AnalyticsObserver analyticsObserver = new AnalyticsObserver();

// Observer'ları REGISTER et (Lab manual: registerObserver)
stockSubject.registerObserver(stockObserver);
stockSubject.registerObserver(priceNotifObserver);
stockSubject.registerObserver(analyticsObserver);
```

### 4.7 Olay Akış Örneği (Lab Manual "Run" formatında)

```
SELLER "Laptop"a %15 indirim ekliyor:

1. DiscountManagementPanel → applyDecorator("PERCENTAGE")
2. stockSubject.setPrice("Laptop", 8500.0)
3. stockSubject.notifyDiscountAdded("Laptop", "%15 indirim")
   │
   ├──▶ StockObserver: (PRICE_CHANGED filtrelenir → atlar)
   │
   ├──▶ PriceNotificationObserver:
   │     "[17:14:11] 🔴 Laptop fiyatı düştü! 10000₺ → 8500₺"
   │     "[17:14:11] 🎉 Laptop indirime girdi! %15 indirim"
   │     → BuyerDashboard NotificationPanel'de gösterilir
   │
   └──▶ AnalyticsObserver:
         "[17:14:11] [PRICE_CHANGED] Laptop: Fiyat: 10000₺ → 8500₺"
         "[17:14:11] [DISCOUNT_ADDED] Laptop: İndirim eklendi: %15"
         → SellerDashboard Analytics panelinde gösterilir
```

### 4.8 Runtime Register/Remove (Lab Manual: Dynamic Registration)

Lab manual'de: *"Can a display be removed at runtime without changing WeatherData?"*

```java
// Lab Manual:
weatherData.removeObserver(statistics);
System.out.println("--- StatisticsDisplay removed ---");
weatherData.setMeasurements(25, 50, 1011);  // statistics artık bildirim almaz

// ✅ Projemiz (TestScenarios.java — Test 3.8):
int logSizeBefore = stockObs.getStockLog().size();
subject.removeObserver(stockObs);                // StockObserver çıkarıldı
subject.setStock("Laptop", 20);                  // Stok değişti
assert stockObs.getStockLog().size() == logSizeBefore;  // ✅ Bildirim almadı!
// → StockSubject hiç değişmedi, sadece observer listesinden çıkarıldı
```

### 4.9 Loose Coupling Kanıtı

| Soru (Lab Manual) | Cevap (Projemiz) |
|---|---|
| Subject hangi somut observer sınıflarını biliyor? | **Hiçbirini!** Sadece `Observer` interface'ini biliyor |
| Yeni observer eklemek için Subject değişmeli mi? | **Hayır!** Sadece `registerObserver()` çağır |
| Observer runtime'da eklenip çıkarılabilir mi? | **Evet!** `registerObserver()` ve `removeObserver()` |
| Subject ve Observer bağımsız değiştirilebilir mi? | **Evet!** Interface üzerinden loose coupling |

**Lab Manual terimiyle:** StockSubject → Observer interface'e bağımlı, somut StockObserver/PriceNotificationObserver/AnalyticsObserver sınıflarına **DEĞİL**.

### 4.10 Avantajlar ve Dezavantajlar

| Avantaj | Dezavantaj |
|---------|------------|
| **Loose coupling** — Subject somut observer'ları bilmez | Bildirim sırası garanti edilmemeli |
| **Kolay genişleme** — Yeni observer mevcut kodu değiştirmez | Çok fazla observer debugging'i zorlaştırabilir |
| **Runtime register/remove** — Dinamik ekleme/çıkarma | Unsubscribe edilmeyen observer memory leak yapabilir |
| **Event-driven sistemlere uygun** — Stok, fiyat, bildirim | Push modeli bazı observer'lara fazla veri gönderebilir |

### 4.11 Karşılaştırma Tablosu (Lab Manual formatında)

| | Observer OLMADAN (Direct Updates) | Observer İLE |
|---|---|---|
| **Bağımlılık** | ❌ Tight coupling — Subject her panel'i bilir | ✅ Loose coupling — Sadece interface bilir |
| **Yeni panel ekleme** | ❌ Subject sınıfı değişmeli | ✅ Sadece `registerObserver()` çağır |
| **Runtime esneklik** | ❌ Panel eklenemez/çıkarılamaz | ✅ `registerObserver()` / `removeObserver()` |
| **Bakım** | ❌ Değişen kısım kapsüllenmemiş | ✅ Her observer kendi sorumluluğuna sahip |
| **Ölçeklenebilirlik** | ❌ Her yeni panel Subject'i büyütür | ✅ Sınırsız observer eklenebilir |

---

## 💰 5. Arz & Talep Dinamik Fiyatlandırma Sistemi

Dinamik fiyatlandırma, **Decorator + Observer** pattern'lerinin birlikte çalışmasıyla gerçekleşir:

### 5.1 Arz-Talep Döngüsü

```
┌──────────────────────────────────────────────────────────────┐
│                    ARZ-TALEP DÖNGÜSÜ                         │
│                                                               │
│  1. TALEP ARTIYOR (Buyer'lar satın alıyor)                   │
│     ├── stockSubject.decreaseStock("Laptop", 1)              │
│     ├── Observer tetiklenir → "STOCK_CHANGED"                │
│     ├── StockObserver: "⚡ DÜŞÜK STOK: 3 adet!"             │
│     └── PriceNotificationObserver: "🟡 Stok azaldı"         │
│                                                               │
│  2. SELLER ANALİTİĞİ GÖRÜR (Observer → AnalyticsObserver)   │
│     ├── "Laptop: 7 adet satıldı, 3 adet kaldı"             │
│     ├── AnalyticsObserver log'unda tüm geçmiş               │
│     └── Karar: Stok az → Fiyat artır VEYA indirim kaldır    │
│                                                               │
│  3. SELLER FİYAT DEĞİŞTİRİR (Decorator ile)                │
│     ├── clearSellerDecorators() → İndirimleri kaldır         │
│     │   VEYA                                                  │
│     ├── addSellerDecorator(FlashSale) → Stok eritme          │
│     ├── Observer tetiklenir → "PRICE_CHANGED"                │
│     └── Tüm buyer'lar yeni fiyatı görür                     │
│                                                               │
│  4. BUYER'LAR TEPKİ VERİR                                    │
│     ├── Bildirim paneli: "🔴 Laptop fiyatı değişti!"        │
│     ├── Katalogda yeni fiyat gösterilir                      │
│     └── Alım kararını etkiler → Döngü devam eder            │
└──────────────────────────────────────────────────────────────┘
```

### 5.2 Somut Senaryo

```
BAŞLANGIÇ: Laptop 10000₺, Stok: 10, %15 indirimli → 8500₺

── TALEP ARTIYOR ──
Buyer1 alıyor → Stok: 9  (Observer: "Stok azaldı")
Buyer2 alıyor → Stok: 8
...
Buyer7 alıyor → Stok: 3  ← DÜŞÜK STOK UYARISI! (Observer tetiklenir)

── SELLER KARAR VERİYOR ──
AnalyticsObserver'dan görüyor: "7 adet satıldı, talep yüksek"
StockObserver: "⚡ DÜŞÜK STOK: Laptop - Kalan: 3 adet"

Seçenek A → Arz az, talep yüksek = Fiyat ARTIŞ:
  Seller indirimleri kaldırır → clearSellerDecorators()
  Fiyat: 8500₺ → 10000₺ (stok az → fiyat yüksek)
  Observer: "Laptop indirimleri kaldırıldı"
  Observer: "🔴 Laptop fiyatı arttı! 8500₺ → 10000₺"

Seçenek B → Stok eritme stratejisi = Fiyat DÜŞÜŞ:
  Seller FlashSale ekler → addSellerDecorator(FlashSale)
  Fiyat: 8500₺ × 0.90 = 7650₺
  Observer: "🎉 Laptop Flash Sale başladı!"
```

### 5.3 Pattern'lerin Rolü

| Sistem Parçası | Pattern | Rolü |
|----------------|---------|------|
| **Stok izleme** | Observer | StockObserver düşük stok/tükendi uyarıları |
| **Fiyat değişikliği** | Decorator | Seller decorator ekler/kaldırır → fiyat değişir |
| **Bildirim** | Observer | PriceNotificationObserver buyer'lara bildirir |
| **Analitik** | Observer | AnalyticsObserver tüm olayları loglar |
| **Buyer'a yansıma** | Observer + Decorator | Katalogda indirimli fiyat + bildirim paneli |

---

## 🏗️ 6. SOLID Prensipleri

### S — Single Responsibility Principle (Tek Sorumluluk)

> Her sınıf yalnızca bir sorumluluğa sahiptir.

| Sınıf | Tek Sorumluluğu |
|-------|----------------|
| `ShoppingCart` | Sepet item yönetimi (ekleme, çıkarma, toplam) |
| `StockSubject` | Stok/fiyat durumu + observer bilgilendirme |
| `StockObserver` | Sadece stok değişikliklerini izleme ve loglama |
| `PriceNotificationObserver` | Sadece kullanıcı bildirimlerini oluşturma |
| `AnalyticsObserver` | Sadece tüm olayları zaman damgasıyla loglama |
| `CouponValidator` | Sadece kupon kodlarını doğrulama |
| `SessionManager` | Sadece login/logout durumu yönetme |
| `ProductDatabase` | Sadece ürün verileri ve seller decorator'ları |
| `UserDatabase` | Sadece kullanıcı verileri ve kimlik doğrulama |
| `OrderDatabase` | Sadece sipariş kayıtları |

### O — Open/Closed Principle (Genişlemeye Açık, Değişikliğe Kapalı)

```java
// ✅ Yeni decorator → Mevcut kod DEĞİŞMEZ:
public class LoyaltyDiscountDecorator extends ProductDecorator { ... }
// → ProductComponent, ConcreteProduct, diğer decorator'lar → HİÇBİRİ değişmez

// ✅ Yeni observer → Mevcut kod DEĞİŞMEZ:
public class SMSNotificationObserver implements Observer { ... }
// → StockSubject, diğer observer'lar → HİÇBİRİ değişmez
```

### L — Liskov Substitution Principle (Yerine Koyma)

```java
// ✅ Her ProductComponent implementasyonu yerine konabilir:
ProductComponent product = new ConcreteProduct(laptop);          // ✅
product = new PercentageDiscountDecorator(product, 15);          // ✅
product = new FreeShippingDecorator(product);                    // ✅
// Her adımda product.getPrice(), product.getName() çalışır

// ✅ Her Observer implementasyonu yerine konabilir:
Observer obs = new StockObserver();               // ✅
obs = new PriceNotificationObserver();            // ✅
obs = new AnalyticsObserver();                    // ✅
// Her durumda obs.update() çalışır
```

### I — Interface Segregation Principle (Arayüz Ayrımı)

```java
// ✅ Minimal, odaklı interface'ler:
public interface ProductComponent { ... }  // 5 metot
public interface Observer { ... }          // 1 metot (update)
public interface Subject { ... }          // 3 metot (register, remove, notify)
public interface CartChangeListener { ... } // 1 metot (onCartChanged)
```

### D — Dependency Inversion Principle (Bağımlılık Tersine Çevirme)

```java
// ✅ StockSubject → Observer INTERFACE'e bağımlı (somut sınıflara DEĞİL)
private final List<Observer> observers;  // ← Interface tipi

// ✅ ProductDecorator → ProductComponent INTERFACE'e bağımlı
protected final ProductComponent wrappedProduct;  // ← Interface tipi

// ✅ ShoppingCart.addItem() → ProductComponent INTERFACE'e bağımlı
public void addItem(ProductComponent decorated, Product original) { ... }
// Hangi decorator chain olduğunu bilmesine GEREK YOK
```

---

## 🗺️ 7. Kod Haritası

```
src/
├── main/
│   ├── Main.java                         ← Giriş noktası
│   ├── models/
│   │   ├── User.java                     ← Abstract base [SRP, LSP]
│   │   ├── Seller.java                   ← extends User [LSP, OCP]
│   │   ├── Buyer.java                    ← extends User [LSP, OCP]
│   │   ├── Product.java                  ← Ürün modeli [SRP]
│   │   └── CartItem.java                 ← Sepet item [SRP]
│   ├── database/
│   │   ├── UserDatabase.java             ← 5 seller + 6 buyer [SRP]
│   │   ├── ProductDatabase.java          ← 26 ürün + decorator [SRP]
│   │   └── OrderDatabase.java            ← Sipariş kayıtları [SRP]
│   ├── patterns/
│   │   ├── singleton/
│   │   │   ├── AppConfig.java            ← SINGLETON [GoF — tek instance]
│   │   │   └── ShoppingCart.java         ← Per-user scoped sepet
│   │   ├── decorator/
│   │   │   ├── ProductComponent.java     ← Interface [ISP, DIP]
│   │   │   ├── ConcreteProduct.java      ← Base component
│   │   │   ├── ProductDecorator.java     ← Abstract decorator [DIP]
│   │   │   ├── PercentageDiscountDecorator.java  ← %indirim
│   │   │   ├── FixedAmountDiscountDecorator.java ← ₺indirim
│   │   │   ├── CouponDecorator.java              ← Kupon
│   │   │   ├── FreeShippingDecorator.java        ← Kargo
│   │   │   └── FlashSaleDecorator.java           ← ⚡Flash
│   │   └── observer/
│   │       ├── Observer.java             ← Interface [ISP, DIP]
│   │       ├── Subject.java              ← Interface [ISP]
│   │       ├── StockSubject.java         ← Concrete Subject
│   │       ├── StockObserver.java        ← Stok izleme
│   │       ├── PriceNotificationObserver.java  ← Bildirim
│   │       └── AnalyticsObserver.java    ← Analitik log
│   ├── utils/
│   │   ├── SessionManager.java           ← Login/logout [SRP]
│   │   └── CouponValidator.java          ← Kupon doğrulama [SRP]
│   └── ui/
│       ├── MainFrame.java                ← CardLayout
│       ├── utils/UIUtils.java            ← Renk, font, factory
│       └── panels/ (9 panel)
└── test/
    └── TestScenarios.java                ← 60 test senaryosu
```

---

## ✅ 8. Test Sonuçları

### Sonuç: **75/75 PASS ✅**

| Test Grubu | Sayı | Test Edilen |
|------------|------|-------------|
| **Test 1: Singleton (AppConfig)** | 17 | `==` kontrolü, default değerler, config1→config2 paylaşım, formatPrice, notifications |
| **Test 1B: ShoppingCart (Per-User)** | 7 | Aynı buyer=aynı sepet, farklı buyer=farklı sepet, clear/reset |
| **Test 2: Decorator** | 19 | 5 decorator türü, zincir, fiyat≥0, çoklu indirim, description |
| **Test 3: Observer** | 18 | 3 observer, stok uyarıları, fiyat bildirim, register/remove |
| **Test 4: Entegrasyon** | 7 | Sepet+Stok uyumu, CouponValidator, User rolleri |
| **Test 5: Decorator Sırası** | 7 | Matematiksel fark kanıtı (75₺), kompleks zincir |

---

## 🚀 9. Çalıştırma & Demo

```bash
# Derleme
cd /path/to/java-design-pattern-project
find src -name "*.java" > sources.txt
javac -d bin @sources.txt

# Test
java -cp bin test.TestScenarios

# Uygulama
java -cp bin main.Main
```

### Demo Akışı

1. **Seller Login** → `seller1@test.com / 123`
   - İndirim Yönetimi → Laptop'a %15 ekle → **Decorator Pattern** göster
   - Observer tetiklenir → Analitik log'da görünür → **Observer Pattern** göster

2. **Buyer Login** → `ahmet@test.com / 123`
   - Katalogda indirimli fiyat → **Decorator** sonucu
   - Bildirim paneli → **Observer** sonucu
   - Sepete ekle → Kupon uygula (WELCOME10) → **Decorator zincirleme**
   - **Singleton**: Aynı sepet her panelden erişilebilir

3. **Başka Buyer Login** → `fatma@test.com / 123`
   - Aynı indirimli fiyat (seller global **Decorator**)
   - Ama farklı sepet → **Singleton per-user** kanıtı

### Test Hesapları

| Rol | E-posta | Şifre |
|-----|---------|-------|
| Buyer | `ahmet@test.com` | `123` |
| Buyer | `fatma@test.com` | `123` |
| Seller | `seller1@test.com` | `123` |
| Seller | `seller3@test.com` | `123` |
