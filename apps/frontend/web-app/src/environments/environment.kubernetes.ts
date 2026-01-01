export const environment = {
  production: true, // Assuming Kubernetes deployment is for production-like environments
  apiUrl: 'https://api.ebanking.local/graphql',
  keycloak: {
    url: 'https://auth.ebanking.local',
    realm: 'ebanking-realm',
    clientId: 'ebanking-client',
  },
};
