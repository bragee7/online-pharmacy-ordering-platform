@echo off
REM =============================================================
REM  Stop the portable MySQL 8 server (graceful shutdown).
REM =============================================================
"C:\Users\Gauth\tools\mysql-8.0.40-winx64\bin\mysqladmin.exe" --protocol=tcp -h 127.0.0.1 -u root -p shutdown 2>nul
if errorlevel 1 (
    echo Could not shut down via root login. Trying to close the mysqld process...
    taskkill /IM mysqld.exe /F >nul 2>nul
)
echo done.