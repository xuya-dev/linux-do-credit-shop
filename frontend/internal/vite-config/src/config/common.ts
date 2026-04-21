import type { UserConfig } from 'vite';

async function getCommonConfig(): Promise<UserConfig> {
  return {
    build: {
      chunkSizeWarningLimit: 500,
      reportCompressedSize: true,
      sourcemap: false,
    },
  };
}

export { getCommonConfig };
