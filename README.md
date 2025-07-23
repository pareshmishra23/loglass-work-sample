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

# Core Functional Scenarios (User Stories)
1. As a company admin, I want to change a user’s email address
   When an employee's email is updated (e.g., from alice@loglass.co.jp to alice.new@loglass.co.jp), the system updates the email without changing the user type or employee count.

If the domain changes to an external one (e.g., alice@example.com), the user becomes a CUSTOMER, and the employee count decreases by 1.

If the domain changes to an internal one (e.g., michael@loglass.co.jp), and the user was a CUSTOMER, they become an EMPLOYEE, and the count increases by 1.

2. As an admin, I want to make sure the email change is notified
   After an email change, a notification is sent using the Mailer.

3. As a system, I must ensure that emails follow valid formats
   The system rejects emails like invalid-email, alice.loglass.co.jp, or "" (empty).

The format is verified using a regular expression.

4. As a developer, I want the system to ignore redundant updates
   If the email hasn’t changed (alice@loglass.co.jp to same), no update happens, and the mailer is not invoked.

5. As a company, I want to track employee count accurately
   When users change domains, employee count adjusts accordingly.

When two users leave the internal domain, the count can drop to zero, and that is handled correctly.

🧪 Edge Case Scenarios
6. Changing email for users with special characters
   Emails like alice+test@loglass.co.jp are accepted and updated properly.

7. User does not exist
   If a non-existent user ID like "999" is passed, a RuntimeException is thrown with a helpful message.

8. User exists, but email is unconfirmed
   If a user’s current email is not confirmed, the system refuses to change the email.

9. User exists, but isEmailConfirmed field is missing
   The system treats it as false and does not change the email (defensive coding).

10. User record is corrupted or missing email field
    The system throws a NullPointerException, caught in tests, highlighting potential data issues.

11. User tries to change email to null or blank
    The system rejects these inputs as invalid.


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

## Test Coverage - 100 % 
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