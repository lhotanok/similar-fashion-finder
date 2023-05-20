FROM node:19-alpine

WORKDIR /app

COPY package*.json ./

RUN npm ci --production

COPY . .

RUN npm run build

EXPOSE 3000

CMD ["npm", "start"]
