# Build stage
FROM node:20-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
ARG STRIPE_PUBLIC_KEY=pk_test_placeholder
ENV STRIPE_PUBLIC_KEY=${STRIPE_PUBLIC_KEY}
RUN sed -i "s|publishableKey: '.*'|publishableKey: '${STRIPE_PUBLIC_KEY}'|" \
  src/environments/environment.ts \
  src/environments/environment.prod.ts
RUN npm run build --prod

# Production stage
FROM nginx:alpine
# CORRECTION : Copier depuis le sous-dossier browser
COPY --from=build-stage /app/dist/location-up-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost/ || exit 1
