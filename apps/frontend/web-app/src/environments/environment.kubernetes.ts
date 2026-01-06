export const environment = {
  production: true,
  apiUrl: (window as any).env?.apiUrl || 'https://bank-api.h4k5.net/graphql',
  apiRestUrl: (window as any).env?.apiRestUrl || 'https://bank-api.h4k5.net', // Gateway proxies this
  keycloak: {
    url: (window as any).env?.keycloakUrl || 'https://bank-auth.h4k5.net',
    realm: (window as any).env?.keycloakRealm || 'ebanking-realm',
    clientId: (window as any).env?.keycloakClientId || 'ebanking-client',
  },
};
