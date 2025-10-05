#!/bin/bash
set -euo pipefail

KEYCLOAK_URL="http://localhost:8081"
REALM="portfolio"

CLIENT_ID="app-listing-service"
CLIENT_SECRET="xUI78nB9oXwHhHfQumAizc9wt8RyEaVT"
PASSWORD="password"

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "Usage: $0 <username> [decode]"
  exit 1
fi

USER_NAME="$1"
DECODE="${2:-}"

echo "🔐 Requesting token for user: $USER_NAME"

# Request access token
RAW_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/$REALM/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=$CLIENT_ID" \
  -d "client_secret=$CLIENT_SECRET" \
  -d "grant_type=password" \
  -d "username=$USER_NAME" \
  -d "password=$PASSWORD")

USER_TOKEN=$(echo "$RAW_RESPONSE" | jq -r '.access_token // empty')

if [[ -z "$USER_TOKEN" ]]; then
  echo "❌ Failed to retrieve user token. Full response:"
  echo "$RAW_RESPONSE" | jq .
  exit 1
fi

# Print token
echo
echo "✅ Access Token"
echo "$USER_TOKEN"

# Optional: decode payload
if [[ "$DECODE" == "decode" ]]; then
  echo
  echo "✅ Token payload (decoded) 👇"
  echo "$USER_TOKEN" | cut -d. -f2 | base64 --decode -i 2>/dev/null | jq .
fi
