const apiUrl = (window as any).env?.apiUrl || 'https://bank-api.h4k5.net/graphql';

export const environment = {
  production: true,
  apiUrl: apiUrl,
  apiRestUrl: (window as any).env?.apiRestUrl || apiUrl.replace(/\/graphql$/, ''),
  accountApiUrl:
    (window as any).env?.accountApiUrl ||
    ((window as any).env?.apiRestUrl || apiUrl.replace(/\/graphql$/, '')) + '/api/accounts',
  paymentApiUrl:
    (window as any).env?.paymentApiUrl ||
    ((window as any).env?.apiRestUrl || apiUrl.replace(/\/graphql$/, '')) + '/api/payments',
  keycloak: {
    url: (window as any).env?.keycloakUrl || 'https://bank-auth.h4k5.net',
    realm: (window as any).env?.keycloakRealm || 'ebanking-realm',
    clientId: (window as any).env?.keycloakClientId || 'ebanking-client',
  },
};