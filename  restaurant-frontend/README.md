# Restaurant Management System - Frontend

A production-grade, scalable Next.js frontend for a multi-role SaaS Restaurant Management System with comprehensive authentication, role-based access control, and modern UI components.

## 🚀 Features

- ✅ **Next.js 14** with App Router
- ✅ **TypeScript** for type safety
- ✅ **Tailwind CSS** for styling
- ✅ **JWT Authentication** with automatic token refresh
- ✅ **Role-Based Access Control (RBAC)** - 8 user roles
- ✅ **Protected Routes** with middleware
- ✅ **Axios API Client** with interceptors
- ✅ **React Hook Form** with Zod validation
- ✅ **Context API** for state management
- ✅ **Responsive Design**
- ✅ **Loading & Error States**
- ✅ **Toast Notifications**

## 📁 Project Structure

```
restaurant-frontend/
├── public/                     # Static assets
│   ├── images/
│   ├── icons/
│   └── fonts/
│
├── src/
│   ├── app/                    # Next.js App Router
│   │   ├── (public)/          # Public routes (no auth)
│   │   │   ├── login/
│   │   │   ├── register/
│   │   │   ├── forgot-password/
│   │   │   └── layout.tsx
│   │   │
│   │   ├── (protected)/       # Protected routes (auth required)
│   │   │   ├── dashboard/
│   │   │   │   ├── super-admin/
│   │   │   │   ├── restaurant-admin/
│   │   │   │   ├── manager/
│   │   │   │   ├── chef/
│   │   │   │   ├── delivery/
│   │   │   │   └── customer/
│   │   │   ├── menu/
│   │   │   ├── orders/
│   │   │   ├── inventory/
│   │   │   ├── users/
│   │   │   ├── settings/
│   │   │   ├── analytics/
│   │   │   └── layout.tsx
│   │   │
│   │   ├── layout.tsx
│   │   └── page.tsx
│   │
│   ├── components/            # Reusable components
│   │   ├── ui/               # Base UI components
│   │   ├── forms/            # Form components
│   │   ├── layout/           # Layout components (Sidebar, Navbar)
│   │   ├── shared/           # Shared components
│   │   ├── dashboard/        # Dashboard components
│   │   ├── menu/             # Menu components
│   │   ├── orders/           # Order components
│   │   └── modals/           # Modal components
│   │
│   ├── lib/                  # Core utilities
│   │   ├── api/              # API services
│   │   │   ├── client.ts     # Axios client with interceptors
│   │   │   ├── auth.service.ts
│   │   │   ├── menu.service.ts
│   │   │   └── order.service.ts
│   │   ├── auth/             # Auth utilities
│   │   ├── permissions/      # RBAC utilities
│   │   ├── utils/            # Helper functions
│   │   └── validations/      # Zod schemas
│   │
│   ├── types/                # TypeScript types
│   │   ├── user.ts
│   │   ├── auth.ts
│   │   ├── api.ts
│   │   ├── permission.ts
│   │   ├── menu.ts
│   │   ├── order.ts
│   │   └── index.ts
│   │
│   ├── contexts/             # React Context providers
│   │   └── AuthContext.tsx
│   │
│   ├── hooks/                # Custom React hooks
│   │
│   ├── config/               # App configuration
│   │   ├── api.config.ts
│   │   └── routes.config.ts
│   │
│   ├── constants/            # Constants
│   │   ├── permissions.ts
│   │   └── index.ts
│   │
│   ├── styles/               # Global styles
│   │   └── globals.css
│   │
│   └── middleware.ts         # Next.js middleware for auth
│
├── .env.local                # Environment variables
├── .env.example              # Environment variables example
├── next.config.js            # Next.js configuration
├── tailwind.config.ts        # Tailwind configuration
├── tsconfig.json             # TypeScript configuration
└── package.json              # Dependencies
```

## 🎯 User Roles & Permissions

### Role Hierarchy (by privilege level)

