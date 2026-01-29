# Customer Preference Tracking System - Complete Guide

## 🎯 Overview

Comprehensive customer preference tracking system that allows customers to:
- Mark favorite menu items
- Store global food preferences (spice level, cooking preferences, dietary restrictions)
- Set item-specific preferences (customizations per menu item)
- Control privacy (preferences visible/hidden from chefs)

**Chefs can view customer preferences across ALL restaurants** to prepare food according to customer preferences.

---

## 🔑 Key Features

### **1. Global Food Preferences**
- **Taste Preferences**: Spice level, sweetness, salt level
- **Cooking Preferences**: Rare, medium, well-done, crispy, etc.
- **Temperature**: Cold, hot, extra hot
- **Dietary Restrictions**: Vegetarian, vegan, gluten-free, dairy-free, nut-free
- **Allergies**: List of allergens
- **Dislikes**: Foods customer doesn't like
- **Portion Preference**: Regular, large, small
- **Privacy Control**: Show/hide from chefs

### **2. Favorite Menu Items**
- Mark items as favorites
- Track order count (how many times ordered)
- Last ordered timestamp
- Custom notes per favorite

### **3. Menu Item Specific Preferences**
- Override global preferences for specific items
- Extra ingredients requests
- Remove ingredients
- Item-specific special instructions

### **4. Chef Access** 🧑‍🍳
- **Cross-restaurant visibility**: Chefs see preferences from customers who ordered at ANY restaurant
- **Privacy respected**: Only visible if customer allows
- **Allergy warnings**: Highlighted for safety
- **Dietary restrictions**: Clear indicators
- **Order-specific view**: See preferences relevant to current order

---

## 📊 System Architecture

### **Entity Relationships**

```
Customer (User)
    ↓
    ├── CustomerPreference (1:1) - Global preferences
    │       ├── Allergies (1:N)
    │       └── Dislikes (1:N)
    ├── FavoriteMenuItem (1:N) - Favorite items across restaurants
    └── MenuItemPreference (1:N) - Item-specific preferences
```

---

## 🔄 Complete User Flows

### **Flow 1: Customer Sets Global Preferences**

```http
PUT /api/preferences/my
{
  "spiceLevel": "HOT",
  "cookingPreference": "WELL_DONE",
  "isVegetarian": true,
  "allergies": ["Peanuts", "Shellfish"],
  "dislikes": ["Cilantro", "Blue Cheese"],
  "specialInstructions": "Please make food less oily",
  "portionPreference": "Large",
  "visibleToChefs": true
}
```

**Response:**
```json
{
  "success": true,
  "message": "Preferences updated successfully",
  "data": {
    "id": 1,
    "customerId": 123,
    "spiceLevel": "HOT",
    "cookingPreference": "WELL_DONE",
    "isVegetarian": true,
    "allergies": ["Peanuts", "Shellfish"],
    "dislikes": ["Cilantro", "Blue Cheese"],
    "visibleToChefs": true
  }
}
```

---

### **Flow 2: Customer Marks Favorite**

```http
POST /api/preferences/favorites
{
  "menuItemId": 42,
  "notes": "Best pizza in town!"
}
```

**Automatic tracking:**
- When customer orders this item → order count increments
- Last ordered timestamp updates

---

### **Flow 3: Customer Sets Item-Specific Preference**

```http
PUT /api/preferences/menu-items
{
  "menuItemId": 42,
  "spiceLevel": "EXTRA_HOT",  // Override global preference
  "extraIngredients": "extra cheese, extra jalapeños",
  "removeIngredients": "onions, bell peppers",
  "specialInstructions": "Well done crust"
}
```

---

### **Flow 4: Chef Views Customer Preferences**

When preparing an order:

```http
GET /api/kitchen/orders/789/preferences
```

**Response:**
```json
{
  "orderId": 789,
  "orderNumber": "ORD20240120001",
  "customerId": 123,
  "customerName": "John Doe",
  "globalPreferences": {
    "spiceLevel": "HOT",
    "isVegetarian": true,
    "allergies": ["Peanuts", "Shellfish"]
  },
  "itemPreferences": [
    {
      "menuItemId": 42,
      "menuItemName": "Margherita Pizza",
      "spiceLevel": "EXTRA_HOT",
      "extraIngredients": "extra cheese",
      "removeIngredients": "onions"
    }
  ],
  "allergyWarnings": ["⚠️ Peanuts", "⚠️ Shellfish"],
  "dietaryRestrictions": ["Vegetarian"],
  "specialInstructions": [
    "Please make food less oily",
    "Well done crust"
  ]
}
```

