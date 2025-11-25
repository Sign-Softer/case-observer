/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  
  // Proxy backend requests when accessed directly (not through nginx)
  // This allows the app to work when accessed on localhost:3000
  async rewrites() {
    // Backend URL - use host.docker.internal when running in Docker,
    // or localhost when running locally
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
