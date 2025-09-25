# Security Integration Guide

## Authentication Flow

### 1. Register User
```bash
POST /auth/register
Content-Type: application/json

{
  "username": "lawyer1",
  "email": "lawyer@example.com", 
  "password": "password123"
}
```

### 2. Login
```bash
POST /auth/login
Content-Type: application/json

{
  "username": "lawyer1",
  "password": "password123"
}
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "role": "USER"
}
```

### 3. Use Access Token
```bash
GET /api/users
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### 4. Refresh Token
```bash
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..."
}
```

## Protected Endpoints

- `/api/users/**` - requires authentication
- `/api/cases` (POST) - requires ROLE_USER
- `/api/cases/{id}/refetch` - requires ROLE_USER  
- `/api/cases/{id}/monitoring/**` - requires ROLE_USER
- `/api/cases/{id}/notification-settings` - requires ROLE_USER

## Open Endpoints

- `/auth/**` - registration/login
- `/api/cases/fetch` - portal data fetch
- `/actuator/**` - health checks

## Frontend Integration

Store tokens in memory (not localStorage for security):
```javascript
// Login
const response = await fetch('/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username, password })
});
const { accessToken, refreshToken } = await response.json();

// Use token
const apiResponse = await fetch('/api/users', {
  headers: { 'Authorization': `Bearer ${accessToken}` }
});
```
