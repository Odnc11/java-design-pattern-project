# 🛒 E-Commerce Shopping System — Design Patterns Project

**Course:** SEN3006 - Software Architecture  
**Semester:** Spring 2026  
**Team Members:** Tamer Oduncu, Sude Nur Şekerci, Robert Bora Orhan  
**Submission Deadline:** June 05, 2026 — 23:59

---

## 📋 Project Overview

A local single-store e-commerce shopping system implemented in Java (Java 17), demonstrating the practical application of **3 Design Patterns** from the GoF catalog:

| # | Pattern | Category | Core Class |
|---|---------|----------|------------|
| 1 | **Singleton** | Creational | `AppConfig` |
| 2 | **Decorator** | Structural | `ProductComponent` → `ProductDecorator` |
| 3 | **Observer** | Behavioral | `Subject` → `StockSubject` |

The system includes a full Java Swing GUI with buyer, seller, and guest flows, plus 80 automated test scenarios.

---

## 🎯 Problem Definition

An e-commerce platform needs to solve the following challenges:

- Users (buyers, sellers, guests) need different views and capabilities.
- Multiple discount types must be stackable at runtime without combinatorial class explosion.
- Stock changes must propagate automatically to pricing, analytics, and UI notifications — without tight coupling.
- Application-wide configuration (shipping cost, stock thresholds, currency) must be consistent and centrally managed.

---

## 🧩 Design Patterns — Accurate Implementation Details

### 1. ✅ Singleton Pattern (Creational)

**Implemented in:** [`AppConfig.java`](src/main/patterns/singleton/AppConfig.java)

**GoF Structure:**
- `private static AppConfig instance` — single static reference
- `private AppConfig()` — prevents external instantiation
- `public static synchronized AppConfig getInstance()` — thread-safe lazy initialization

**What it manages:**
- `language`, `theme`, `currency` — locale settings
- `defaultShippingCost` — used by `ShoppingCart.getShippingCost()`
- `lowStockThreshold` — used by `StockObserver`, `DemandPricingObserver`, `PriceNotificationObserver`
- `notificationsEnabled` — checked by `PriceNotificationObserver` before firing

**Cross-cutting usage:** `AppConfig.getInstance()` is called from `StockObserver`, `DemandPricingObserver`, `PriceNotificationObserver`, and `ShoppingCart` — demonstrating true global shared state.

> ⚠️ **Important clarification:**
> - `AppConfig` → **TRUE GoF Singleton** (single app-wide instance)
> - `ShoppingCart` → **per-user scoped object** (one instance per buyer ID, via `ConcurrentHashMap`). The class javadoc explicitly states: *"NOT a GoF Singleton Pattern!"*
> - `SessionManager` → **static utility class** (non-instantiable via `private SessionManager()`), not a Singleton.

---

### 2. ✅ Decorator Pattern (Structural)

**Implemented in:** `src/main/patterns/decorator/`

**GoF Roles:**

| Role | Class |
|------|-------|
| Component (interface) | [`ProductComponent`](src/main/patterns/decorator/ProductComponent.java) |
| Concrete Component (leaf) | [`ConcreteProduct`](src/main/patterns/decorator/ConcreteProduct.java) |
| Abstract Decorator | [`ProductDecorator`](src/main/patterns/decorator/ProductDecorator.java) |
| Concrete Decorators | 6 classes below |

**`ProductComponent` interface exposes:**
```java
String getName();
String getDescription();
double getPrice();
boolean hasFreeShipping();
double getShippingCost();
```

**`ProductDecorator`** holds a `protected final ProductComponent wrappedProduct` and delegates all calls — concrete decorators only override what they change.

**Concrete Decorators:**

| Class | Behavior |
|-------|----------|
| `PercentageDiscountDecorator` | `price * (1 - discount/100)` — validated 0–100% |
| `FixedAmountDiscountDecorator` | `price - fixedAmount`, floored at 0 |
| `CouponDecorator` | Fixed ₺ off via coupon code, floored at 0 |
| `FreeShippingDecorator` | Sets `hasFreeShipping()` → `true`, `getShippingCost()` → `0` |
| `FlashSaleDecorator` | Hardcoded `−10%` flash discount |
| `DynamicPricingDecorator` | Queries `DemandPricingObserver.getModifier(name)` at runtime for +20%/−10% |

