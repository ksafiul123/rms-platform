import { z } from "zod";

// ==================== Auth Schemas ====================

export const loginSchema = z.object({
  email: z.string().email("Invalid email address"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

export const registerSchema = z.object({
  fullName: z.string().min(2, "Name must be at least 2 characters"),
  email: z.string().email("Invalid email address"),
  password: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .regex(/[A-Z]/, "Password must contain at least one uppercase letter")
    .regex(/[a-z]/, "Password must contain at least one lowercase letter")
    .regex(/[0-9]/, "Password must contain at least one number"),
  phoneNumber: z
    .string()
    .regex(
      /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/,
      "Invalid phone number"
    ),
});

export const forgotPasswordSchema = z.object({
  email: z.string().email("Invalid email address"),
});

export const resetPasswordSchema = z.object({
  password: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .regex(/[A-Z]/, "Password must contain at least one uppercase letter")
    .regex(/[a-z]/, "Password must contain at least one lowercase letter")
    .regex(/[0-9]/, "Password must contain at least one number"),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"],
});

// ==================== Menu Schemas ====================

export const menuCategorySchema = z.object({
  name: z.string().min(2, "Category name must be at least 2 characters"),
  description: z.string().optional(),
  displayOrder: z.number().min(0, "Display order must be positive"),
  isActive: z.boolean().default(true),
});

export const menuItemSchema = z.object({
  name: z.string().min(2, "Item name must be at least 2 characters"),
  description: z.string().optional(),
  basePrice: z.number().min(0, "Price must be positive"),
  categoryId: z.number().min(1, "Please select a category"),
  imageUrl: z.string().url("Invalid image URL").optional().or(z.literal("")),
  isAvailable: z.boolean().default(true),
  isVegetarian: z.boolean().default(false),
  isVegan: z.boolean().default(false),
  isGlutenFree: z.boolean().default(false),
  preparationTime: z.number().min(0, "Preparation time must be positive"),
});

export const modifierGroupSchema = z.object({
  name: z.string().min(2, "Group name must be at least 2 characters"),
  description: z.string().optional(),
  isRequired: z.boolean().default(false),
  minSelections: z.number().min(0),
  maxSelections: z.number().min(1),
  displayOrder: z.number().min(0),
}).refine((data) => data.minSelections <= data.maxSelections, {
  message: "Min selections cannot exceed max selections",
  path: ["minSelections"],
});

// ==================== Order Schemas ====================

export const createOrderSchema = z.object({
  orderType: z.enum(["DINE_IN", "TAKEAWAY", "DELIVERY"]),
  tableNumber: z.string().optional(),
  customerName: z.string().optional(),
  customerPhone: z.string().optional(),
  deliveryAddress: z.string().optional(),
  items: z
    .array(
      z.object({
        menuItemId: z.number(),
        quantity: z.number().min(1),
        variantId: z.number().optional(),
        modifierOptionIds: z.array(z.number()).optional(),
        specialInstructions: z.string().optional(),
      })
    )
    .min(1, "Order must have at least one item"),
  notes: z.string().optional(),
});

// ==================== Restaurant Schemas ====================

export const restaurantRegisterSchema = z.object({
  restaurantName: z.string().min(2, "Restaurant name is required"),
  address: z.string().min(5, "Address is required"),
  restaurantPhone: z
    .string()
    .regex(
      /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/,
      "Invalid phone number"
    ),
  adminFullName: z.string().min(2, "Admin name is required"),
  adminEmail: z.string().email("Invalid email address"),
  adminPassword: z
    .string()
    .min(8, "Password must be at least 8 characters")
    .regex(/[A-Z]/, "Password must contain at least one uppercase letter")
    .regex(/[a-z]/, "Password must contain at least one lowercase letter")
    .regex(/[0-9]/, "Password must contain at least one number"),
  adminPhone: z
    .string()
    .regex(
      /^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/,
      "Invalid phone number"
    ),
  subscriptionPlanId: z.number().optional(),
});

// ==================== Helper Functions ====================

export type LoginFormData = z.infer<typeof loginSchema>;
export type RegisterFormData = z.infer<typeof registerSchema>;
export type ForgotPasswordFormData = z.infer<typeof forgotPasswordSchema>;
export type ResetPasswordFormData = z.infer<typeof resetPasswordSchema>;
export type MenuCategoryFormData = z.infer<typeof menuCategorySchema>;
export type MenuItemFormData = z.infer<typeof menuItemSchema>;
export type ModifierGroupFormData = z.infer<typeof modifierGroupSchema>;
export type CreateOrderFormData = z.infer<typeof createOrderSchema>;
export type RestaurantRegisterFormData = z.infer<typeof restaurantRegisterSchema>;