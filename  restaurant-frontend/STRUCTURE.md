# Complete Project Structure Documentation

## 📂 Detailed Folder Structure

```
restaurant-frontend/
│
├── 📁 public/                           # Static Assets
│   ├── images/                          # Image files
│   ├── icons/                           # Icon files (SVG, PNG)
│   └── fonts/                           # Custom fonts
│
├── 📁 src/
│   │
│   ├── 📁 app/                          # Next.js App Router
│   │   │
│   │   ├── 📁 (public)/                # Public Routes (No Auth Required)
│   │   │   ├── login/
│   │   │   │   └── page.tsx            # Login page
│   │   │   ├── register/
│   │   │   │   └── page.tsx            # Registration page
│   │   │   ├── forgot-password/
│   │   │   │   └── page.tsx            # Forgot password page
│   │   │   ├── reset-password/
│   │   │   │   └── page.tsx            # Reset password page
│   │   │   └── layout.tsx              # Public routes layout
│   │   │
│   │   ├── 📁 (protected)/             # Protected Routes (Auth Required)
│   │   │   │
│   │   │   ├── 📁 dashboard/           # Dashboard Routes
│   │   │   │   ├── super-admin/
│   │   │   │   │   └── page.tsx       # Super Admin dashboard
│   │   │   │   ├── restaurant-admin/
│   │   │   │   │   └── page.tsx       # Restaurant Admin dashboard
│   │   │   │   ├── manager/
│   │   │   │   │   └── page.tsx       # Manager dashboard
│   │   │   │   ├── chef/
│   │   │   │   │   └── page.tsx       # Chef dashboard
│   │   │   │   ├── delivery/
│   │   │   │   │   └── page.tsx       # Delivery dashboard
│   │   │   │   ├── customer/
│   │   │   │   │   └── page.tsx       # Customer dashboard
│   │   │   │   └── page.tsx           # Default dashboard
│   │   │   │
│   │   │   ├── 📁 menu/               # Menu Management
│   │   │   │   ├── categories/
│   │   │   │   │   ├── [id]/
│   │   │   │   │   │   └── page.tsx   # Edit category
│   │   │   │   │   ├── new/
│   │   │   │   │   │   └── page.tsx   # Create category
│   │   │   │   │   └── page.tsx       # Categories list
│   │   │   │   ├── items/
│   │   │   │   │   ├── [id]/
│   │   │   │   │   │   └── page.tsx   # Edit menu item
│   │   │   │   │   ├── new/
│   │   │   │   │   │   └── page.tsx   # Create menu item
│   │   │   │   │   └── page.tsx       # Menu items list
│   │   │   │   ├── modifiers/
│   │   │   │   │   └── page.tsx       # Modifier groups
│   │   │   │   └── page.tsx           # Menu overview
│   │   │   │
│   │   │   ├── 📁 orders/             # Order Management
│   │   │   │   ├── [id]/
│   │   │   │   │   └── page.tsx       # Order details
│   │   │   │   └── page.tsx           # Orders list
│   │   │   │
│   │   │   ├── 📁 inventory/          # Inventory Management
│   │   │   │   └── page.tsx           # Inventory dashboard
│   │   │   │
│   │   │   ├── 📁 users/              # User Management
│   │   │   │   └── page.tsx           # Users list
│   │   │   │
│   │   │   ├── 📁 settings/           # Settings
│   │   │   │   └── page.tsx           # Settings page
│   │   │   │
│   │   │   ├── 📁 analytics/          # Analytics
│   │   │   │   └── page.tsx           # Analytics dashboard
│   │   │   │
│   │   │   ├── 📁 payments/           # Payment Management
│   │   │   │   └── page.tsx           # Payments dashboard
│   │   │   │
│   │   │   ├── 📁 restaurant/         # Restaurant Settings
│   │   │   │   └── page.tsx           # Restaurant configuration
│   │   │   │
│   │   │   └── layout.tsx             # Protected routes layout
│   │   │
│   │   ├── layout.tsx                 # Root layout
│   │   └── page.tsx                   # Home page
│   │
│   ├── 📁 components/                  # Reusable Components
│   │   │
│   │   ├── 📁 ui/                     # Base UI Components
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Card.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── Table.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Dropdown.tsx
│   │   │   ├── Tabs.tsx
│   │   │   ├── Alert.tsx
│   │   │   ├── Spinner.tsx
│   │   │   └── Avatar.tsx
│   │   │
│   │   ├── 📁 forms/                  # Form Components
│   │   │   ├── FormInput.tsx
│   │   │   ├── FormSelect.tsx
│   │   │   ├── FormTextarea.tsx
│   │   │   ├── FormCheckbox.tsx
│   │   │   ├── FormRadio.tsx
│   │   │   └── FormError.tsx
│   │   │
│   │   ├── 📁 layout/                 # Layout Components
│   │   │   ├── Sidebar.tsx
│   │   │   ├── Navbar.tsx
│   │   │   ├── Header.tsx
│   │   │   ├── Footer.tsx
│   │   │   └── Container.tsx
│   │   │
│   │   ├── 📁 shared/                 # Shared Components
│   │   │   ├── LoadingSpinner.tsx
│   │   │   ├── ErrorMessage.tsx
│   │   │   ├── EmptyState.tsx
│   │   │   ├── Pagination.tsx
│   │   │   ├── SearchBar.tsx
│   │   │   └── FilterPanel.tsx
│   │   │
│   │   ├── 📁 dashboard/              # Dashboard Components
│   │   │   ├── StatsCard.tsx
│   │   │   ├── RecentOrders.tsx
│   │   │   ├── SalesChart.tsx
│   │   │   └── QuickActions.tsx
│   │   │
│   │   ├── 📁 menu/                   # Menu Components
│   │   │   ├── CategoryCard.tsx
│   │   │   ├── MenuItemCard.tsx
│   │   │   ├── MenuItemForm.tsx
│   │   │   └── ModifierForm.tsx
│   │   │
│   │   ├── 📁 orders/                 # Order Components
│   │   │   ├── OrderCard.tsx
│   │   │   ├── OrderDetails.tsx
│   │   │   ├── OrderStatusBadge.tsx
│   │   │   └── OrderTimeline.tsx
│   │   │
│   │   ├── 📁 inventory/              # Inventory Components
│   │   │   ├── StockTable.tsx
│   │   │   ├── LowStockAlert.tsx
│   │   │   └── InventoryForm.tsx
│   │   │
│   │   ├── 📁 users/                  # User Components
│   │   │   ├── UserTable.tsx
│   │   │   ├── UserForm.tsx
│   │   │   └── RoleBadge.tsx
│   │   │
│   │   ├── 📁 analytics/              # Analytics Components
│   │   │   ├── SalesChart.tsx
│   │   │   ├── OrdersChart.tsx
│   │   │   └── RevenueCard.tsx
│   │   │
│   │   └── 📁 modals/                 # Modal Components
│   │       ├── ConfirmModal.tsx
│   │       ├── FormModal.tsx
│   │       └── DetailsModal.tsx
│   │
│   ├── 📁 lib/                        # Core Utilities
│   │   │
│   │   ├── 📁 api/                    # API Services
│   │   │   ├── client.ts             # Axios client with interceptors
│   │   │   ├── auth.service.ts       # Auth API calls
│   │   │   ├── menu.service.ts       # Menu API calls
│   │   │   ├── order.service.ts      # Order API calls
│   │   │   ├── user.service.ts       # User API calls
│   │   │   ├── inventory.service.ts  # Inventory API calls
│   │   │   ├── analytics.service.ts  # Analytics API calls
│   │   │   └── index.ts              # Export all services
│   │   │
│   │   ├── 📁 auth/                   # Auth Utilities
│   │   │   └── index.ts              # Auth helper functions
│   │   │
│   │   ├── 📁 permissions/            # RBAC Utilities
│   │   │   └── index.ts              # Permission checking functions
│   │   │
│   │   ├── 📁 utils/                  # Helper Functions
│   │   │   └── index.ts              # Common utility functions
│   │   │
│   │   └── 📁 validations/            # Validation Schemas
│   │       └── schemas.ts            # Zod validation schemas
│   │
│   ├── 📁 types/                      # TypeScript Types
│   │   ├── user.ts                   # User types
│   │   ├── auth.ts                   # Auth types
│   │   ├── api.ts                    # API types
│   │   ├── permission.ts             # Permission types
│   │   ├── menu.ts                   # Menu types
│   │   ├── order.ts                  # Order types
│   │   └── index.ts                  # Export all types
│   │
│   ├── 📁 contexts/                   # React Context
│   │   ├── AuthContext.tsx           # Authentication context
│   │   └── ThemeContext.tsx          # Theme context (future)
│   │
│   ├── 📁 hooks/                      # Custom Hooks
│   │   ├── useAuth.ts                # Auth hook
│   │   ├── usePermissions.ts         # Permissions hook
│   │   ├── useDebounce.ts            # Debounce hook
│   │   └── useLocalStorage.ts        # LocalStorage hook
│   │
│   ├── 📁 config/                     # Configuration
│   │   ├── api.config.ts             # API configuration
│   │   └── routes.config.ts          # Routes configuration
│   │
│   ├── 📁 constants/                  # Constants
│   │   ├── permissions.ts            # Permission constants
│   │   └── index.ts                  # Common constants
│   │
│   ├── 📁 styles/                     # Styles
│   │   └── globals.css               # Global styles
│   │
│   └── middleware.ts                  # Next.js middleware
│
├── .env.local                         # Environment variables
├── .env.example                       # Environment variables template
├── .gitignore                         # Git ignore rules
├── next.config.js                     # Next.js configuration
├── tailwind.config.ts                 # Tailwind CSS configuration
├── postcss.config.js                  # PostCSS configuration
├── tsconfig.json                      # TypeScript configuration
├── package.json                       # Dependencies
└── README.md                          # Project documentation
```

