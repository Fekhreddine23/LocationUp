export const environment = {
  production: true,
  apiUrl: 'https://locationup-backend-1.onrender.com',
  useMockOffers: false,
  stripe: {
    // Injectée au build via Docker/Render
    publishableKey: "pk_test_51SVuyARx2Bs8g50PT5eqPzDE701s24jRU4tRyHKJjEysHPs2DCFLIu8epXVQoI5quIlbcXJ0xEENWJ40qBoWv3cA00Rr8nVzKO"
  }
};
