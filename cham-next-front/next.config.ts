const nextConfig = {
   reactStrictMode: true,

   compiler: {
      styledComponents: true
   },

   basePath: '/feedback',
   assetPrefix: '/feedback',

   typescript: {
      ignoreBuildErrors: true,
   },

   async rewrites() {
      return [
         {
            source: '/cham/:path*',
            destination: `${process.env.NEXT_PUBLIC_API_URL}/cham/:path*`,
         },
      ];
   },

   env: {
      NEXT_PUBLIC_SITE_URL: process.env.NEXT_PUBLIC_SITE_URL,
      NEXT_PUBLIC_API_URL: process.env.NEXT_PUBLIC_API_URL,
   },
};

export default nextConfig;
