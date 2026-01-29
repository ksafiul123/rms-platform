# ✅ Restaurant Management System – TODO LIST

> This todo.md is generated after reviewing the PRD, system design notes,
> and finalized tech rules.  
> The goal is to build the system incrementally, one production-ready feature at a time.

---

## ✅ 🧱 PHASE 0 – PROJECT FOUNDATION (BLOCKING)

- [ ] Initialize Git repository
- [ ] Setup project structure (Spring Boot – Monolith)
- [ ] Configure Java 17 + Maven/Gradle
- [ ] Setup environment profiles (dev / prod)
- [ ] Configure PostgreSQL connection
- [ ] Add Swagger (OpenAPI)
- [ ] Add global exception handling
- [ ] Define base response & error format
- [ ] Setup logging strategy
- [ ] Add Docker support (basic)

---

##  ✅🔐 PHASE 1 – AUTHENTICATION & SECURITY

- [ ] User entity & role entity
- [ ] JWT authentication (access + refresh tokens)
- [ ] Password hashing & validation
- [ ] Role-Based Access Control (RBAC)
- [ ] Secure API endpoints using Spring Security
- [ ] Login / Logout / Token refresh APIs
- [ ] Global auth exception handling
- [ ] Swagger security configuration

---

## ✅🏢 PHASE 2 – MULTI-TENANT RESTAURANT ONBOARDING

- [ ] Restaurant entity (multi-tenant safe)
- [ ] Restaurant creation (Salesman)
- [ ] Restaurant activation / deactivation
- [ ] Feature enable / disable per restaurant
- [ ] Subscription status handling
- [ ] Restaurant isolation using `restaurant_id`
- [ ] Salesman reporting workflow

---

## ✅🧑‍🤝‍🧑 PHASE 3 – ROLE & PANEL MANAGEMENT

- [ ] Admin panel APIs
- [ ] Developer panel (ticket & system ops)
- [ ] Salesman panel APIs
- [ ] Restaurant admin / manager APIs
- [ ] Chef-specific limited APIs
- [ ] Delivery man role APIs
- [ ] Role-based visibility enforcement

---

## ✅🍽️ PHASE 4 – MENU & CATEGORY MANAGEMENT

- [ ] Category CRUD
- [ ] Menu item CRUD
- [ ] Price & availability control
- [ ] Item modifiers (spice, bake level, add-ons)
- [ ] Menu visibility toggles
- [ ] Menu caching strategy (future Redis)

---

## ✅🧾 PHASE 5 – ORDER MANAGEMENT SYSTEM

- [ ] Order entity & lifecycle design
- [ ] Dine-in order flow
- [ ] Takeaway order flow
- [ ] Delivery order flow
- [ ] Order status transitions
- [ ] Order cancellation & refund rules
- [ ] Chef order update flow
- [ ] Role-based order access

---

## ✅📱 PHASE 6 – QR CODE TABLE ORDERING

- [ ] Table entity
- [ ] QR code generation per table
- [ ] Table-session order mapping
- [ ] Multiple users ordering from same table
- [ ] Web/App auto-redirect logic
- [ ] QR security & expiration rules

---

## ✅📦 PHASE 7 – INVENTORY & STOCK AUTOMATION

- [ ] Ingredient entity
- [ ] Recipe mapping per menu item
- [ ] Automatic stock deduction on order success
- [ ] Low-stock alerts
- [ ] Inventory usage reports
- [ ] Stock rollback on cancellation

---

## ✅❤️ PHASE 8 – CUSTOMER PREFERENCE ENGINE

- [ ] Favorite item tracking
- [ ] Preference profile (spice, bake level)
- [ ] Cross-restaurant preference visibility
- [ ] Chef-only preference access
- [ ] Chef feedback storage

---

## 🚚 PHASE 9 – LIVE ORDER TRACKING

- [ ] Order status broadcasting
- [ ] Kitchen → Delivery → Customer flow
- [ ] Delivery GPS tracking
- [ ] Google Maps integration hooks
- [ ] WebSocket readiness (future)

---

## 🔔 PHASE 10 – NOTIFICATION SYSTEM

- [ ] Email notification service
- [ ] WhatsApp notification hooks
- [ ] Push notification support (Firebase)
- [ ] Discount & campaign notifications
- [ ] Notification preferences per user

---

## 🎮 PHASE 11 – WAITING-TIME GAMES & REWARDS

- [ ] Game session tracking
- [ ] Multiplayer table-based session logic
- [ ] Score validation
- [ ] Discount reward calculation
- [ ] Abuse prevention rules

---

## 💳 PHASE 12 – PAYMENT & WALLET SYSTEM

- [ ] Online payment gateway integration
- [ ] Internal wallet ledger
- [ ] Transaction history
- [ ] Payment verification
- [ ] Refund handling
- [ ] Payment failure recovery

---

## 🏦 PHASE 13 – SETTLEMENT & PAYOUT

- [ ] Restaurant earnings dashboard
- [ ] Manual settlement workflow
- [ ] Bank payout tracking
- [ ] Settlement audit logs

---

## 📺 PHASE 14 – LIVE MONITOR / TV DISPLAY

- [ ] Read-only live order APIs
- [ ] Order number & status display
- [ ] Performance-optimized endpoints
- [ ] Auto-refresh logic

---

## 📊 PHASE 15 – ANALYTICS & REPORTING

- [ ] Sales analytics
- [ ] Popular item reports
- [ ] Rush hour detection
- [ ] Inventory analytics
- [ ] Revenue summaries

---

## ⚙️ PHASE 16 – PERFORMANCE & HARDENING

- [ ] Database indexing & optimization
- [ ] Transaction boundary review
- [ ] Redis caching
- [ ] API rate limiting
- [ ] Audit logs
- [ ] Security review

---

## 🐳 PHASE 17 – DEVOPS & DEPLOYMENT

- [ ] Dockerfile (backend)
- [ ] docker-compose (backend + DB + Redis)
- [ ] GitHub Actions CI/CD
- [ ] Environment secret management
- [ ] Production monitoring (Prometheus)
- [ ] Backup & recovery strategy

---

## 🔍 PHASE 18 – FINAL REVIEW

- [ ] Security audit
- [ ] Scalability bottleneck review
- [ ] Cost optimization review
- [ ] Production readiness checklist
