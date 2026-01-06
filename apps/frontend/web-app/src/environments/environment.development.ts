export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/graphql',
  paymentApiUrl: 'http://localhost:8085/api/payments',
  accountApiUrl: 'http://localhost:8084/api/accounts',
  keycloak: {
    url: 'http://localhost:8092',
    realm: 'ebanking-realm',
    clientId: 'ebanking-client',
  },
};
