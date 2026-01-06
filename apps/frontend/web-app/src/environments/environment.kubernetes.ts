export const environment = {
  production: true,
  apiUrl: (window as any).env?.apiUrl || 'https://bank-api.h4k5.net/graphql',
  kycApiUrl: (window as any).env?.kycApiUrl || 'https://bank-api.h4k5.net/api/v1/kyc',
  keycloak: {
    url: (window as any).env?.keycloakUrl || 'https://bank-auth.h4k5.net',
    realm: (window as any).env?.keycloakRealm || 'ebanking-realm',
    clientId: (window as any).env?.keycloakClientId || 'ebanking-client',
  },
};