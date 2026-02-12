# Swagger Access Fix

## Issue
"Access to localhost was denied" when trying to access Swagger UI

## Solution Applied

Updated `SecurityConfig.java` to permit all Swagger-related endpoints:

```java
.requestMatchers(
    "/api/v1/auth/login",
    "/api/v1/auth/register",
    "/api/v1/auth/refresh-token",
    "/api/v1/auth/health",
    "/swagger-ui.html",      // Added
    "/swagger-ui/**",
    "/v3/api-docs/**",
    "/v3/api-docs",          // Added
    "/swagger-resources/**",
    "/swagger-resources",    // Added
    "/webjars/**",
    "/configuration/**",     // Added
    "/api-docs/**"          // Added
).permitAll()
```

## Steps to Apply Fix

### 1. Restart the Application

Stop the current running application (Ctrl+C) and restart it:

```bash
mvn spring-boot:run
```

Or if using IntelliJ:
- Stop the application (Red square button)
- Run again (Green play button)

### 2. Clear Browser Cache

Sometimes browsers cache the 403 error. Try:
- **Chrome/Edge**: Ctrl+Shift+Delete → Clear cached images and files
- **Firefox**: Ctrl+Shift+Delete → Cached Web Content
- Or use **Incognito/Private mode**

### 3. Access Swagger UI

After restarting, try these URLs:

1. **Primary URL**: http://localhost:8080/swagger-ui.html
2. **Alternative URL**: http://localhost:8080/swagger-ui/index.html
3. **API Docs**: http://localhost:8080/v3/api-docs

## Testing Without Swagger

If Swagger still doesn't work, you can test the API directly:

### Using cURL (Command Line)

```bash
# Register a new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test@example.com",
    "phoneNumber": "+8801712345678",
    "password": "TestPass123",
    "restaurantId": null
  }'
```

### Using PowerShell

```powershell
$body = @{
    fullName = "Test User"
    email = "test@example.com"
    phoneNumber = "+8801712345678"
    password = "TestPass123"
    restaurantId = $null
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### Using Postman

1. **Method**: POST
2. **URL**: `http://localhost:8080/api/v1/auth/register`
3. **Headers**:
   - Content-Type: application/json
4. **Body** (raw JSON):
```json
{
  "fullName": "Test User",
  "email": "test@example.com",
  "phoneNumber": "+8801712345678",
  "password": "TestPass123",
  "restaurantId": null
}
```

## Verify Security Configuration

Check if the application started successfully:

```bash
# Check logs
tail -f logs/rms-application.log

# Look for this line:
# "Started RestaurantManagementSystemApplication in X seconds"
```

## Alternative: Disable Security Temporarily (For Testing Only)

If you want to test without security temporarily, you can create a test profile:

Create `src/main/resources/application-test.yml`:

```yaml
spring:
  security:
    enabled: false
```

Then run with:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

**⚠️ WARNING**: Never use this in production!

## Common Issues

### Issue 1: Port Already in Use
```
Port 8080 is already in use
```

**Solution**: Kill the existing process or use a different port:
```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Or change port in application.yml
server:
  port: 8081
```

### Issue 2: Database Connection Failed
```
Unable to connect to database
```

**Solution**: Ensure PostgreSQL is running:
```bash
# Check if PostgreSQL is running
# Windows: Check Services
# Or start it manually
```

### Issue 3: Redis Connection Failed (Optional)
If you see Redis errors but don't need Redis:

Comment out Redis configuration in `application.yml`:
```yaml
#  redis:
#    host: localhost
#    port: 6379
```

## Verification Checklist

After restarting, verify:

- [ ] Application started successfully (check logs)
- [ ] No errors in console
- [ ] Can access http://localhost:8080/api/v1/auth/health
- [ ] Can access http://localhost:8080/swagger-ui.html
- [ ] Can register a user via POST request

## Quick Test Script

Save as `test-swagger-access.ps1`:

```powershell
# Test if application is running
$health = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/health" -Method Get
Write-Host "Health Check: $($health.message)" -ForegroundColor Green

# Test if Swagger is accessible
try {
    $swagger = Invoke-WebRequest -Uri "http://localhost:8080/swagger-ui.html" -Method Get
    Write-Host "Swagger UI: Accessible (Status: $($swagger.StatusCode))" -ForegroundColor Green
} catch {
    Write-Host "Swagger UI: Not accessible" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
}

# Test registration endpoint
$body = @{
    fullName = "Test User"
    email = "test$(Get-Date -Format 'yyyyMMddHHmmss')@example.com"
    phoneNumber = "+8801712345678"
    password = "TestPass123"
    restaurantId = $null
} | ConvertTo-Json

try {
    $register = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/register" `
        -Method Post `
        -ContentType "application/json" `
        -Body $body
    Write-Host "Registration: Success" -ForegroundColor Green
    Write-Host "User: $($register.data.user.email)"
} catch {
    Write-Host "Registration: Failed" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)"
}
```

Run with: `powershell -File test-swagger-access.ps1`

## Need More Help?

If Swagger still doesn't work after restarting:

1. Check the logs: `logs/rms-application.log`
2. Look for any security-related errors
3. Verify the application is running on port 8080
4. Try accessing the health endpoint first: http://localhost:8080/api/v1/auth/health

## Summary

✅ Updated SecurityConfig to permit Swagger endpoints
✅ Restart application to apply changes
✅ Clear browser cache or use incognito mode
✅ Access http://localhost:8080/swagger-ui.html
