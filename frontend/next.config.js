/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  
  // Next.js rewrites handle server-side requests (SSR, API routes)
  // Client-side requests use NEXT_PUBLIC_API_URL to go through nginx
  // This allows both server-side and client-side requests to work correctly
  async rewrites() {
    // Backend URL for server-side requests (from inside Docker container)
    const backendUrl = process.env.BACKEND_URL || 'http://host.docker.internal:8080';
    
    return [
      {
        source: '/auth/:path*',
        destination: `${backendUrl}/auth/:path*`,
      },
      {
        source: '/api/:path*',
        destination: `${backendUrl}/api/:path*`,
      },
      {
        source: '/actuator/:path*',
        destination: `${backendUrl}/actuator/:path*`,
      },
    ];
  },
};

module.exports = nextConfig;