**Stacking example:**
```text
ConcreteProduct (10,000₺)
└── DynamicPricingDecorator (+20% demand surge) → 12,000₺
    └── PercentageDiscountDecorator (10%) → 10,800₺
        └── FreeShippingDecorator → 10,800₺ + 0₺ shipping
```

> **Order matters.** Applying `PercentageDiscount` before `FixedAmount` gives a different result than the reverse — demonstrated in `TestScenarios.java`.

---

### 3. ✅ Observer Pattern (Behavioral)

**Implemented in:** `src/main/patterns/observer/`

**GoF Roles:**

| Role | Class |
|------|-------|
| Subject (interface) | [`Subject`](src/main/patterns/observer/Subject.java) |
| Concrete Subject | [`StockSubject`](src/main/patterns/observer/StockSubject.java) |
| Observer (interface) | [`Observer`](src/main/patterns/observer/Observer.java) |
| Concrete Observers | 4 classes below |

**`Observer` interface:**
```java
void update(String eventType, String productName, Object data);
```

**`Subject` interface:**
```java
void registerObserver(Observer observer);
void removeObserver(Observer observer);
void notifyObservers(String eventType, String productName, Object data);
```

**`StockSubject` manages:**
- Per-product stock levels (`Map<String, Integer> stockLevels`)
- Per-product base prices (`Map<String, Double> priceLevels`)
- Observer list — notified on `setStock()`, `setPrice()`, `notifyDiscountAdded()`, `notifyDiscountRemoved()`

**Supported event types:**

| Event | Trigger | Data payload |
|-------|---------|--------------|
| `STOCK_CHANGED` | `setStock()` / `decreaseStock()` / `increaseStock()` | `int[] {oldStock, newStock}` |
| `PRICE_CHANGED` | `setPrice()` | `double[] {oldPrice, newPrice}` |
| `DISCOUNT_ADDED` | `notifyDiscountAdded()` | `String` description |
| `DISCOUNT_REMOVED` | `notifyDiscountRemoved()` | `null` |

**Concrete Observers:**

| Observer | Listens To | Action |
|----------|-----------|--------|
| `StockObserver` | `STOCK_CHANGED` | Logs to internal list + console; warns on low/zero stock using `AppConfig.lowStockThreshold` |
| `DemandPricingObserver` | `STOCK_CHANGED` | Updates static `priceModifiers` map: `≤5 → +20%`, `≥20 → −10%`, else `0%` |
| `PriceNotificationObserver` | `PRICE_CHANGED`, `DISCOUNT_ADDED`, `DISCOUNT_REMOVED`, `STOCK_CHANGED` | Creates user-facing notification strings, calls registered `NotificationListener` callbacks for UI |
| `AnalyticsObserver` | ALL events | Records every event as `AnalyticsEntry` (timestamp, type, product, details) for Seller Dashboard |

**Observer flow on checkout / stock change:**
```text
Buyer checks out → product stock decreases
         │
         ▼
StockSubject.decreaseStock("iPhone 15", 1)
         │
         └─► setStock("iPhone 15", newStock)
                   │
                   ▼
         notifyObservers("STOCK_CHANGED", "iPhone 15", [oldStock, newStock])
                   │
         ┌─────────┼───────────────┬──────────────────────┐
         ▼         ▼               ▼                      ▼
  StockObserver  DemandPricing  PriceNotification    Analytics
  (logs warning)  Observer       Observer             Observer
                 (sets +20%     (shows UI toast       (records entry
                  if stock ≤5)   if stock ≤5)         for dashboard)
```

**Bridge between Observer and Decorator:**  
`DemandPricingDecorator.getPrice()` calls `DemandPricingObserver.getModifier(productName)` at render time — so the Decorator reads the Observer's computed state dynamically. No direct coupling between the two pattern implementations.

---

## 🔥 Supply & Demand — Dynamic Pricing System

| Stock Level | Condition | Effect |
|-------------|-----------|--------|
| High Demand | `newStock ≤ lowStockThreshold` (default: 5) and `> 0` | `+20%` price surge via `DynamicPricingDecorator` |
| Overstock | `newStock ≥ 20` | `−10%` clearance via `DynamicPricingDecorator` |
| Normal | 6–19 items | `0%` modifier |
| Out of stock | `newStock == 0` | UI shows "Tükendi" — no price modifier |

The threshold (`5`) is read from `AppConfig.getInstance().getLowStockThreshold()` — changing the Singleton config immediately affects all observers without code changes.

---

## 🏗️ System Architecture

