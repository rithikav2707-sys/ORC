# ORC Energy Management System

Spring Boot application for a demonstration Organic Rankine Cycle waste-heat facility. Telemetry is manually entered or seeded sample data; this application is not connected to physical sensors.

## Run

Requires Java 17 or later. From the project directory:

```powershell
.\mvnw.cmd spring-boot:run
```

Open `http://localhost:8080` and sign in with:

- Admin: `admin` / `orcAdmin123`
- Accountant: `accountant` / `accountant123`

The default profile uses a persistent local H2 database at `./data/orcdb-demo`. Demo records are inserted when the relevant tables are empty. The H2 console is available at `/dev/h2-console`.

## MySQL

Create the database:

```sql
CREATE DATABASE orc_db;
```

Set the profile and credentials in PowerShell, then run the wrapper:

```powershell
$env:SPRING_PROFILES_ACTIVE = 'mysql'
$env:MYSQL_USERNAME = 'root'
$env:MYSQL_PASSWORD = 'your-local-password'
.\mvnw.cmd spring-boot:run
```

The MySQL profile defaults to `jdbc:mysql://localhost:3306/orc_db`; set `MYSQL_URL` to override it. Hibernate creates and updates tables with `ddl-auto=update`.

## API

- `GET/POST /api/orc/turbines`, `GET/PUT/DELETE /api/orc/turbines/{id}`
- `GET/POST /api/orc/telemetry`, `GET /api/orc/telemetry/{id}`
- CRUD at `/api/contacts` and `/api/products`
- `GET/POST /api/sales-orders`, `GET/POST /api/purchases`
- `POST /api/purchases/{id}/receive`
- `GET/POST /api/customer-invoices`, `GET/POST /api/vendor-bills`, `GET/POST /api/payments`
- `POST /api/inventory/consume`
- `GET /api/dashboard/summary`, `GET /api/dashboard/generation`
- `GET /api/reports/profit-loss`, `/api/reports/balance-sheet`, `/api/reports/budget`, `/api/reports/generation?from=YYYY-MM-DD&to=YYYY-MM-DD`

All API routes require login. API requests may use HTTP Basic credentials or an authenticated session; validation failures return HTTP 400 with a concise error message.

## Tests

```powershell
.\mvnw.cmd test
```

Tests use an isolated in-memory H2 database and do not modify the local demo database.
