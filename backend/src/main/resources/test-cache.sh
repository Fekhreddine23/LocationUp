#!/bin/bash

TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MzA0OTI1NSwiZXhwIjoxNzYzMTM1NjU1LCJyb2xlIjoiUk9MRV9BRE1JTiJ9._cp9rUUXIBlYDkJ7C-nuVJQHe7PCyDTbSHS-hDyHPZE"
BASE_URL="http://localhost:8088/api/admin"

echo "🧪 Test du cache Redis - Dashboard Admin"
echo "=========================================="

echo ""
echo "1. Premier appel aux statistiques (sans cache)..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/stats | jq '.totalUsers, .lastUpdated' 2>/dev/null || curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/stats

echo ""
echo "2. Deuxième appel aux statistiques (avec cache)..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/stats | jq '.totalUsers, .lastUpdated' 2>/dev/null || curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/stats

echo ""
echo "3. État du cache..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/cache/status

echo ""
echo "4. Invalidation du cache..."
curl -s -X POST -H "Authorization: Bearer $TOKEN" $BASE_URL/cache/invalidate

echo ""
echo "5. Appel après invalidation..."
curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/stats | jq '.totalUsers, .lastUpdated' 2>/dev/null || curl -s -H "Authorization: Bearer $TOKEN" $BASE_URL/stats