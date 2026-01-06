export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/graphql',
  apiRestUrl: 'http://localhost:8083',
  accountApiUrl: 'http://localhost:8084/api/accounts',
  paymentApiUrl: 'http://localhost:8085/api/payments',
  keycloak: {
    url: 'http://localhost:8092',
    realm: 'ebanking-realm',
    clientId: 'ebanking-client',
  },
};
