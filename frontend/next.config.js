/** @type {import('next').NextConfig} */
const nextConfig = {
  output: 'export',  // Enable static exports
  basePath: '/trip-planner', // Replace with your repo name
  images: {
    unoptimized: true,
  },
  webpack: (config, { isServer }) => {
    // Only apply this on the client side
    if (!isServer) {
      config.resolve.fallback = {
        ...config.resolve.fallback,
        fs: false,  // Provides an empty module for fs
        path: false,
      };
    }
    return config;
  },
};

module.exports = nextConfig;