# SMTP Setup Guide

To enable email functionality (e.g., for Keycloak email verification), you need to provide a valid SMTP key.

## Local Development

1. Create a `.env` file in the project root (if it doesn't exist).
2. Add your Brevo (or other provider) SMTP key:
   ```env
   BREVO_SMTP_KEY=your_64_character_key_here
   ```

## Keycloak Configuration

The `tools/docker/realm-export.json` file uses the `${BREVO_SMTP_KEY}` placeholder. When importing the realm or running in an environment that supports variable substitution, ensure this variable is set.

If you are manually configuring Keycloak via the UI:

- **Host:** `smtp-relay.brevo.com`
- **Port:** `2525`
- **Authentication:** Enabled
- **StartTLS:** Enabled
- **Password:** Your Brevo SMTP Key