## 📝 File Descriptions

### Configuration Files

- **next.config.js** - Next.js framework configuration
- **tailwind.config.ts** - Tailwind CSS theme and plugins
- **tsconfig.json** - TypeScript compiler options
- **postcss.config.js** - PostCSS plugins (Tailwind, Autoprefixer)
- **.env.local** - Environment variables (local development)
- **.env.example** - Environment variables template

### Core Application Files

- **src/app/layout.tsx** - Root layout with providers
- **src/app/page.tsx** - Home page (redirects to login/dashboard)
- **src/middleware.ts** - Authentication middleware for route protection

### Context & State Management

- **src/contexts/AuthContext.tsx** - Global auth state (login, logout, user)

### API Layer

- **src/lib/api/client.ts** - Axios instance with interceptors
- **src/lib/api/*.service.ts** - API service modules

### Type Definitions

- **src/types/*.ts** - TypeScript interfaces and types

### Utilities

- **src/lib/utils/** - Helper functions
- **src/lib/permissions/** - RBAC utilities
- **src/lib/validations/** - Zod schemas

### Constants

- **src/constants/permissions.ts** - Role permissions mapping
- **src/constants/index.ts** - UI constants (colors, formats, etc.)

### Configuration

- **src/config/api.config.ts** - API endpoints and settings
- **src/config/routes.config.ts** - Route paths and access rules

## 🎯 Key Architecture Decisions

1. **App Router** - Using Next.js 14 App Router for better performance
2. **Route Groups** - `(public)` and `(protected)` for layout separation
3. **Colocated Components** - Components organized by feature
4. **Service Layer** - Separate API services for each domain
5. **Type Safety** - Comprehensive TypeScript types
6. **Permission-Based UI** - Components check user permissions
7. **Middleware Auth** - Route protection at middleware level
8. **Token Management** - Automatic refresh with Axios interceptors

## 🔄 Data Flow

```
User Action → Component → Hook → API Service → Axios Client → Backend
                ↓                                                  ↓
            Update UI ← Context ← Response ← Interceptor ← Response
```

## 🛡️ Security Features

- JWT token storage in localStorage
- Automatic token refresh
- Route protection with middleware
- Permission-based component rendering
- CSRF protection (built into Next.js)
- XSS protection with React's built-in escaping

## 📦 Module Boundaries

- **Components** - Pure UI components, no business logic
- **Services** - API calls and data fetching
- **Hooks** - Reusable stateful logic
- **Contexts** - Global state management
- **Utils** - Pure functions, no side effects

This structure is designed to scale with your application while maintaining clean separation of concerns.