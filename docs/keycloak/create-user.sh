#!/bin/bash
set -euo pipefail

# This script can be used to create a demo user in keycloak. You've to provide username and email as parameters.
# The password will be hard coded automatically to "password".
#
# ============================================
# CONFIG
# ============================================
KEYCLOAK_URL="http://localhost:8081"
REALM="portfolio"
ADMIN_USER="admin"
ADMIN_PASS="admin"

USER_NAME=${1:-}
USER_EMAIL=${2:-}

if [[ -z "$USER_NAME" || -z "$USER_EMAIL" ]]; then
  echo "Usage: $0 <username> <email>"
  exit 1
fi

# ============================================
# STEP 1: Get admin token
# ============================================
echo "🔑 Getting admin token..."
ADMIN_TOKEN=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASS" \
  -d 'grant_type=password' \
  -d 'client_id=admin-cli' | jq -r .access_token)

if [[ "$ADMIN_TOKEN" == "null" || -z "$ADMIN_TOKEN" ]]; then
  echo "❌ Failed to get admin token. Check admin credentials."
  exit 1
fi

# ============================================
# STEP 2: Create new user
# ============================================
echo "👤 Creating user '$USER_NAME'..."
CREATE_USER_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$KEYCLOAK_URL/admin/realms/$REALM/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"username\": \"$USER_NAME\",
    \"enabled\": true,
    \"emailVerified\": true,
    \"email\": \"$USER_EMAIL\",
    \"credentials\": [{
      \"type\": \"password\",
      \"value\": \"password\",
      \"temporary\": false
    }]
  }")

if [[ "$CREATE_USER_RESPONSE" -ne 201 ]]; then
  echo "⚠️  User creation may have failed (HTTP $CREATE_USER_RESPONSE). Might already exist."
fi

echo "✅ Successfully created user '$USER_NAME'"