---

## 🧑‍🍳 Chef-Specific Features

### **Kitchen Dashboard**

**Get Active Orders with Preferences:**
```http
GET /api/kitchen/orders/with-preferences?status=PREPARING
```

**Response includes:**
- Order details
- Customer preferences (if visible)
- Allergy warnings highlighted
- Dietary restrictions
- Item-specific customizations

---

### **Dietary Alerts**

```http
GET /api/kitchen/dietary-alerts
```

Returns only orders with:
- Allergies
- Dietary restrictions (vegetarian, vegan, etc.)

**Example Use Case:**
Chef can quickly see which orders need special attention for allergies.

---

### **Customer Preference Privacy**

```java
if (customer.preferences.visibleToChefs == false) {
    // Preferences hidden
    return null;
}
```

**Privacy Controls:**
- Customer controls visibility via `visibleToChefs` flag
- Defaults to `true` (visible)
- Chef sees "Customer preferences are private" if hidden

---

## 📋 API Endpoints

### **Customer Endpoints**

```
# Global Preferences
GET    /api/preferences/my                    # Get my preferences
PUT    /api/preferences/my                    # Update preferences
GET    /api/preferences/my/summary            # Preference summary

# Favorites
POST   /api/preferences/favorites             # Add favorite
GET    /api/preferences/favorites             # List favorites
GET    /api/preferences/favorites/restaurant/{id}  # By restaurant
DELETE /api/preferences/favorites/{menuItemId}      # Remove favorite

# Menu Item Preferences
PUT    /api/preferences/menu-items            # Set item preference
GET    /api/preferences/menu-items            # List item preferences
DELETE /api/preferences/menu-items/{id}       # Remove preference
```

### **Chef Endpoints**

```
# Kitchen Views
GET    /api/kitchen/orders/active             # Active kitchen orders
GET    /api/kitchen/orders/{id}/preferences   # Order preferences
GET    /api/kitchen/dietary-alerts            # Orders with allergies/restrictions
GET    /api/kitchen/orders/with-preferences   # Orders enriched with preferences

# Customer Preferences
GET    /api/preferences/customer/{customerId}/menu-item/{menuItemId}
                                               # Get customer preferences for item
```

---

## 💡 Preference Types Explained

### **Spice Levels**
- `NONE` - No spice at all
- `MILD` - Little spice
- `MEDIUM` - Normal spice
- `HOT` - Very spicy
- `EXTRA_HOT` - Maximum spice

### **Cooking Preferences**
- `RARE` - For meats (red center)
- `MEDIUM_RARE` - Slightly cooked center
- `MEDIUM` - Pink center
- `MEDIUM_WELL` - Mostly cooked
- `WELL_DONE` - Fully cooked
- `CRISPY` - For fried items
- `SOFT` - For breads/pastries
- `AL_DENTE` - For pasta

### **Temperature Preferences**
- `COLD` - Prefer cold
- `ROOM_TEMP` - Room temperature
- `HOT` - Prefer hot
- `EXTRA_HOT` - Very hot

---

## 🔐 Privacy & Security

### **Privacy Control**

Customers can hide preferences:
```json
{
  "visibleToChefs": false  // Preferences hidden from all chefs
}
```

### **Anonymization**

In chef view, customer name can be anonymized:
```json
{
  "customerName": "Customer #123"  // Instead of real name
}
```

### **Cross-Restaurant Access**

**How it works:**
1. Customer sets preferences once (stored globally)
2. Preferences linked to customer account (not restaurant)
3. When customer orders at **any restaurant**, chef can see preferences
4. Helps customer get consistent food preparation across restaurants

**Example:**
- Customer orders at Restaurant A: "Extra spicy, no onions"
- Later orders at Restaurant B: Chef sees same preferences
- Consistent experience for customer

---

## 📊 Analytics & Insights

### **View: Popular Favorite Items**
```sql
SELECT * FROM popular_favorite_items
ORDER BY favorite_count DESC
LIMIT 10;
```

### **View: Customer Dietary Summary**
```sql
SELECT * FROM customer_dietary_summary
WHERE has_allergies = TRUE;
```

