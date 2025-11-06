# Frontend Setup Guide

## Architecture

```
Client → Nginx (port 80) → Frontend (port 3000) + Backend API (port 8080)
```

- **Nginx** acts as reverse proxy on port 80
- **Frontend** (Next.js) runs on port 3000 (internal)
- **Backend** (Spring Boot) runs on port 8080 (internal)
- All requests go through Nginx for unified access

## Quick Start

### Option 1: Using Docker Compose (Recommended)

```bash
# From project root
docker-compose -f docker-compose.dev.yml up

# Access application at http://localhost
```

### Option 2: Local Development

```bash
# Install dependencies (requires Node.js 20+)
cd frontend
npm install

# Start development server
npm run dev

# Access at http://localhost:3000
# Note: Backend should be running on port 8080
```

## Configuration

### Environment Variables

- `NEXT_PUBLIC_API_URL` - Set to `http://localhost/api` when behind Nginx
- `NODE_ENV` - Set to `development` for dev mode

### Nginx Routing

- `/` → Frontend (Next.js)
- `/api/*` → Backend API
- `/auth/*` → Backend Auth endpoints
- `/_next/webpack-hmr` → Frontend WebSocket (hot reload)

## Next Steps

After setup, you can:
1. Install additional dependencies: `npm install <package>`
2. Add new pages in `app/` directory
3. Create API client in `lib/api/`
4. Add components and styles

