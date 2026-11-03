@echo off
chcp 65001 >nul
echo ========================================================
echo   BrewLite Coffee - Khởi Động Hệ Thống Phát Triển
echo ========================================================
echo.

echo [1/2] Đang khởi động các dịch vụ Docker (MySQL, Redis, Frontend Node.js)...
cd /d "%~dp0"
docker compose up -d

if %errorlevel% neq 0 (
    echo.
    echo [LỖI] Không thể khởi động Docker! Hãy chắc chắn Docker Desktop đang chạy.
    echo.
    pause
    exit /b %errorlevel%
)

echo.
echo [OK] Các dịch vụ Docker đã sẵn sàng:
echo      - MySQL Database:  localhost:3306
echo      - Redis Cache:     localhost:6379
echo      - Frontend (Vite): http://localhost:3000
echo.
echo [2/2] Đang khởi động Backend Spring Boot trên cổng 8081...
echo ========================================================
echo.

mvnw.cmd spring-boot:run
