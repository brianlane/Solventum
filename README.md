# solventum
Project setup with Maven and Spring Boot dependencies
Core URL shortening algorithm with Base-62 encoding
Basic encode/decode functionality
REST API endpoints (/encode and /decode)
Request/Response models with validation
Comprehensive unit and integration tests

#Algorithm Implementation
Base-62 encoding using [a-z, A-Z, 0-9]
Atomic counter for thread-safe unique ID generation
ConcurrentHashMap for bidirectional URL mapping
URL validation with regex pattern matching

#API Endpoints
POST /api/encode - Convert long URL to short URL
POST /api/decode - Convert short URL back to long URL
GET /api/health - Health check endpoint
GET /api/stats - Service statistics

#Running the Application
Prerequisites

Java 17+
Maven 3.6+

Build and Run
bash# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
Use the test.html page - open it in your browser for a user-friendly interface
Copy the short URL from the encode response to test decode
Monitor the console - You'll see DEBUG logs showing the operations