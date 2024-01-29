# Use an official Node runtime as the base image
FROM node:18 as build

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and package-lock.json files
COPY package*.json ./

# Install Angular CLI globally
RUN npm install -g @angular/cli

# Install dependencies
RUN npm install

# Copy the entire project to the container
COPY . .

# Build the Angular app in production mode
RUN ng build

# Use Nginx to serve the Angular app in production
FROM nginx:alpine

# Copy the built Angular app from the 'build' stage
COPY --from=build /app/dist/* /usr/share/nginx/html/

# Expose port 80 to the outside world
EXPOSE 80

# CMD instruction to start Nginx when the container starts
CMD ["nginx", "-g", "daemon off;"]
