# Loglass Work Sample Test - Complete Implementation

## Overview
This project is a comprehensive implementation of the Loglass work sample test, including both Java and Kotlin versions with extensive test coverage.
 
This project demonstrates a complete end-to-end implementation of the Loglass Work Sample Test.
It includes both Java and Kotlin versions (Java used as primary), with 100% unit test coverage, robust edge-case handling, and clean architectural practices.
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
# open build/reports/jacoco/test/html/index.html
./gradlew test jacocoTestReport

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