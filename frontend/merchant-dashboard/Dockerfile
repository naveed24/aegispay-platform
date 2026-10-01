FROM node:24-alpine AS build
WORKDIR /app
COPY frontend/merchant-dashboard/package*.json ./
RUN npm install
COPY frontend/merchant-dashboard/ ./
RUN npm run build

FROM nginx:1.29-alpine
COPY frontend/merchant-dashboard/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
