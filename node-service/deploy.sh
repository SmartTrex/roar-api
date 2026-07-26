#!/bin/bash
echo "1. Pulling fresh code..."
git pull origin main
echo "2. Rebuilding and starting Docker containers..."
docker compose up -d --build