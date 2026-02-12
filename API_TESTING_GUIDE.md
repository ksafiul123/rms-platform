# API Testing Guide - Restaurant Management System

## Issue: "An unexpected error occurred"

### Root Cause
You were using **GET** method, but the register endpoint requires **POST** method.

Error in logs:
```
Request method 'GET' is not supported
```

## ✅ Correct Way to Register

### Method 1: Using Swagger UI (Easiest)

1. Open: http://localhost:8080/swagger-ui.html
2. Navigate to **Authentication** section
3. Click on **POST /api/v1/auth/register**
4. Click **"Try it out"** button
5. Fill in the request body:

```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+8801712345678",
  "password": "MyPass123",
  "restaurantId": null
}
```

6. Click **"Execute"**

### Method 2: Using cURL

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+8801712345678",
    "password": "MyPass123",
    "restaurantId": null
  }'
```

### Method 3: Using Postman

1. **Method**: POST (not GET!)
2. **URL**: `http://localhost:8080/api/v1/auth/register`
3. **Headers**:
   - Key: `Content-Type`
   - Value: `application/json`
4. **Body** (select "raw" and "JSON"):

```json
{
  "fullName": "John Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "+8801712345678",
  "password": "MyPass123",
  "restaurantId": null
}
```

### Method 4: Using JavaScript/Fetch

```javascript
fetch('http://localhost:8080/api/v1/auth/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    fullName: "John Doe",
    email: "john.doe@example.com",
    phoneNumber: "+8801712345678",
    password: "MyPass123",
    restaurantId: null
  })
})
.then(response => response.json())
.then(data => console.log(data))
.catch(error => console.error('Error:', error));
```

## Field Requirements

### Required Fields:
- ✅ **fullName**: 2-100 characters
- ✅ **email**: Valid email format
- ✅ **password**: 8-100 characters, must contain:
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit

### Optional Fields:
- **phoneNumber**: Format: `+[country code][number]` (e.g., `+8801712345678`)
- **restaurantId**: Leave as `null` for customer registration

## Password Requirements

Your password must:
- Be 8-100 characters long
- Contain at least one uppercase letter (A-Z)
- Contain at least one lowercase letter (a-z)
- Contain at least one digit (0-9)

### Valid Password Examples:
- ✅ `MyPass123`
- ✅ `SecurePass1`
- ✅ `Test1234`

### Invalid Password Examples:
- ❌ `password` (no uppercase, no digit)
- ❌ `PASSWORD123` (no lowercase)
- ❌ `MyPassword` (no digit)
- ❌ `Pass1` (too short)

## Expected Success Response

```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
    "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600000,
    "user": {
      "id": 1,
      "email": "john.doe@example.com",
      "fullName": "John Doe",
      "phoneNumber": "+8801712345678",
      "role": "CUSTOMER"
    }
  },
  "timestamp": "2026-02-11T06:45:00"
}
```

## Common Errors

### 1. Method Not Allowed (405)
```json
{
  "success": false,
  "message": "An unexpected error occurred"
}
```
**Solution**: Use POST method, not GET

### 2. Validation Error (400)
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "password": "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
  }
}
```
**Solution**: Fix the validation errors in your request

### 3. Email Already Exists (400)
```json
{
  "success": false,
  "message": "Email already exists"
}
```
**Solution**: Use a different email address

## Other Authentication Endpoints

### Login
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "MyPass123"
}
```

### Refresh Token
```bash
POST /api/v1/auth/refresh-token
Content-Type: application/json

{
  "refreshToken": "your-refresh-token-here"
}
```

### Logout
```bash
POST /api/v1/auth/logout
Content-Type: application/json

{
  "refreshToken": "your-refresh-token-here"
}
```

### Get Current User
```bash
GET /api/v1/auth/me
Authorization: Bearer your-access-token-here
```

## Testing with Swagger UI

The easiest way to test all endpoints is using Swagger UI:

1. **Open**: http://localhost:8080/swagger-ui.html
2. **Browse**: All available endpoints with documentation
3. **Test**: Click "Try it out" on any endpoint
4. **Authenticate**: Use the "Authorize" button to add your token for protected endpoints

## Quick Test Script

Save this as `test-register.sh`:

```bash
#!/bin/bash

curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "Test User",
    "email": "test'$(date +%s)'@example.com",
    "phoneNumber": "+8801712345678",
    "password": "TestPass123",
    "restaurantId": null
  }' | jq
```

Run with: `bash test-register.sh`

## Summary

✅ **Always use POST method for /api/v1/auth/register**
✅ **Include Content-Type: application/json header**
✅ **Provide valid JSON body with required fields**
✅ **Use Swagger UI for easy testing**
