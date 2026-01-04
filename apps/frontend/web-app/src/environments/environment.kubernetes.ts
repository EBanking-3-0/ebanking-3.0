export const environment = {
  production: true, // Assuming Kubernetes deployment is for production-like environments
  apiUrl: 'https://bank-api.h4k5.net/graphql',
  keycloak: {
    url: 'https://bank-auth.h4k5.net',
    realm: 'ebanking-realm',
    clientId: 'ebanking-client',
  },
};
