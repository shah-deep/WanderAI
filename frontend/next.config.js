/** @type {import('next').NextConfig} */
const nextConfig = {
  webpack: (config, { isServer }) => {
    // Only apply this on the client side
    if (!isServer) {
      config.resolve.fallback = {
        ...config.resolve.fallback,
        fs: false,  // Provides an empty module for fs
        path: false,
        // net: false, // Provides an empty module for net
        // tls: false, // Provides an empty module for tls
        // dns: false, // Provides an empty module for dns
        // "utf-8-validate": false,
        // "bufferutil": false,
      };
    }
    return config;
  },
};

module.exports = nextConfig;