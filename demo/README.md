# E-Commerce System - Backend

## 📋 Prerequisites

- Java 21+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.9+

## 🚀 Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/hoangdinhdung05/E-Commerce.git
cd E-Commerce/demo
```

### 2. Setup Environment Variables

Copy the example environment file and configure it:

```bash
cp .env.example .env
```

Edit `.env` and fill in your actual values:

```env
# Database
DB_URL=jdbc:mysql://localhost:3306/demo-system
DB_USERNAME=root
DB_PASSWORD=your_password

# JWT Keys (IMPORTANT: Generate new keys for production!)
JWT_ACCESS_KEY=your_base64_key_here
JWT_REFRESH_KEY=your_base64_key_here

# Mail Configuration
MAIL_USERNAME=your_email@gmail.com
MAIL_PASSWORD=your_app_password
```

**🔐 Generate JWT Keys:**

```bash
# For Access Key
openssl rand -base64 64

# For Refresh Key
openssl rand -base64 64
```

### 3. Create Database

```sql
CREATE DATABASE `demo-system` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. Run the Application

**Development Mode:**
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Production Mode:**
```bash
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### 5. Access the Application

- **API Base URL:** http://localhost:8080/api
- **Swagger UI:** http://localhost:8080/swagger-ui.html (dev only)
- **API Docs:** http://localhost:8080/v3/api-docs

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear layer separation:

```
┌─────────────────────────────────────┐
│      Presentation Layer (API)      │  ← Controllers, DTOs
├─────────────────────────────────────┤
│   Application Layer (Use Cases)    │  ← Business Orchestration
├─────────────────────────────────────┤
│      Domain Layer (Core)            │  ← Entities, Business Logic
├─────────────────────────────────────┤
│   Infrastructure Layer (Tech)       │  ← DB, Redis, RabbitMQ
└─────────────────────────────────────┘
```

### Project Structure

```
src/main/java/com/training/demo/
├── config/                  # Configuration classes
├── controller/              # REST API Controllers
├── dto/                     # Data Transfer Objects
│   ├── request/
│   └── response/
├── entity/                  # JPA Entities
├── exception/               # Custom Exceptions
│   └── DomainException.java # Base exception with error codes
├── mapper/                  # Entity ↔ DTO Mappers
├── repository/              # Data Access Layer
├── security/                # Security & JWT
├── service/                 # Business Logic
│   ├── base/
│   │   └── BaseService.java # Reusable base service
│   └── impl/
├── utils/
│   ├── constants/           # Constants (API, Security, Validation)
│   ├── enums/               # Enumerations
│   └── helper/              # Helper utilities
└── DemoApplication.java
```

## 🔧 Configuration

### Profiles

The application supports multiple profiles:

- **dev** - Development (verbose logging, Swagger enabled)
- **staging** - Staging environment
- **prod** - Production (minimal logging, Swagger disabled)

Activate profile via environment variable:

```bash
export SPRING_PROFILES_ACTIVE=dev
```

Or via command line:

```bash
java -jar app.jar --spring.profiles.active=prod
```

### Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `DB_URL` | Database connection URL | ✅ | - |
| `DB_USERNAME` | Database username | ✅ | - |
| `DB_PASSWORD` | Database password | ✅ | - |
| `JWT_ACCESS_KEY` | JWT access token secret | ✅ | - |
| `JWT_REFRESH_KEY` | JWT refresh token secret | ✅ | - |
| `JWT_EXPIRY_MINUTES` | Access token expiry | ❌ | 15 |
| `JWT_EXPIRY_DAY` | Refresh token expiry | ❌ | 14 |
| `REDIS_HOST` | Redis host | ❌ | localhost |
| `REDIS_PORT` | Redis port | ❌ | 6379 |
| `MAIL_USERNAME` | SMTP username | ✅ | - |
| `MAIL_PASSWORD` | SMTP password | ✅ | - |

## 📚 API Documentation

### Authentication Endpoints

```
POST   /api/auth/register       # Register new user
POST   /api/auth/login          # Login
POST   /api/auth/logout         # Logout
POST   /api/auth/refresh-token  # Refresh access token
POST   /api/auth/active         # Activate account with OTP
```

### User Endpoints

```
GET    /api/users               # Get all users (Admin)
GET    /api/users/{id}          # Get user by ID
GET    /api/users/current       # Get current user
PATCH  /api/users/{id}          # Update user
DELETE /api/users/{id}          # Delete user (Admin)
POST   /api/users/change-password  # Change password
```

### Product Endpoints

```
GET    /api/products            # Get all products
GET    /api/products/{id}       # Get product by ID
POST   /api/products/create     # Create product (Admin)
PATCH  /api/products/{id}       # Update product (Admin)
DELETE /api/products/{id}       # Delete product (Admin)
GET    /api/products/search     # Search products
```

## 🔐 Security

### JWT Authentication

- **Access Token:** Short-lived (15 minutes default)
- **Refresh Token:** Long-lived (14 days default)
- Tokens stored in Redis for revocation support

### Password Requirements

- Minimum 8 characters
- Must contain uppercase, lowercase, and numbers
- Hashed with BCrypt (cost factor 10)

## 🧪 Testing

Run tests:

```bash
mvn test
```

Run tests with coverage:

```bash
mvn test jacoco:report
```

Coverage report will be in `target/site/jacoco/index.html`

## 📊 Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

## 🐛 Troubleshooting

### Database Connection Error

```
Error: Could not connect to database
```

**Solution:** Check if MySQL is running and credentials are correct in `.env`

### JWT Token Invalid

```
Error: Invalid token signature
```

**Solution:** Ensure `JWT_ACCESS_KEY` and `JWT_REFRESH_KEY` are properly configured

### Redis Connection Error

```
Error: Could not connect to Redis
```

**Solution:** Start Redis server:

```bash
redis-server
```

## 📝 Development Guidelines

### Naming Conventions

- **Classes:** PascalCase (e.g., `UserService`)
- **Methods:** camelCase (e.g., `getUserById`)
- **Constants:** UPPER_SNAKE_CASE (e.g., `MAX_PAGE_SIZE`)
- **Packages:** lowercase (e.g., `com.training.demo`)

### Git Workflow

1. Create feature branch from `develop`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make changes and commit:
   ```bash
   git add .
   git commit -m "feat: add new feature"
   ```

3. Push and create Pull Request:
   ```bash
   git push origin feature/your-feature-name
   ```

4. After approval, merge to `develop`

### Commit Message Format

Follow Conventional Commits:

- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `refactor:` Code refactoring
- `test:` Adding tests
- `chore:` Maintenance tasks

Example:
```
feat(auth): implement JWT refresh token mechanism

- Add refresh token endpoint
- Store tokens in Redis
- Add token revocation support
```

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📄 License

This project is licensed under the MIT License.

## 👥 Authors

- Hoang Dinh Dung - [@hoangdinhdung05](https://github.com/hoangdinhdung05)

## 🙏 Acknowledgments

- Spring Boot
- Spring Security
- JWT
- MySQL
- Redis
- RabbitMQ
