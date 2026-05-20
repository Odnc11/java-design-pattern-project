# 🛒 E-Commerce Shopping System - Design Patterns Project

**Course:** SEN3006 - Software Architecture  
**Semester:** Spring 2026  
**Team Members:** [İsim 1], [İsim 2], [İsim 3]  
**Submission Date:** June 8, 2026

---

## 📋 Project Overview

A local e-commerce shopping system with a **demand-driven auction model** implemented in Java, demonstrating the use of **3 Design Patterns**:

1. **Singleton Pattern** - Ensures single shopping cart instance per user session
2. **Decorator Pattern** - Applies dynamic discounts, coupons, and shipping options to products
3. **Observer Pattern** - Notifies observers when product stock changes AND drives the auction system with demand-based dynamic pricing

---

## 🎯 Problem Statement

An e-commerce platform where:
- Users can add/remove products from a cart
- Multiple discount types can be applied dynamically (percentage, coupon, free shipping)
- System must track stock changes and notify relevant components
- **Products' prices change dynamically based on market demand (click/view count) and stock scarcity**
- **Users can bid against each other in auctions when demand exceeds a threshold**
- Each user session must have exactly one shopping cart instance
- The system must be flexible, extensible, and maintainable

---

## 🧩 Design Patterns Used

### 1. Singleton Pattern (Creational)
**Purpose:** Guarantee single shopping cart instance  
**Why:** Prevents cart data inconsistency across different parts of the application  
**Implementation:** `ShoppingCart.getInstance()` with thread-safe double-checked locking

### 2. Decorator Pattern (Structural)
**Purpose:** Add discounts/features to products dynamically  
**Why:** Avoid class explosion from multiple discount combinations  
**Implementation:** Product wrapped by DiscountDecorator, CouponDecorator, etc.

```
Product (100₺)
└── DiscountDecorator (10%) → 90₺
    └── CouponDecorator (20₺) → 70₺
        └── FreeShippingDecorator → 70₺ + free shipping
```

### 3. Observer Pattern (Behavioral)
**Purpose:** Notify observers on stock changes AND drive demand-based auction pricing  
**Why:** Loose coupling between stock management, notification system, and auction engine  
**Implementation:**

#### Stock Tracking (StockSubject)
- `StockObserver` - Monitors stock levels and logs changes
- `NotificationObserver` - Creates user-facing notifications for UI

#### Auction / Demand Tracking (DemandTracker → AuctionSubject)
- `AuctionEventObserver` - Logs price changes and bid events
- Tracks product demand (click count) and calculates dynamic prices

---

## 🔥 Auction System - Demand-Driven Pricing

### How It Works

The auction system uses the **Observer Pattern** to track product demand and dynamically adjust prices based on a combination of demand intensity and stock scarcity:

```
📊 Dynamic Pricing Formula:
─────────────────────────────
demandRatio   = clickCount / DEMAND_THRESHOLD(5)
scarcityRatio = 1.0 / (currentStock + 1)
multiplier    = 1 + (demandRatio × scarcityRatio × SCALE_FACTOR(0.5))
newPrice      = basePrice × min(multiplier, MAX_MULTIPLIER(3.0))
```

### Example Scenario

```
Product: Laptop (15,999.99₺), Stock: 2, Clicks: 20

demandRatio   = 20 / 5 = 4.0
scarcityRatio = 1 / 3  = 0.33
multiplier    = 1 + (4.0 × 0.33 × 0.5) = 1.67

New Price = 15,999.99 × 1.67 = 26,719.98₺ (+67% increase!)
```

### Key Features

| Feature | Description |
|---------|-------------|
| **Demand Tracking** | Every product click increases demand count |
| **Auto-Activation** | Auctions activate when demand ≥ 5 clicks |
| **Dynamic Pricing** | Prices rise with more demand + lower stock |
| **User Bidding** | Users compete by placing bids above current price |
| **Max Cap** | Prices cannot exceed 3x the base price |
| **Real-time Events** | All changes broadcast via Observer pattern |
| **Heat Indicators** | Visual demand levels: Low → Medium → High → Very High |

### Observer Pattern Flow in Auction

```
User Clicks Product
       │
       ▼
DemandTracker.recordDemand()
       │
       ├── Calculates new dynamic price
       │     (based on demand + stock ratio)
       │
       ▼
AuctionSubject.notifyPriceChange()
       │
       ├── AuctionEventObserver.onPriceChanged()
       │     └── Logs event + notifies UI
       │
       └── UI updates price display
```

---

## 🏗️ System Architecture

```
User
│
├── ShoppingCart (Singleton)
│     │
│     ├── List<CartItem> (Decorated Products)
│     │     └── Decorators: Discount, Coupon, FreeShipping
│     │
│     └── Observes StockSubject
│           └── Notifies: StockObserver, NotificationObserver
│
└── Auction System (Observer Pattern Extension)
      │
      ├── DemandTracker (AuctionSubject)
      │     ├── Tracks clicks/demand per product
      │     ├── Calculates dynamic prices
      │     └── Manages bids
      │
      └── AuctionEventObserver
            └── Logs price changes + bid events → UI
```