```text
User (Buyer / Seller / Guest)
│
├── SessionManager (static utility) — tracks logged-in user (login/logout)
│
├── AppConfig (SINGLETON) — global settings: threshold, shipping cost, currency
│
├── ShoppingCart (per-user scoped)
│     │   ShoppingCart.getInstance(buyerId) — one cart per buyer
│     └── List<CartItem>
│           └── Decorated ProductComponents
│                 (PercentageDiscount, FixedAmount, Coupon, FreeShipping, FlashSale, DynamicPricing)
│
├── ProductDatabase — stores products + each product's StockSubject instance
├── UserDatabase    — stores buyers and sellers
├── OrderDatabase   — stores completed orders
│
└── StockSubject (one per product, created by ProductDatabase)
      └── Registered Observers:
            ├── StockObserver           (stock logging)
            ├── DemandPricingObserver   (price modifier calculation)
            ├── PriceNotificationObserver (UI toasts)
            └── AnalyticsObserver       (seller dashboard log)
```

---

## 📁 Project Structure

```
java-design-pattern-project/
├── src/
│   ├── main/
│   │   ├── Main.java                                # Entry point — launches Swing UI
│   │   ├── models/
│   │   │   ├── User.java                            # Base user class
│   │   │   ├── Buyer.java                           # Buyer (has freeShippingThreshold)
│   │   │   ├── Seller.java                          # Seller (manages products)
│   │   │   ├── Product.java                         # Product data model
│   │   │   └── CartItem.java                        # Cart item (product + quantity)
│   │   ├── database/
│   │   │   ├── ProductDatabase.java                 # In-memory store; creates StockSubject per product
│   │   │   ├── UserDatabase.java                    # In-memory buyer/seller store
│   │   │   └── OrderDatabase.java                   # Order history store
│   │   ├── patterns/
│   │   │   ├── singleton/
│   │   │   │   ├── AppConfig.java                   # ✅ TRUE GoF Singleton
│   │   │   │   └── ShoppingCart.java                # Per-user scoped (NOT Singleton)
│   │   │   ├── decorator/
│   │   │   │   ├── ProductComponent.java            # ✅ Component interface
│   │   │   │   ├── ProductDecorator.java            # ✅ Abstract Decorator
│   │   │   │   ├── ConcreteProduct.java             # ✅ Concrete Component (leaf)
│   │   │   │   ├── PercentageDiscountDecorator.java # ✅ Concrete Decorator
│   │   │   │   ├── FixedAmountDiscountDecorator.java# ✅ Concrete Decorator
│   │   │   │   ├── CouponDecorator.java             # ✅ Concrete Decorator
│   │   │   │   ├── FreeShippingDecorator.java       # ✅ Concrete Decorator
│   │   │   │   ├── FlashSaleDecorator.java          # ✅ Concrete Decorator
│   │   │   │   └── DynamicPricingDecorator.java     # ✅ Concrete Decorator (bridges to Observer)
│   │   │   └── observer/
│   │   │       ├── Subject.java                     # ✅ Subject interface
│   │   │       ├── Observer.java                    # ✅ Observer interface
│   │   │       ├── StockSubject.java                # ✅ Concrete Subject
│   │   │       ├── StockObserver.java               # ✅ Concrete Observer
│   │   │       ├── DemandPricingObserver.java       # ✅ Concrete Observer
│   │   │       ├── PriceNotificationObserver.java   # ✅ Concrete Observer
│   │   │       └── AnalyticsObserver.java           # ✅ Concrete Observer
│   │   ├── ui/
│   │   │   ├── MainFrame.java                       # Root Swing window (tab/panel manager)
│   │   │   ├── panels/
│   │   │   │   ├── LoginPanel.java
│   │   │   │   ├── RegisterPanel.java
│   │   │   │   ├── GuestBrowsePanel.java
│   │   │   │   ├── BuyerDashboard.java
│   │   │   │   ├── SellerDashboard.java
│   │   │   │   ├── ProductCatalogPanel.java
│   │   │   │   ├── ShoppingCartPanel.java
│   │   │   │   ├── NotificationPanel.java
│   │   │   │   └── DiscountManagementPanel.java
│   │   │   └── utils/                               # UI helper utilities
│   │   └── utils/
│   │       ├── SessionManager.java                  # Static utility — session state
│   │       └── CouponValidator.java                 # Coupon code validation logic
│   └── test/
│       └── TestScenarios.java                       # 80 automated test scenarios
├── docs/                                            # Project report (PDF)
├── lib/                                             # External libraries
├── bin/                                             # Compiled .class output (git-ignored)
├── tasks/                                           # Task tracking & lessons learned
├── UML_Diagrams.md                                  # Mermaid.js: Class, Sequence, Use Case diagrams
├── presentation.md                                  # Presentation slide content
└── sources.txt                                      # Reference list
```

