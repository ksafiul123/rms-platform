// Menu Types
export interface MenuCategory {
  id: number;
  name: string;
  description?: string;
  displayOrder: number;
  isActive: boolean;
  restaurantId: number;
  itemCount?: number;
  createdAt: string;
  updatedAt: string;
}

export interface MenuItem {
  id: number;
  name: string;
  description?: string;
  basePrice: number;
  categoryId: number;
  categoryName?: string;
  imageUrl?: string;
  isAvailable: boolean;
  isVegetarian: boolean;
  isVegan: boolean;
  isGlutenFree: boolean;
  preparationTime: number;
  restaurantId: number;
  variants?: ItemVariant[];
  modifierGroups?: ModifierGroup[];
  ingredients?: ItemIngredient[];
  createdAt: string;
  updatedAt: string;
}

export interface ItemVariant {
  id: number;
  menuItemId: number;
  name: string;
  priceAdjustment: number;
  isAvailable: boolean;
  displayOrder: number;
}

export interface ModifierGroup {
  id: number;
  name: string;
  description?: string;
  isRequired: boolean;
  minSelections: number;
  maxSelections: number;
  restaurantId: number;
  options: ModifierOption[];
  displayOrder: number;
}

export interface ModifierOption {
  id: number;
  modifierGroupId: number;
  name: string;
  priceAdjustment: number;
  isAvailable: boolean;
  displayOrder: number;
}

export interface ItemIngredient {
  id: number;
  menuItemId: number;
  ingredientId: number;
  ingredientName: string;
  quantity: number;
  unit: string;
}

export interface PriceSchedule {
  id: number;
  menuItemId: number;
  dayOfWeek?: number;
  startTime?: string;
  endTime?: string;
  price: number;
  isActive: boolean;
}

// Request Types
export interface MenuCategoryRequest {
  name: string;
  description?: string;
  displayOrder: number;
  isActive: boolean;
}

export interface MenuItemRequest {
  name: string;
  description?: string;
  basePrice: number;
  categoryId: number;
  imageUrl?: string;
  isAvailable: boolean;
  isVegetarian: boolean;
  isVegan: boolean;
  isGlutenFree: boolean;
  preparationTime: number;
}

export interface ModifierGroupRequest {
  name: string;
  description?: string;
  isRequired: boolean;
  minSelections: number;
  maxSelections: number;
  displayOrder: number;
}