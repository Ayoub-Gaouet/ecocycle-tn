# ecocycle-tn

EcoCycle TN - Plateforme d'economie circulaire (Spring Boot + DevOps).

## EC-2 - Inscription et authentification des utilisateurs

Implemented endpoints:

- `POST /api/auth/register`
- `POST /api/auth/login`

The auth flow includes:

- email and password validation
- BCrypt password hashing
- duplicate email rejection with HTTP 400
- invalid login rejection with HTTP 401
- JWT HS256 token generation with a one-hour expiration
- JWT claims: `sub` email and `role`
- stateless Spring Security with a JWT filter

## Database

The application uses MariaDB. By default the JDBC URL creates the database if
the configured user has `CREATE` privileges.

If your MariaDB user cannot create databases, create it manually before
starting the app:

```sql
CREATE DATABASE ecocycle_tn CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Default datasource values:

- `DB_URL=jdbc:mariadb://localhost:3306/ecocycle_tn?createDatabaseIfNotExist=true`
- `DB_USERNAME=root`
- `DB_PASSWORD=`

Run locally:

```bash
./mvnw spring-boot:run
```

Run tests:

```bash
./mvnw test
```

For production, set `JWT_SECRET` to a secret with at least 32 bytes of entropy.
