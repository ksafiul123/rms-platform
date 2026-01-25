package com.rms.controller;

import com.rms.security.annotation.RequireAllPermissions;
import com.rms.security.annotation.RequireAnyPermission;
import com.rms.security.annotation.RequirePermission;
import com.rms.security.annotation.RequireRoleLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Example Controller with Permission Annotations
 */
@RestController
@RequestMapping("/api/v1/menu")
@RequiredArgsConstructor
@Tag(name = "Menu Management (Example)", description = "Example of permission-based access")
@SecurityRequirement(name = "bearerAuth")
public class MenuControllerExample {

    /**
     * Example: Single permission required
     */
    @PostMapping
    @RequirePermission(value = "menu:create", resource = "menu")
    @Operation(summary = "Create menu item", description = "Requires menu:create permission")
    public ResponseEntity<String> createMenuItem() {
        return ResponseEntity.ok("Menu item created");
    }

    /**
     * Example: Any of multiple permissions
     */
    @GetMapping
    @RequireAnyPermission(value = {"menu:read", "menu:manage"}, resource = "menu")
    @Operation(summary = "Get menu items", description = "Requires menu:read OR menu:manage")
    public ResponseEntity<String> getMenuItems() {
        return ResponseEntity.ok("Menu items list");
    }

    /**
     * Example: All permissions required
     */
    @PutMapping("/{id}")
    @RequireAllPermissions(value = {"menu:update", "menu:manage"}, resource = "menu")
    @Operation(summary = "Update menu item", description = "Requires menu:update AND menu:manage")
    public ResponseEntity<String> updateMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok("Menu item updated");
    }

    /**
     * Example: Using Spring Security expression with custom evaluator
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionEvaluator.hasPermission('menu:delete')")
    @Operation(summary = "Delete menu item", description = "Requires menu:delete permission")
    public ResponseEntity<String> deleteMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok("Menu item deleted");
    }

    /**
     * Example: Multiple permissions with OR logic
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("@permissionEvaluator.hasAnyPermission('menu:approve', 'menu:manage')")
    @Operation(summary = "Approve menu item", description = "Requires menu:approve OR menu:manage")
    public ResponseEntity<String> approveMenuItem(@PathVariable Long id) {
        return ResponseEntity.ok("Menu item approved");
    }

    /**
     * Example: Role-based with role level check
     */
    @PostMapping("/bulk-import")
    @RequireRoleLevel(value = 3)
    @RequirePermission(value = "menu:bulk_import", resource = "menu")
    @Operation(summary = "Bulk import menu", description = "Requires high-level role and permission")
    public ResponseEntity<String> bulkImportMenu() {
        return ResponseEntity.ok("Bulk import completed");
    }
}
