# Use an official Node runtime as the base image
FROM node:18

# Set the working directory in the container
WORKDIR /app

# Copy the package.json and package-lock.json files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy the entire project to the container
COPY . .

# Expose port 80 to the outside world
EXPOSE 8089

# CMD instruction to start Nginx when the container starts
CMD ["node", "api.js"]
