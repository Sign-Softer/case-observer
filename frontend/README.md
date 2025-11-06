# Case Observer Frontend

Next.js frontend application for the Case Observer system.

## Architecture

The frontend is accessed through Nginx reverse proxy:
- **Nginx** (port 80) → **Frontend** (port 3000) and **Backend API** (port 8080)
- Frontend makes API calls to `/api/*` which are proxied by Nginx to the backend

## Setup

### Prerequisites
- Node.js 20+ and npm

### Installation

```bash
# Install dependencies
npm install

# Run development server
npm run dev
```

The application will be available at `http://localhost:3000` when running directly, or `http://localhost` when accessed through Nginx.

## Development with Docker

The frontend is configured to run in Docker Compose:

```bash
# Start all services (backend, frontend, nginx, database)
docker-compose -f docker-compose.dev.yml up

# Access application at http://localhost (via Nginx)
```

## Environment Variables

- `NEXT_PUBLIC_API_URL` - Backend API URL (default: `http://localhost/api` when behind Nginx)

## Project Structure

```
frontend/
├── app/              # Next.js App Router pages
│   ├── layout.tsx    # Root layout
│   ├── page.tsx      # Home page
│   └── globals.css   # Global styles
├── package.json      # Dependencies
└── Dockerfile.dev    # Development Docker image
```

