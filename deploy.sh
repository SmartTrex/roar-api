#!/bin/bash
set -e

echo "1. Забираем свежие обновления..."
git pull origin main

echo "2. Ставим наклейку DEPLOY_ROAR..."
git tag -f DEPLOY_ROAR
git push origin DEPLOY_ROAR -f

echo "3. Переходим в папку Ноды..."
cd node-service

echo "4. Запускаем сервер..."
docker compose up -d --build

echo "===Delo sdelano!==="