1. **SUPER_ADMIN** - Full system access
2. **DEVELOPER** - System maintenance
3. **SALESMAN** - Restaurant onboarding
4. **RESTAURANT_ADMIN** - Full restaurant access
5. **MANAGER** - Operations management
6. **CHEF** - Kitchen operations
7. **DELIVERY_MAN** - Delivery operations
8. **CUSTOMER** - Customer self-service

### Permission Categories

- Menu Management (create, read, update, delete, approve, manage)
- Order Management (create, read, update, cancel, approve, assign, manage)
- Inventory Management (create, read, update, delete, manage)
- User Management (create, read, update, delete, manage)
- Permission Management (view, override, role management)
- Restaurant Management (update, manage, features, branches)
- Financial Management (process, refund, view, reports)
- Analytics (view, export)
- Customer Management (view, manage)
- Table Management (view, manage)

## 🛠️ Installation

### Prerequisites

- Node.js 18+ and npm/yarn
- Backend API running on `http://localhost:8080`

### Steps

1. **Clone the repository**
```bash
git clone <repository-url>
cd restaurant-frontend
```

2. **Install dependencies**
```bash
npm install
# or
yarn install
```

3. **Configure environment variables**
```bash
cp .env.example .env.local
```

Edit `.env.local` and set your API URL:
```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
```

4. **Run development server**
```bash
npm run dev
# or
yarn dev
```

5. **Open browser**
```
http://localhost:3000
```

## 🔧 Configuration

### API Configuration

Edit `src/config/api.config.ts` to configure API endpoints:

```typescript
export const API_CONFIG = {
  BASE_URL: process.env.NEXT_PUBLIC_API_URL,
  TIMEOUT: 30000,
  ENDPOINTS: {
    AUTH: { ... },
    MENU: { ... },
    ORDERS: { ... },
    // ...
  }
}
```

### Route Configuration

Edit `src/config/routes.config.ts` to configure routes:

```typescript
export const ROUTES = {
  LOGIN: "/login",
  DASHBOARD: "/dashboard",
  MENU: "/menu",
  // ...
}
```

## 🔐 Authentication Flow

1. User logs in with email/password
2. Backend returns JWT access token (1h) and refresh token (7d)
3. Access token stored in localStorage
4. Axios interceptor adds token to all requests
5. Token auto-refreshed before expiry
6. Invalid tokens trigger automatic logout

## 🎨 Styling

- **Tailwind CSS** for utility-first styling
- **CSS Variables** for theming
- **Dark mode** support (configured)
- **Responsive** design
- **Custom animations** (fade-in, slide-in, spin)

## 📝 Code Conventions

### File Naming

- Components: PascalCase (`LoginForm.tsx`)
- Utilities: camelCase (`formatDate.ts`)
- Constants: UPPER_SNAKE_CASE (`API_CONFIG`)
- Types: PascalCase (`UserInfo`)

### Component Structure

```typescript
// 1. Imports
import { useState } from "react";
import { useAuth } from "@/contexts/AuthContext";

// 2. Types
interface Props {
  title: string;
}

// 3. Component
export default function Component({ title }: Props) {
  // 4. Hooks
  const { user } = useAuth();
  
  // 5. State
  const [loading, setLoading] = useState(false);
  
  // 6. Effects
  useEffect(() => {}, []);
  
  // 7. Handlers
  const handleClick = () => {};
  
  // 8. Render
  return <div>{title}</div>;
}
```

## 🧪 Testing

```bash
# Type checking
npm run type-check

# Linting
npm run lint

# Format code
npm run format
```

## 📦 Build & Deploy

```bash
# Build for production
npm run build

# Start production server
npm start
```

## 🚀 Next Steps

**Phase 2** - UI Components:
- Reusable UI component library
- Layout components (Sidebar, Navbar)
- Form components
- Modal components
- Table components

**Phase 3** - Feature Modules:
- Menu Management UI
- Order Management UI
- Inventory Management UI
- User Management UI
- Analytics Dashboard

## 📄 License

MIT License

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open Pull Request

## 📧 Support

For issues and questions, please open a GitHub issue.