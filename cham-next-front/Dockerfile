FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --no-audit --fund=false --legacy-peer-deps

COPY . .

# 어떤 env 파일을 쓸지 선택 (기본: production)
ARG ENV_FILE=.env.production

# 같은 파일이면 복사 안 함
RUN if [ "$ENV_FILE" != ".env.production" ]; then \
      cp "$ENV_FILE" .env.production ; \
    fi

ENV NODE_ENV=production

RUN npm run build


FROM node:20-alpine

WORKDIR /app

COPY --from=builder /app ./

ENV NODE_ENV=production
ENV PORT=3000

EXPOSE 3000

CMD ["npm", "run", "start"]
