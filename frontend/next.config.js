/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  // API rewrites for development - proxy to backend through nginx
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: '/api/:path*', // Will be handled by nginx
      },
      {
        source: '/auth/:path*',
        destination: '/auth/:path*', // Will be handled by nginx
      },
    ];
  },
};

module.exports = nextConfig;

