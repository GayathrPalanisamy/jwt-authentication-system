# JWT-Based User Authentication and Product Management
A RESTful backend application built using Java, Spring Boot, Spring Security, JWT authentication, and PostgreSQL. The application supports user registration, secure login, token-based authentication, role-based access to settings, and product management APIs.

## Features
- User registration with input validation
- Secure password hashing using BCrypt
- JWT-based authentication with token expiration
- Logout with token revocation
- Role-based authorization for Administrator-only settings
- Product CRUD operations (Create, Read, Update, Delete)
- PostgreSQL persistence for users and products
- Centralized API error handling and validation
- Swagger/OpenAPI API documentation
- REST API testing using Postman

## Technology Stack
- Java
- Spring Boot
- Spring Security
- JSON Web Tokens (JWT)
- PostgreSQL
- Spring Data JPA / Hibernate
- Maven
- Swagger/OpenAPI
- Postman

## Project Structure
authentication/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/onedao/authentication/
│   │   └── resources/
│   └── test/
│       └── java/
├── .mvn/
│   └── wrapper/
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

## Prerequisites
Install the following before running the application:
- Java version compatible with the project configuration
- PostgreSQL
- Git (optional if downloading the source as a ZIP file)

## Configuration
Configure your PostgreSQL connection and JWT settings using your local application configuration or environment variables.
Do not commit real database passwords, JWT signing secrets, or other sensitive credentials to version control.
Ensure that the PostgreSQL database exists and that the application has permission to connect to it.

## Running the Application
From the "authentication" directory, run the following commands.

### Windows
mvnw.cmd spring-boot:run

### Linux or macOS
./mvnw spring-boot:run

Alternatively, build the project:
mvnw.cmd clean package

The default Spring Boot server URL is:
http://localhost:8080

## API Endpoints

| Method | Endpoint | Description |
| POST | /api/auth/register | Register a user |
| POST | /api/auth/login | Authenticate a user and obtain a JWT |
| POST | /api/auth/logout | Log out and revoke the JWT |
| GET | /api/dashboard | View the authenticated user's welcome message |
| GET | /api/settings | Access administrator settings |
| GET | /api/products | Retrieve products |
| GET | /api/products/{id} | Retrieve a product by ID |
| POST | /api/products | Create a product |
| PUT | /api/products/{id} | Update a product |
| DELETE | /api/products/{id} | Delete a product |

Protected endpoints require a valid JWT in the HTTP Authorization header:
Authorization: Bearer <your-jwt-token>

Product endpoints require authentication. The Settings endpoint is restricted to Administrators.

## API Documentation

After starting the application, open Swagger UI:

[http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

OpenAPI JSON is available at:

[http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Testing

API endpoints can be tested using Postman or Swagger UI.

Test cases include:
- Successful registration and login
- Invalid registration data
- Invalid credentials
- Access without a JWT
- Expired, invalid, or revoked JWTs
- Dashboard access after login and logout
- Administrator-only Settings access
- Product CRUD operations
- Invalid product data and nonexistent product IDs
- PostgreSQL persistence after restarting the application

## Security Notes
- Passwords are hashed rather than stored in plain text.
- JWTs have an expiration time.
- Revoked tokens are rejected by the application.
- Protected API endpoints require authentication.
- Sensitive configuration values should be supplied securely and excluded from source control.

## Future Improvements
- Complete country-based registration restrictions
- Add pagination, sorting, and filtering to product retrieval
- Add automated integration and security tests
- Add OTP verification if required by the final specification
- Build a frontend for registration, login, dashboard, and product management

## Author
Developed as a backend developer machine-test project.
