export const environment = {
  production: true,
  apiUrl: 'https://locationup-backend-1.onrender.com',
  useMockOffers: false,
  stripe: {
    // Injectée au build via Docker/Render
    publishableKey: 'pk_test_placeholder'
  }
};
