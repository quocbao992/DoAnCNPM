@echo off
echo ========================================================
echo   BrewLite Coffee - Starting Development Environment
echo ========================================================
echo.

echo [1/2] Starting Docker services (MySQL, Redis, Frontend)...
cd /d "%~dp0"
docker compose up -d

if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Failed to start Docker services!
    echo Please make sure Docker Desktop is running.
    echo.
    pause
    exit /b %errorlevel%
)

echo.
echo [OK] Docker services are running:
echo      - MySQL Database:  localhost:3306
echo      - Redis Cache:     localhost:6379
echo      - Frontend (Vite): http://localhost:3000
echo.
echo [2/2] Starting Spring Boot Backend on port 8081...
echo ========================================================
echo.

call mvnw.cmd spring-boot:run
