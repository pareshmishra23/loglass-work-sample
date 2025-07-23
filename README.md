# Loglass Work Sample Test - Complete Implementation

## Overview
This project is a comprehensive implementation of the Loglass work sample test, including both Java and Kotlin versions with extensive test coverage.

## Project Structure
```
src/
├── main/
│   ├── java/javaversion/
│   │   ├── Database.java
│   │   ├── Mailer.java
│   │   ├── Main.java
│   │   └── UserController.java
│   └── kotlin/
│       ├── Database.kt
│       ├── Mailer.kt
│       ├── Main.kt
│       └── UserController.kt
└── test/
    ├── java/javaversion/
    │   └── ComprehensiveJUnitTest.java
    └── kotlin/
        ├── ComprehensiveJUnitTest.kt
        └── ComprehensiveKotestTest.kt
```

## Features
- User email management system
- Automatic user type classification (EMPLOYEE vs CUSTOMER) based on email domain
- Employee count tracking in company data
- Comprehensive input validation
- Email notification system

## Running the Project

### Prerequisites
- JDK 17 or higher
- Gradle 7.0 or higher

### Commands
```bash
# Build the project
./gradlew build

# Run all tests
./gradlew test

# Run Java main
./gradlew run --main-class=javaversion.Main

# Run Kotlin main
./gradlew run --main-class=MainKt

# Run specific test classes
./gradlew test --tests "javaversion.ComprehensiveJUnitTest"
./gradlew test --tests "ComprehensiveJUnitTest"
./gradlew test --tests "ComprehensiveKotestTest"
```

## Test Coverage
- Database operations (CRUD)
- Email change scenarios
- User type conversions
- Employee count management
- Input validation and error handling
- Edge cases and special characters

## Key Improvements
1. Email format validation with regex patterns
2. Enhanced error handling with clear messages
3. Proper employee count logic with delta calculations
4. Mock integration for isolated testing
5. Comprehensive edge case coverage