### **View: Restaurant Preference Stats**
```sql
SELECT * FROM restaurant_preference_stats
WHERE restaurant_id = 100;
```

Shows:
- Total customers with favorites
- Vegetarian/vegan customer counts
- Customers who prefer spicy food
- Gluten-free customers

---

## 🔄 Automatic Order Tracking

When order is completed:

```java
@Transactional
public void completeOrder(Order order) {
    // ... mark order as completed ...
    
    // Automatically update favorite counts
    for (OrderItem item : order.getOrderItems()) {
        preferenceService.incrementFavoriteOrderCount(
            order.getCustomerId(), 
            item.getMenuItemId()
        );
    }
}
```

**Tracks:**
- How many times customer ordered favorite
- Last time favorite was ordered
- Helps identify truly favorite items

---

## 📱 Frontend Implementation Tips

### **Customer Preference Form**

```javascript
// Spice level selector
<select name="spiceLevel">
  <option value="NONE">No Spice</option>
  <option value="MILD">Mild</option>
  <option value="MEDIUM">Medium</option>
  <option value="HOT">Hot 🌶️</option>
  <option value="EXTRA_HOT">Extra Hot 🌶️🌶️</option>
</select>

// Dietary restrictions checkboxes
<input type="checkbox" name="isVegetarian" /> Vegetarian
<input type="checkbox" name="isVegan" /> Vegan
<input type="checkbox" name="isGlutenFree" /> Gluten-Free

// Allergies (dynamic list)
<input type="text" placeholder="Add allergy" />
<ul id="allergies">
  <li>Peanuts <button>Remove</button></li>
  <li>Shellfish <button>Remove</button></li>
</ul>
```

---

### **Favorite Button**

```javascript
async function toggleFavorite(menuItemId) {
  const isFavorite = await checkIsFavorite(menuItemId);
  
  if (isFavorite) {
    await removeFavorite(menuItemId);
    button.innerHTML = '♡ Add to Favorites';
  } else {
    await addFavorite(menuItemId);
    button.innerHTML = '❤️ Favorited';
  }
}
```

---

### **Chef Kitchen View**

```javascript
// Display preferences with order
function renderOrderCard(order, preferences) {
  return `
    <div class="order-card">
      <h3>Order #${order.orderNumber}</h3>
      
      ${preferences.allergyWarnings.length > 0 ? `
        <div class="allergy-warning">
          ⚠️ ALLERGIES: ${preferences.allergyWarnings.join(', ')}
        </div>
      ` : ''}
      
      ${preferences.dietaryRestrictions.length > 0 ? `
        <div class="dietary-info">
          🌱 ${preferences.dietaryRestrictions.join(', ')}
        </div>
      ` : ''}
      
      <div class="preferences">
        <span>🌶️ Spice: ${preferences.globalPreferences.spiceLevel}</span>
        <span>🔥 Cooking: ${preferences.globalPreferences.cookingPreference}</span>
      </div>
      
      ${renderOrderItems(order.items, preferences.itemPreferences)}
    </div>
  `;
}
```

---

## ✅ Best Practices

### **For Customers:**
1. Set global preferences once
2. Mark favorites as you discover them
3. Add allergies for safety
4. Set item-specific preferences for customizations
5. Keep preferences updated

### **For Restaurants:**
1. Train chefs to check preferences before cooking
2. Highlight allergy warnings in kitchen
3. Respect customer privacy settings
4. Use preferences to improve customer experience
5. Track popular items via favorites

### **For Developers:**
1. Always check `visibleToChefs` flag
2. Anonymize customer data in chef view
3. Highlight allergies prominently
4. Cache preference data for performance
5. Update favorite counts on order completion

---

## 🚀 Production Features

✅ Global customer preferences  
✅ Cross-restaurant preference sharing  
✅ Privacy controls  
✅ Favorite item tracking  
✅ Item-specific customizations  
✅ Automatic order count tracking  
✅ Chef-accessible views  
✅ Allergy warnings  
✅ Dietary restriction tracking  
✅ Multi-level preference hierarchy  
✅ Preference analytics  
✅ Database views for reporting  

---

## 🎯 Next Steps

1. **Set up customer preferences** in user profile
2. **Train kitchen staff** on checking preferences
3. **Implement allergy warnings** in kitchen display
4. **Test cross-restaurant** preference sharing
5. **Monitor analytics** for popular items
6. **Gather feedback** from customers and chefs

Ready for production! 🚀
