# 🛒 E-Commerce Shopping System - Design Patterns Project

**Course:** SEN3006 - Software Architecture  
**Semester:** Spring 2026  
**Team Members:** Tamer Oduncu, Sude Nur Şekerci, Robert Bora Orhan  

---

## 📋 Project Overview

A local single-store e-commerce shopping system implemented in Java, demonstrating the use of **3 Core Design Patterns**:

1. **Singleton Pattern** - Ensures single shopping cart instance per user session, along with application config and session state.
2. **Decorator Pattern** - Applies dynamic discounts, coupons, and shipping options to products.
3. **Observer Pattern** - Notifies observers when product stock changes AND drives the demand-based dynamic pricing system based on stock scarcity.

---

## 🎯 Problem Statement

An e-commerce platform where:
- Users can view products, filter by categories, and add/remove products from a cart.
- Multiple discount types can be applied dynamically (percentage, fixed amount, free shipping, flash sale).
- System must track stock changes and notify relevant components (Analytics, UI, Pricing).
- **Products' prices change dynamically based on supply and demand (stock scarcity and overstock).**
- Each user session must have exactly one shopping cart instance.
- The system must be flexible, extensible, and maintainable.

---

## 🧩 Design Patterns Used

### 1. Singleton Pattern (Creational)
**Purpose:** Guarantee single instances for core managers.  
**Why:** Prevents data inconsistency across different parts of the application.  
**Implementation:** 
- `ShoppingCart.getInstance(userId)` with thread-safe management per user.
- `AppConfig` for global application settings.
- `SessionManager` to handle currently logged-in user state.

### 2. Decorator Pattern (Structural)
**Purpose:** Add discounts/features to products dynamically.  
**Why:** Avoid class explosion from multiple discount combinations.  
**Implementation:** Product wrapped by `PercentageDiscountDecorator`, `FreeShippingDecorator`, `DynamicPricingDecorator`, etc.

```text
Product (10,000₺)
└── DynamicPricingDecorator (+20% Demand Surge) → 12,000₺
    └── PercentageDiscountDecorator (10%) → 10,800₺
        └── FreeShippingDecorator → 10,800₺ + free shipping
```

### 3. Observer Pattern (Behavioral)
**Purpose:** Notify components on stock changes AND drive demand-based pricing.  
**Why:** Loose coupling between stock management, notification system, analytics, and pricing engine.  
**Implementation:**

#### Stock Tracking (`StockSubject`)
- `StockObserver` - Monitors stock levels and logs changes to the console.
- `PriceNotificationObserver` - Creates user-facing notifications for UI when prices or stock change.
- `AnalyticsObserver` - Records events for the Admin Dashboard.
- `DemandPricingObserver` - Calculates dynamic prices based on current stock levels.

---

## 🔥 Supply & Demand System - Dynamic Pricing

### How It Works

The system uses the **Observer Pattern** to track stock levels and dynamically adjust prices based on supply and demand principles:

- **High Demand (Low Stock):** If stock drops to $\le 5$ items, the price automatically increases by **+20%**.
- **Overstock:** If stock increases to $\ge 20$ items, the price automatically drops by **-10%**.
- **Normal:** Standard price applies.

### Key Features

| Feature | Description |
|---------|-------------|
| **Auto-Triggers** | Price modifiers apply instantly when stock thresholds are crossed. |
| **Decorator Integration** | Modifiers are wrapped around the base product using `DynamicPricingDecorator`. |
| **Real-time Notifications** | Price drops or surges trigger UI alerts via `PriceNotificationObserver`. |
| **Admin Controls** | Store managers can manually update stock to trigger sales or price hikes. |

**Observer Pattern Flow:**
```text
Buyer Checkouts / Admin Updates Stock
       │
       ▼
StockSubject.decreaseStock() / setStock()
       │
       ▼
notifyObservers("STOCK_CHANGED", stockData)
       │
       ├── DemandPricingObserver → Calculates new price modifier
       ├── AnalyticsObserver     → Logs the event
       ├── PriceNotificationObserver → Shows UI Alert
       └── StockObserver         → Console logs
```

---

## 🏗️ System Architecture

```text
User (Buyer / Admin)
│
├── SessionManager (Singleton)
│
├── ShoppingCart (Singleton per user)
│     │
│     └── List<CartItem> (Decorated Products)
│           └── Decorators: % Discount, Fixed Amount, Free Shipping
│
└── Store Management (Observer Pattern)
      │
      ├── StockSubject (Observable)
      │
      └── Observers:
            ├── DemandPricingObserver (Dynamic +20% / -10%)
            ├── AnalyticsObserver (Admin Dashboard Logs)
            └── PriceNotificationObserver (UI popups)
```

---

## 🖥️ User Interface

- **Buyer Dashboard:** Product catalog with search/filter, shopping cart panel, dynamic price tags, and low stock warnings.
- **Admin Dashboard (Mağaza Yönetimi):** View all products, manually update stock (triggering dynamic pricing), apply manual decorator discounts, and view analytics logs.
- **Notification Area:** Real-time toasts for price drops, stock alerts, and coupon success messages.

---

## ⚙️ Technologies

- **Language:** Java 17
- **GUI Framework:** Java Swing
- **Build Tool:** None (plain Java project)
- **IDE:** IntelliJ IDEA / Eclipse / VS Code

---

## 🚀 How to Run

```bash
# Clone the repository
git clone https://github.com/[your-username]/java-design-pattern-project.git

# Navigate to project directory
cd java-design-pattern-project

# Compile the source code
javac -d bin $(find src -name "*.java")

# Run the application
java -cp bin main.Main

# Run all 80 tests
javac -d bin $(find src -name "*.java") && java -cp bin test.TestScenarios
```

---

## 📊 UML Diagrams

Refer to the included `UML_Diagrams.md` for Mermaid.js source codes of:
1. **Class Diagram**
2. **Sequence Diagram** (Checkout process)
3. **Use Case Diagram**

---

## 📚 Documentation

Full project report: `docs/report/project-report.pdf` *(Coming Soon)*

### ✅ Project Requirements Checklist
- [x] Problem definition
- [x] 3 Design patterns implemented (Singleton, Decorator, Observer)
- [x] SOLID principles applied
- [x] Executable via Main.java
- [x] UML diagrams (Class, Sequence, Use Case)
- [x] Test scenarios (80 tests, all passing)
- [x] Dynamic demand/supply pricing system
- [ ] Project report (10-14 pages)
- [ ] Presentation slides

---

## 🔧 SOLID Principles Applied

| Principle | Application |
|-----------|-------------|
| **SRP** | `Product` = data, `ShoppingCart` = cart ops, `StockSubject` = stock state, `DemandPricingObserver` = pricing math. |
| **OCP** | New decorators (e.g., `FlashSaleDecorator`) or observers can be added without modifying existing code. |
| **LSP** | All decorators implement `ProductComponent` and can replace base products seamlessly. |
| **ISP** | `Observer` interface is minimal and focused on `update(event, data)`. |
| **DIP** | Core systems depend on abstractions (`Observer`, `ProductComponent`), not concrete implementations. |

---

## 📄 License

This project is for educational purposes only.

*Last Updated: June 2026*
