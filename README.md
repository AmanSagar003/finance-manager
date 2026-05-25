# 💰 Personal Finance Manager API

A production-ready RESTful API built with **Kotlin + Spring Boot 3** for managing personal finances, including user registration, JWT authentication, transaction management, savings goals, and financial reports.

---

## 🚀 Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 1.9 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JWT (jjwt 0.12) |
| Database | H2 (in-memory, dev) |
| Build | Maven / Gradle |
| Hosting | Render (Docker) |

---

## 📁 Project Structure

```
src/
├── main/kotlin/com/financemanager/
│   ├── FinanceManagerApplication.kt
│   ├── config/
│   │   ├── JwtUtil.kt              # JWT token generation & validation
│   │   ├── JwtAuthFilter.kt        # JWT request filter
│   │   └── SecurityConfig.kt       # Spring Security config
│   ├── controller/
│   │   ├── AuthController.kt       # POST /api/auth/register, /login
│   │   ├── UserController.kt       # GET /api/users/profile
│   │   ├── TransactionController.kt # CRUD /api/transactions
│   │   ├── SavingsGoalController.kt # CRUD /api/savings-goals
│   │   └── ReportController.kt     # GET /api/reports/*
│   ├── service/                    # Business logic
│   ├── repository/                 # Spring Data JPA repositories
│   ├── model/                      # JPA entities (User, Transaction, SavingsGoal)
│   ├── dto/                        # Request/Response DTOs
│   └── exception/                  # Global exception handler
└── test/kotlin/com/financemanager/
    └── FinanceManagerIntegrationTests.kt  # 29 integration tests
```

---

## 🔧 Local Setup

### Prerequisites
- JDK 17+
- Maven 3.8+ or Gradle 8.5+

### Run locally

```bash
git clone https://github.com/YOUR_USERNAME/finance-manager.git
cd finance-manager

# Maven
mvn spring-boot:run

# OR Gradle
./gradlew bootRun
```

App starts on **http://localhost:8080**

H2 console: **http://localhost:8080/h2-console**

### Run Tests

```bash
# Maven
mvn test

# OR Gradle
./gradlew test
```

Test report generated at: `target/surefire-reports/` or `build/reports/tests/test/`

---

## 🌐 Deploy to Render

1. Push this repo to GitHub (public)
2. Go to [https://render.com](https://render.com) → New → Web Service
3. Connect your GitHub repo
4. Settings:
   - **Environment**: Docker
   - **Plan**: Free
   - **Health Check Path**: `/actuator/health`
5. Add environment variables:
   - `JWT_SECRET` → any long random string
   - `JWT_EXPIRATION` → `86400000`
6. Click **Deploy**

Your live URL will be: `https://finance-manager-XXXX.onrender.com`

---

## 📡 API Endpoints

### Authentication
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/register` | Register new user | ❌ |
| POST | `/api/auth/login` | Login & get JWT | ❌ |

### User
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/users/profile` | Get current user profile | ✅ |

### Transactions
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/transactions` | Create transaction | ✅ |
| GET | `/api/transactions` | List all transactions | ✅ |
| GET | `/api/transactions?type=INCOME` | Filter by type | ✅ |
| GET | `/api/transactions?category=Food` | Filter by category | ✅ |
| GET | `/api/transactions/{id}` | Get by ID | ✅ |
| PUT | `/api/transactions/{id}` | Update transaction | ✅ |
| DELETE | `/api/transactions/{id}` | Delete transaction | ✅ |

### Savings Goals
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/savings-goals` | Create goal | ✅ |
| GET | `/api/savings-goals` | List all goals | ✅ |
| GET | `/api/savings-goals/{id}` | Get goal by ID | ✅ |
| PUT | `/api/savings-goals/{id}` | Update goal | ✅ |
| PATCH | `/api/savings-goals/{id}/progress` | Update saved amount | ✅ |
| PATCH | `/api/savings-goals/{id}/status` | Update status | ✅ |
| DELETE | `/api/savings-goals/{id}` | Delete goal | ✅ |

### Reports
| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/api/reports/summary` | All-time summary | ✅ |
| GET | `/api/reports/monthly?year=2025&month=5` | Monthly breakdown | ✅ |
| GET | `/api/reports/categories?type=EXPENSE` | Per-category totals | ✅ |

---

## 🔐 Authentication

All protected routes require a **Bearer Token** in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## 📋 Example Requests

### Register
```json
POST /api/auth/register
{
  "username": "aman123",
  "email": "aman@example.com",
  "password": "securepass",
  "fullName": "Aman Singh"
}
```

### Create Transaction
```json
POST /api/transactions
Authorization: Bearer <token>
{
  "amount": 5000.00,
  "type": "INCOME",
  "category": "Salary",
  "description": "Monthly salary",
  "transactionDate": "2025-05-01T09:00:00"
}
```

### Create Savings Goal
```json
POST /api/savings-goals
Authorization: Bearer <token>
{
  "name": "Emergency Fund",
  "description": "6 months expenses",
  "targetAmount": 100000.00,
  "currentAmount": 25000.00,
  "targetDate": "2026-12-31"
}
```

### Update Progress
```json
PATCH /api/savings-goals/1/progress
Authorization: Bearer <token>
{
  "currentAmount": 50000.00
}
```

---

## 🧪 Test Coverage

29 integration tests covering:
- ✅ User registration & duplicate detection
- ✅ Login with valid/invalid credentials  
- ✅ JWT auth on protected routes
- ✅ Full CRUD for transactions
- ✅ Transaction filtering (type, category, date range)
- ✅ Full CRUD for savings goals
- ✅ Goal progress & auto-completion
- ✅ Summary, monthly, and category reports
- ✅ Security: missing/invalid tokens return 401

---

## 📊 Response Format

All responses follow a consistent envelope:

```json
{
  "success": true,
  "message": "Transaction created",
  "data": { ... }
}
```

Error responses:
```json
{
  "success": false,
  "message": "Username 'aman123' already taken",
  "data": null
}
```
