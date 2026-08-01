# Portable local MySQL 8

This repo targets **MySQL 8** persistence. If you have no MySQL server installed, these
scripts run a **portable** MySQL 8.0.40 instance whose binaries, config and data live
**outside** the repository so nothing sensitive is committed:

| Piece | Location |
|---|---|
| MySQL binaries | `C:\Users\Gauth\tools\mysql-8.0.40-winx64` |
| Server config | `C:\Users\Gauth\tools\my.ini` |
| Data directory | `C:\Users\Gauth\tools\mysql-data` |

## Usage

```cmd
scripts\db-start.cmd     :: start mysqld + wait until port 3306 answers
scripts\db-stop.cmd      :: graceful shutdown
```

## Databases

| Database | Purpose |
|---|---|
| `online_pharmacy` | Application database (schema created by Flyway on boot) |
| `online_pharmacy_test` | Used by `mvnw test` |

## App user (dev only)

| Field | Value |
|---|---|
| Host | `localhost` / `127.0.0.1` |
| User | `pharmacy` |
| Password | `pharmacyDev2024` |

Override via `DB_USERNAME` / `DB_PASSWORD` environment variables. Never use these
credentials in any real environment.

## First-time initialization (already done on this machine)

```cmd
mysqld --defaults-file=C:\Users\Gauth\tools\my.ini --initialize-insecure
:: then start, then:
CREATE DATABASE online_pharmacy CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE online_pharmacy_test CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'pharmacy'@'localhost' IDENTIFIED BY 'pharmacyDev2024';
CREATE USER 'pharmacy'@'127.0.0.1' IDENTIFIED BY 'pharmacyDev2024';
GRANT ALL PRIVILEGES ON online_pharmacy.* TO 'pharmacy'@'localhost';
GRANT ALL PRIVILEGES ON online_pharmacy.* TO 'pharmacy'@'127.0.0.1';
GRANT ALL PRIVILEGES ON online_pharmacy_test.* TO 'pharmacy'@'localhost';
GRANT ALL PRIVILEGES ON online_pharmacy_test.* TO 'pharmacy'@'127.0.0.1';
```