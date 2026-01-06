export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/graphql',
  kycApiUrl: 'http://localhost:8083/api/v1/kyc',
  keycloak: {
    url: 'http://localhost:8092',
    realm: 'ebanking-realm',
    clientId: 'ebanking-client',
  },
};