export const checkBackendHealth = async () => {
  try {
    const backendUrl = process.env.NEXT_PUBLIC_BACKEND_URL || 'http://localhost:8080';
    const response = await fetch(`${backendUrl}/health`);
    return response.ok;
  } catch (error) {
    console.warn('Health check failed:', error);
    return false;
  }
};
