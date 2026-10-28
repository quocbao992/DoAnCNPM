@echo off
echo Starting BrewLite Backend on port 8081...
start "BrewLite Backend (Spring Boot)" cmd /k "cd /d D:\CongNghePhanMem\DoAnCNPM && mvnw.cmd spring-boot:run"

timeout /t 3 /nobreak >nul

echo Starting BrewLite Frontend on port 3000...
start "BrewLite Frontend (React Vite)" cmd /k "cd /d D:\CongNghePhanMem\DoAnCNPM\frontend && npm run dev"

echo [OK] Both Backend and Frontend have been launched in separate windows.