---

## 🖥️ User Interface

- **Tab 1 - Product Catalog:** Product catalog with search/filter, dynamic prices, auction badges
- **Tab 2 - Live Auction:** Demand heat indicators, bidding interface, pricing algorithm display
- **Shopping Cart Panel (right):** View cart items, apply coupons/decorators, checkout
- **Notification Area (bottom):** Stock alerts, auction events (Observer Pattern)

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

# Compile
javac -d bin src/main/models/*.java src/main/patterns/observer/*.java src/main/patterns/decorator/*.java src/main/patterns/singleton/*.java src/main/ui/*.java src/main/Main.java

# Run the application
java -cp bin main.Main

# Run tests
javac -d bin src/main/models/*.java src/main/patterns/observer/*.java src/main/patterns/decorator/*.java src/main/patterns/singleton/*.java src/main/ui/*.java src/main/Main.java src/test/TestScenarios.java
java -cp bin test.TestScenarios
```

---

## 📊 UML Diagrams

- [Class Diagram](docs/diagrams/class-diagram.png)
- [Sequence Diagram](docs/diagrams/sequence-diagram.png)
- [Use Case Diagram](docs/diagrams/use-case-diagram.png)

---

## 📚 Documentation

Full project report: [docs/report/project-report.pdf](docs/report/project-report.pdf)

---

## ✅ Project Requirements Checklist

- [x] Problem definition
- [x] 3 Design patterns implemented (Singleton, Decorator, Observer)
- [x] SOLID principles applied
- [x] Executable via Main.java
- [x] UML diagrams (Class, Sequence, Use Case)
- [x] Test scenarios (57 tests, all passing)
- [x] Auction/demand-driven pricing system
- [x] Project report (10-14 pages)
- [ ] Presentation slides

---

## 🔧 SOLID Principles Applied

| Principle | Application |
|-----------|------------|
| **SRP** | Each class has a single responsibility (Product = data, ShoppingCart = cart ops, StockSubject = stock management, DemandTracker = demand/auction) |
| **OCP** | New decorators/observers can be added without modifying existing code. AuctionObserver extends Observer pattern without changing stock observers |
| **LSP** | All decorators are substitutable for ProductComponent |
| **ISP** | Observer, AuctionObserver, Subject, and AuctionSubject interfaces are minimal and focused |
| **DIP** | High-level modules depend on abstractions (Observer, AuctionObserver, Subject, AuctionSubject, ProductComponent interfaces) |

---

## 📁 Project Structure

```
src/
├── main/
│   ├── Main.java                          # Entry point
│   ├── models/
│   │   ├── Product.java                   # Product data model
│   │   ├── User.java                      # User data model
│   │   └── Bid.java                       # Auction bid model
│   ├── patterns/
│   │   ├── singleton/
│   │   │   └── ShoppingCart.java           # Singleton cart
│   │   ├── decorator/
│   │   │   ├── ProductComponent.java       # Component interface
│   │   │   ├── ConcreteProduct.java        # Concrete component
│   │   │   ├── ProductDecorator.java       # Abstract decorator
│   │   │   ├── DiscountDecorator.java      # % discount
│   │   │   ├── CouponDecorator.java        # Fixed coupon
│   │   │   └── FreeShippingDecorator.java  # Free shipping
│   │   └── observer/
│   │       ├── Observer.java               # Stock observer interface
│   │       ├── Subject.java                # Stock subject interface
│   │       ├── AuctionObserver.java        # Auction observer interface
│   │       ├── AuctionSubject.java         # Auction subject interface
│   │       ├── StockSubject.java           # Stock tracking
│   │       ├── StockObserver.java          # Stock monitor
│   │       ├── NotificationObserver.java   # UI notifications
│   │       ├── DemandTracker.java          # Demand + auction engine
│   │       └── AuctionEventObserver.java   # Auction event logger
│   └── ui/
│       ├── MainFrame.java                  # Main window (tabbed)
│       ├── ProductPanel.java               # Product catalog
│       ├── CartPanel.java                  # Shopping cart
│       └── AuctionPanel.java              # Auction interface
└── test/
    └── TestScenarios.java                  # 57 test scenarios
```

---

## 👥 Team Responsibilities

| Member | Responsibilities |
|--------|-----------------| 
| [İsim 1] | Singleton + Observer + Class Diagram |
| [İsim 2] | Decorator + UI + Sequence Diagram |
| [İsim 3] | Auction System + Integration + Testing + Report |

---

## 📅 Development Timeline

- **Week 1 (May 21-27):** Core patterns implementation
- **Week 2 (May 28 - June 3):** UI + Auction System + Integration + UML
- **Week 3 (June 4-8):** Testing + Report + Final submission

---

## 🔗 References

- Design Patterns: Elements of Reusable Object-Oriented Software (GoF)
- Head First Design Patterns
- Refactoring.Guru - Design Patterns

---

## 📄 License

This project is for educational purposes only.

---

**Last Updated:** May 20, 2026
