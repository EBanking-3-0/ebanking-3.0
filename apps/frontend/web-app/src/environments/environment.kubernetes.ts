export const environment = {
  production: true, // Assuming Kubernetes deployment is for production-like environments
  apiUrl: 'http://graphql-gateway-microservice:8081/graphql',
  keycloak: {
    url: 'http://ebanking-infra-keycloak:8080',
    realm: 'ebanking-realm',
    clientId: 'ebanking-client',
  },
};
