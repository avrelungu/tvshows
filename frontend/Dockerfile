# Multi-stage build for Angular application

# Build stage
FROM node:20-alpine AS build
WORKDIR /app

# Install Angular CLI globally
RUN npm install -g @angular/cli@19

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci

# Copy application source
COPY . .

# Build the Angular application
ARG API_URL=http://localhost:8080
ENV API_URL=$API_URL
RUN npm run build -- --configuration=production

# Runtime stage
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Remove default nginx website
RUN rm -rf ./*

# Copy built application from build stage
COPY --from=build /app/dist/frontend/browser .

# Copy nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Create runtime environment configuration script
RUN echo '#!/bin/sh' > /docker-entrypoint.sh && \
    echo 'envsubst "$$API_URL" < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf' >> /docker-entrypoint.sh && \
    echo 'nginx -g "daemon off;"' >> /docker-entrypoint.sh && \
    chmod +x /docker-entrypoint.sh

# Copy nginx template
COPY nginx.conf /etc/nginx/conf.d/default.conf.template

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost || exit 1

# Start nginx
CMD ["/docker-entrypoint.sh"]
