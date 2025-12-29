# syntax=docker/dockerfile:1

FROM nginx:alpine
COPY apps/frontend/web-app/nginx.conf /etc/nginx/conf.d/default.conf
COPY dist/apps/frontend/web-app/browser /usr/share/nginx/html
EXPOSE 80
ENTRYPOINT ["nginx", "-g", "daemon off;"]
