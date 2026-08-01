@echo off
REM =============================================================
REM  Start the portable MySQL 8 server used by this capstone.
REM  Datadir and config live OUTSIDE the repository (C:\Users\Gauth\tools).
REM =============================================================
set MYSQL_BIN=C:\Users\Gauth\tools\mysql-8.0.40-winx64\bin
set MYSQL_INI=C:\Users\Gauth\tools\my.ini

netstat -an | findstr :3306 | findstr LISTENING >nul
if %errorlevel%==0 (
    echo MySQL is already running on port 3306.
    exit /b 0
)

if not exist "%MYSQL_BIN%\mysqld.exe" (
    echo Cannot find mysqld.exe at %MYSQL_BIN%
    echo Download MySQL 8.0.40 winx64 zip and extract to C:\Users\Gauth\tools
    exit /b 1
)

start "MySQL80" /B "%MYSQL_BIN%\mysqld.exe" --defaults-file="%MYSQL_INI%"
echo Waiting for MySQL to accept connections on 3306...
timeout /t 8 /nobreak >nul
mysql --protocol=tcp -h 127.0.0.1 -u pharmacy -ppharmacyDev2024 -e "SELECT 'MySQL is up' AS status;" 2>nul
echo MySQL setup complete. Credentials are documented in scripts\README.md and README.md.