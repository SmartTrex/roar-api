FROM node:20-alpine

WORKDIR /app

COPY package.json packege-lock.json* ./

RUN npm install --omit =dev

COPY src ./src

EXPOSE 3000

CMD ["npm", "start"]