---

## 🖥️ User Interface

| Panel | Role |
|-------|------|
| `LoginPanel` | User login |
| `RegisterPanel` | New user registration |
| `GuestBrowsePanel` | Read-only product catalog without login |
| `BuyerDashboard` | Main buyer view — catalog + cart + notifications |
| `SellerDashboard` | Seller view — product management, stock update, analytics log |
| `ProductCatalogPanel` | Product grid with search/filter, live price tags, low-stock badges |
| `ShoppingCartPanel` | Cart contents, decorator/discount application, checkout |
| `DiscountManagementPanel` | Manually wrap products with decorator discounts |
| `NotificationPanel` | Real-time toast notifications (price drops, stock alerts) |

---

## 🔧 SOLID Principles Applied

| Principle | Application |
|-----------|-------------|
| **SRP** | `Product` = data only. `ShoppingCart` = cart operations. `StockSubject` = state + notification. `DemandPricingObserver` = pricing math only. `AnalyticsObserver` = logging only. |
| **OCP** | New decorators (e.g., `LoyaltyDecorator`) or new observers can be added without modifying `StockSubject`, `ProductDecorator`, or any existing class. |
| **LSP** | All decorators extend `ProductDecorator implements ProductComponent` — fully substitutable. All observers implement `Observer` — interchangeable from `StockSubject`'s perspective. |
| **ISP** | `Observer` interface has 1 method. `Subject` interface has 3 focused methods. `ProductComponent` defines only what a product must expose. |
| **DIP** | `StockSubject` depends on `Observer` interface, not concrete observers. `ShoppingCart` depends on `ProductComponent` interface, not concrete decorators. |

---

## ⚙️ Technologies

| Technology | Details |
|------------|---------|
| **Language** | Java 17 |
| **GUI Framework** | Java Swing |
| **Build Tool** | Plain Java — no Maven or Gradle |
| **IDE** | IntelliJ IDEA / Eclipse / VS Code |
| **Testing** | Custom `TestScenarios.java` — 80 scenarios, all passing |

---

## 🚀 How to Run

```bash
# Clone the repository
git clone https://github.com/[your-username]/java-design-pattern-project.git
cd java-design-pattern-project

# Compile all source files
javac -d bin $(find src -name "*.java")

# Run the application
java -cp bin main.Main

# Run all 80 test scenarios
javac -d bin $(find src -name "*.java") && java -cp bin test.TestScenarios
```

> **Requirements:** Java 17+. No external dependencies.

---

## 📊 UML Diagrams

Refer to [`UML_Diagrams.md`](UML_Diagrams.md) for Mermaid.js source of:
1. **Class Diagram** — full hierarchy, interfaces, associations
2. **Sequence Diagram** — checkout flow (buyer → cart → stock → observers → decorators)
3. **Use Case Diagram** — buyer, seller, guest actor interactions

---

## ✅ Project Requirements Checklist (SEN3006)

- [x] Problem clearly defined
- [x] At least 2 design patterns implemented (we implement 3: Singleton, Decorator, Observer)
- [x] Patterns from approved list (Singleton ✓, Decorator ✓, Observer ✓)
- [x] Java 17 — meets Java 8+ requirement
- [x] SOLID principles applied and documented
- [x] At least 3 classes including interfaces and abstract classes
- [x] Executable via `Main.java`
- [x] Test cases demonstrated (80 scenarios in `TestScenarios.java`)
- [x] UML diagrams: Class, Sequence, Use Case
- [x] Full GUI implementation (Swing)
- [ ] Project report (10–14 pages, TÜBİTAK 2209 format) — *in progress*
- [ ] Presentation slides — *in progress*

---

## 📚 Documentation

| File | Description |
|------|-------------|
| [`UML_Diagrams.md`](UML_Diagrams.md) | Mermaid.js UML diagrams |
| [`presentation.md`](presentation.md) | Presentation slides content |
| [`sources.txt`](sources.txt) | Reference list |
| `docs/` | Full project report PDF *(coming soon)* |

---

## 📄 License

This project is for educational purposes only — SEN3006 Software Architecture, Spring 2026.

*Last Updated: June 2026*
