# Honkai Backend

## Technologies Used
- **Spring Boot**: Core framework for the backend application
- **MySQL**: Core database for the backend application
- **Spring Security**: For authentication and authorization
- **JWT (JSON Web Token)**: For stateless authentication
- **JPA/Hibernate**: For ORM and database interactions
- **Redis**: For JWT token blacklisting
- **Lombok**: For reducing boilerplate code
- **Bcrypt**: For password encryption

## Project Structure

### Configuration
- **SecurityConfig**: Configures Spring Security with JWT filter and authentication
- **CorsConfig**: Handles Cross-Origin Resource Sharing for frontend-backend communication
- **LettuceRedisConfig**: Redis configuration for connecting/disconnecting the database and processing commands.
- **RedisCodecConfig**: Redis configuration for allowing custom object/primitive data types to be stored in Redis.

### Authentication Flow
1. User registers or logs in via `/auth/register` or `/auth/login` endpoints
2. Upon successful authentication, a JWT token is generated with 2-hour expiration
3. The token is sent to the client and used for subsequent requests
4. The token is validated by the JwtFilter for protected routes
5. Logout mechanism clears local storage and will blacklist tokens 

### Security Features
- Secure password storage with BCrypt encoding
- JWT-based authentication with token validation
- HTTPS Enabled
- JWT token is stored through cookie-based sessions.
- Requests authorization is verified from cookies.
- CORS configuration for secure cross-origin requests
- Role-based access control (ADMIN and USER roles)
- Protection against CSRF attacks (configuration ready for future implementation)
- Comprehensive input validation

### Models
- **User**: Main entity with username, email, password, role, and bio fields
- **Role**: Enum defining user roles (ADMIN, USER)

### DTOs (Data Transfer Objects)
- **LoginRequest**: For login operations with userInput (email or username) and password
- **RegisterRequest**: For registration with username, email, password, and optional bio

### Services
- **UserService**: Manages user-related operations (creation, retrieval)
- **JWTService**: Handles JWT token generation, validation, and parsing
- **CustomUserDetailsService**: Implements Spring Security's UserDetailsService for authentication
- **BlacklistTokenService**: For managing revoked tokens

### Utilities
- **JWTFilter**: Every requests a user made, they are validated and verified through the this filter.
- **Jackson2JSonRedisCodec**: A utility class for essentially storing any object/primitive data types inside Redis.

## Setup Requirements

### Environment Variables
- MySQL configuration properties:
  - `MYSQL_USER`: Username of the MySQL Database
  - `MYSQL_PASSWORD`: Password of the MySQL Database
- `CORS_ALLOWED_ORIGIN`: URL of the frontend application for CORS configuration
- Redis configuration properties in application properties:
    - `redis.host`: Redis server hostname
    - `redis.port`: Redis server port
    - `redis.username`: Redis username (if applicable)
    - `redis.password`: Redis password (if applicable)
    - `redis.database`: Redis database index (Default = 0)
    - `redis.timeout`: Connection timeout in milliseconds (Default = 3000 milliseconds)
- SSL/TLS Certificate Configuration for enabling HTTPS
  - `SSL_KEYSTORE_PATH`: Keystore file location for SSL Certificate
  - `SSL_KEYSTORE_PASSWORD`: Password of the SSL Certificate

## Setting up HTTPS for Local Development

To enable HTTPS for local development, follow these steps:

**Install mkcert**
  - For Windows: `choco install mkcert`
  - For macOS: `brew install mkcert`
  - For Linux: Check distribution-specific instructions

**Set up the local CA and create certificates**
```bash
mkcert -install
mkcert localhost 127.0.0.1 ::1
```
This will create two files: `localhost+2.pem` (certificate) and `localhost+2-key.pem` (private key)

**Convert to PKCS12 format for Java**
```bash
openssl pkcs12 -export -in localhost+2.pem -inkey localhost+2-key.pem -out honkai-keystore.p12 -name honkai-ssl
```
When prompted, enter a password you'll remember

**Move the keystore file to your resources directory**
```bash
mv honkai-keystore.p12 src/main/resources/
```

**Configure your environmental variables**
```dotenv
SSL_KEYSTORE_PATH=classpath:honkai-keystore.p12
SSL_KEYSTORE_PASSWORD=your_password_here
```

### Frontend Configuration

Update your frontend environment variables (if it hasn't been set already) to use the HTTPS URL:
```dotenv
VITE_API_URL=https://localhost:8443
```

## Security Notes
- Passwords are encoded with BCrypt before storage
- JWT tokens expire after 2 hours
- The system supports login with either username or email
- User roles determine access permissions
- Token validation checks for expiration and user matching

## Logging
The application uses SLF4J for comprehensive logging across all components, with different log levels for:
- DEBUG: Detailed information for development and troubleshooting
- INFO: Standard operational messages
- WARN: Potential issues that don't prevent operation
- ERROR: Problems that need attention

## Future Implementations
- User profile updates and management
- Creating and posting contents for social application
