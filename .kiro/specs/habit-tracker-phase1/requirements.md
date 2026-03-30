# Requirements Document: Habit Tracker Phase 1 (MVP)

## Introduction

The Habit Tracker Phase 1 MVP is a production-ready web application that enables users to build and maintain daily habits through systematic tracking and analytics. The system provides secure user authentication with email verification, comprehensive habit management capabilities, daily completion tracking with streak calculation, and visual analytics to motivate consistent behavior. The application consists of a Kotlin + Spring Boot backend with MariaDB database and a React frontend, designed to support individual users in developing positive daily routines through data-driven insights.

## Glossary

- **System**: The complete Habit Tracker application including backend API and frontend UI
- **Backend**: The Kotlin + Spring Boot REST API server
- **Frontend**: The React web application
- **User**: A registered account holder who can create and track habits
- **Habit**: A daily activity that a user wants to track and maintain
- **Habit_Record**: A single completion entry for a habit on a specific date
- **Streak**: The count of consecutive days a habit has been completed
- **JWT**: JSON Web Token used for authentication
- **Verification_Token**: A unique token sent via email to verify user email addresses
- **BCrypt**: Password hashing algorithm used for secure password storage
- **LocalDate**: Java time class representing a date without time or timezone
- **Frequency_Type**: The recurrence pattern for a habit (DAILY only in Phase 1)
- **Record_Status**: The completion state of a habit record (DONE only in Phase 1)
- **Database**: MariaDB 11.8.2 relational database
- **SMTP**: Simple Mail Transfer Protocol for sending verification emails
- **CORS**: Cross-Origin Resource Sharing for frontend-backend communication

## Requirements

### Requirement 1: User Registration

**User Story:** As a new user, I want to register an account with my email and password, so that I can access the habit tracking system.

#### Acceptance Criteria

1. WHEN a user submits a signup request with valid email and password, THE Backend SHALL create a new user account with verified status set to false
2. WHEN a user submits a signup request, THE Backend SHALL hash the password using BCrypt before storing it
3. WHEN a user submits a signup request with an email that already exists, THE Backend SHALL return a 409 Conflict error
4. WHEN a user submits a signup request with invalid email format, THE Backend SHALL return a 400 Bad Request error
5. WHEN a user submits a signup request with password shorter than 8 characters, THE Backend SHALL return a 400 Bad Request error
6. WHEN a user account is created, THE Backend SHALL generate a unique verification token with 24-hour expiration
7. WHEN a user account is created, THE Backend SHALL send a verification email containing the verification token
8. THE Backend SHALL never store passwords in plaintext


### Requirement 2: Email Verification

**User Story:** As a registered user, I want to verify my email address, so that I can activate my account and login.

#### Acceptance Criteria

1. WHEN a user clicks a verification link with a valid token, THE Backend SHALL update the user's verified status to true
2. WHEN a user submits a verification token that has expired, THE Backend SHALL return a 400 Bad Request error
3. WHEN a user submits an invalid verification token, THE Backend SHALL return a 400 Bad Request error
4. WHEN a user's email is successfully verified, THE Backend SHALL delete the verification token
5. THE Backend SHALL set verification token expiration to 24 hours from creation time

### Requirement 3: User Authentication

**User Story:** As a verified user, I want to login with my email and password, so that I can access my habits and tracking data.

#### Acceptance Criteria

1. WHEN a verified user submits valid credentials, THE Backend SHALL return a JWT access token
2. WHEN a user submits invalid credentials, THE Backend SHALL return a 401 Unauthorized error
3. WHEN an unverified user attempts to login, THE Backend SHALL return a 403 Forbidden error
4. WHEN generating a JWT token, THE Backend SHALL include user ID and email as claims
5. WHEN generating a JWT token, THE Backend SHALL set expiration to 24 hours from creation
6. WHEN generating a JWT token, THE Backend SHALL sign it with a secret key
7. THE Backend SHALL validate JWT token signature on all authenticated requests
8. THE Backend SHALL reject expired JWT tokens with 401 Unauthorized error

### Requirement 4: Habit Creation

**User Story:** As an authenticated user, I want to create new daily habits, so that I can track activities I want to maintain.

#### Acceptance Criteria

1. WHEN an authenticated user submits a habit creation request with valid name, THE Backend SHALL create a new habit with DAILY frequency type
2. WHEN a user submits a habit creation request without authentication, THE Backend SHALL return a 401 Unauthorized error
3. WHEN a user submits a habit creation request with blank name, THE Backend SHALL return a 400 Bad Request error
4. WHEN a user submits a habit creation request with name exceeding 100 characters, THE Backend SHALL return a 400 Bad Request error
5. WHEN a habit is created, THE Backend SHALL associate it with the authenticated user's ID
6. WHEN a habit is created, THE Backend SHALL set the frequency type to DAILY
7. WHEN a habit is created, THE Backend SHALL record the creation timestamp

### Requirement 5: Habit Retrieval

**User Story:** As an authenticated user, I want to view all my habits, so that I can see what I'm tracking.

#### Acceptance Criteria

1. WHEN an authenticated user requests their habits, THE Backend SHALL return all habits belonging to that user
2. WHEN an authenticated user requests their habits, THE Backend SHALL not return habits belonging to other users
3. WHEN a user requests habits without authentication, THE Backend SHALL return a 401 Unauthorized error
4. WHEN returning habits, THE Backend SHALL include habit ID, name, frequency type, and creation timestamp

### Requirement 6: Habit Modification

**User Story:** As an authenticated user, I want to update my habit names, so that I can refine how I describe my activities.

#### Acceptance Criteria

1. WHEN an authenticated user submits an update request for their own habit, THE Backend SHALL update the habit name
2. WHEN a user attempts to update a habit they do not own, THE Backend SHALL return a 404 Not Found error
3. WHEN a user submits an update request with blank name, THE Backend SHALL return a 400 Bad Request error
4. WHEN a user submits an update request with name exceeding 100 characters, THE Backend SHALL return a 400 Bad Request error
5. WHEN a user updates a habit without authentication, THE Backend SHALL return a 401 Unauthorized error

### Requirement 7: Habit Deletion

**User Story:** As an authenticated user, I want to delete habits I no longer track, so that I can keep my habit list relevant.

#### Acceptance Criteria

1. WHEN an authenticated user deletes their own habit, THE Backend SHALL remove the habit from the database
2. WHEN a habit is deleted, THE Backend SHALL also delete all associated habit records
3. WHEN a user attempts to delete a habit they do not own, THE Backend SHALL return a 404 Not Found error
4. WHEN a user deletes a habit without authentication, THE Backend SHALL return a 401 Unauthorized error

### Requirement 8: Daily Habit Tracking

**User Story:** As an authenticated user, I want to mark habits as completed for specific dates, so that I can record my progress.

#### Acceptance Criteria

1. WHEN an authenticated user marks a habit as done for a specific date, THE Backend SHALL create a habit record with status DONE
2. WHEN a user marks the same habit as done for the same date twice, THE Backend SHALL update the existing record instead of creating a duplicate
3. WHEN a user attempts to mark a habit they do not own, THE Backend SHALL return a 404 Not Found error
4. WHEN a user marks a habit without authentication, THE Backend SHALL return a 401 Unauthorized error
5. WHEN a user submits an invalid date format, THE Backend SHALL return a 400 Bad Request error
6. THE Database SHALL enforce a unique constraint on habit ID and date combination
7. WHEN a habit record is created, THE Backend SHALL use LocalDate for timezone-safe date storage

### Requirement 9: Streak Calculation

**User Story:** As an authenticated user, I want to see my current streak for each habit, so that I can stay motivated to maintain consistency.

#### Acceptance Criteria

1. WHEN calculating a streak, THE Backend SHALL count consecutive days from today backwards where the habit was completed
2. WHEN today's habit is not completed, THE Backend SHALL count consecutive days from yesterday backwards
3. WHEN there is a gap in completion dates, THE Backend SHALL stop counting at the gap
4. WHEN a habit has no completion records, THE Backend SHALL return a streak of 0
5. WHEN calculating streaks, THE Backend SHALL use LocalDate arithmetic for timezone-safe date comparisons
6. THE Backend SHALL ensure streak count is always non-negative
7. THE Backend SHALL ensure streak count never exceeds total number of completion records

### Requirement 10: Statistics and Analytics

**User Story:** As an authenticated user, I want to view statistics for all my habits, so that I can understand my overall progress.

#### Acceptance Criteria

1. WHEN an authenticated user requests statistics, THE Backend SHALL return data for all their habits
2. WHEN returning statistics, THE Backend SHALL include habit ID, habit name, current streak, and total completions
3. WHEN a user requests statistics without authentication, THE Backend SHALL return a 401 Unauthorized error
4. WHEN calculating statistics, THE Backend SHALL dynamically compute streaks from habit records
5. THE Backend SHALL not store streak values in the database

### Requirement 11: Password Security

**User Story:** As a system administrator, I want all passwords to be securely hashed, so that user credentials are protected.

#### Acceptance Criteria

1. THE Backend SHALL hash all passwords using BCrypt with strength factor 10 or higher
2. THE Backend SHALL never log or expose password values in API responses
3. WHEN comparing passwords during login, THE Backend SHALL use BCrypt's secure comparison function
4. THE Backend SHALL generate unique salts for each password hash

### Requirement 12: JWT Token Security

**User Story:** As a system administrator, I want JWT tokens to be securely generated and validated, so that authentication is reliable.

#### Acceptance Criteria

1. THE Backend SHALL use a secret key of at least 256 bits for JWT signing
2. THE Backend SHALL store the JWT secret in environment variables, not in source code
3. WHEN validating JWT tokens, THE Backend SHALL verify the signature
4. WHEN validating JWT tokens, THE Backend SHALL check the expiration time
5. THE Backend SHALL use HS256 algorithm for JWT signing

### Requirement 13: Database Constraints

**User Story:** As a system administrator, I want database constraints to enforce data integrity, so that the system remains consistent.

#### Acceptance Criteria

1. THE Database SHALL enforce a unique constraint on user email addresses
2. THE Database SHALL enforce a unique constraint on the combination of habit ID and date in habit records
3. THE Database SHALL enforce foreign key constraints between habits and users
4. THE Database SHALL enforce foreign key constraints between habit records and habits
5. THE Database SHALL enforce NOT NULL constraints on all required fields

### Requirement 14: API Input Validation

**User Story:** As a system administrator, I want all API inputs to be validated, so that invalid data is rejected early.

#### Acceptance Criteria

1. WHEN a request contains invalid data, THE Backend SHALL return a 400 Bad Request error with descriptive message
2. THE Backend SHALL validate email format using standard email validation rules
3. THE Backend SHALL validate that required fields are not blank or null
4. THE Backend SHALL validate string length constraints before processing
5. THE Backend SHALL validate date format for habit tracking requests

### Requirement 15: Authorization Enforcement

**User Story:** As a system administrator, I want users to only access their own data, so that privacy is maintained.

#### Acceptance Criteria

1. WHEN a user attempts to access another user's habit, THE Backend SHALL return a 404 Not Found error
2. WHEN a user attempts to modify another user's habit, THE Backend SHALL return a 404 Not Found error
3. WHEN a user attempts to delete another user's habit, THE Backend SHALL return a 404 Not Found error
4. THE Backend SHALL extract user ID from JWT token for all authenticated requests
5. THE Backend SHALL verify habit ownership before allowing any habit operations

### Requirement 16: CORS Configuration

**User Story:** As a frontend developer, I want the backend to accept requests from the frontend origin, so that the application works correctly.

#### Acceptance Criteria

1. THE Backend SHALL allow cross-origin requests from the configured frontend URL
2. THE Backend SHALL allow GET, POST, PATCH, and DELETE HTTP methods
3. THE Backend SHALL allow all necessary headers in CORS requests
4. WHERE the application is in production, THE Backend SHALL restrict CORS to the production frontend domain only

### Requirement 17: Error Response Format

**User Story:** As a frontend developer, I want consistent error response formats, so that I can handle errors predictably.

#### Acceptance Criteria

1. WHEN an error occurs, THE Backend SHALL return a JSON response with an error message
2. WHEN validation fails, THE Backend SHALL include all validation error messages in the response
3. THE Backend SHALL use appropriate HTTP status codes for different error types
4. THE Backend SHALL not expose sensitive information in error messages

### Requirement 18: Email Delivery

**User Story:** As a new user, I want to receive verification emails promptly, so that I can activate my account.

#### Acceptance Criteria

1. WHEN a user signs up, THE Backend SHALL send a verification email within 30 seconds
2. WHEN sending verification emails, THE Backend SHALL include a clickable verification link
3. WHEN sending verification emails, THE Backend SHALL use a configured SMTP server
4. IF email sending fails, THE Backend SHALL log the error but not fail the signup request

### Requirement 19: Date Handling

**User Story:** As a system architect, I want all date operations to be timezone-safe, so that tracking works correctly for users in any timezone.

#### Acceptance Criteria

1. THE Backend SHALL use LocalDate for all date storage and comparisons
2. THE Backend SHALL not use timestamp or datetime types for habit record dates
3. WHEN performing date arithmetic, THE Backend SHALL use LocalDate methods
4. THE Backend SHALL handle month and year boundaries correctly in date calculations

### Requirement 20: Frequency Type Constraint

**User Story:** As a product manager, I want to enforce DAILY frequency only in Phase 1, so that the MVP scope is controlled.

#### Acceptance Criteria

1. WHEN creating a habit, THE Backend SHALL set frequency type to DAILY
2. THE Backend SHALL not accept non-DAILY frequency types in Phase 1
3. THE Backend SHALL store frequency type as an enum value

### Requirement 21: Record Status Constraint

**User Story:** As a product manager, I want to enforce DONE status only in Phase 1, so that the MVP scope is controlled.

#### Acceptance Criteria

1. WHEN creating a habit record, THE Backend SHALL set status to DONE
2. THE Backend SHALL not accept non-DONE status values in Phase 1
3. THE Backend SHALL store record status as an enum value

### Requirement 22: Database Indexing

**User Story:** As a system administrator, I want appropriate database indexes, so that queries perform efficiently.

#### Acceptance Criteria

1. THE Database SHALL have an index on the user email column
2. THE Database SHALL have an index on the habit user_id column
3. THE Database SHALL have an index on the habit_record habit_id column
4. THE Database SHALL have an index on the habit_record date column
5. THE Database SHALL have a composite unique index on habit_record (habit_id, date)

### Requirement 23: Frontend Authentication State

**User Story:** As a frontend user, I want my login session to persist across page refreshes, so that I don't have to login repeatedly.

#### Acceptance Criteria

1. WHEN a user logs in, THE Frontend SHALL store the JWT token in browser storage
2. WHEN the page refreshes, THE Frontend SHALL retrieve the JWT token from storage
3. WHEN the JWT token expires, THE Frontend SHALL redirect the user to the login page
4. WHEN a user logs out, THE Frontend SHALL remove the JWT token from storage

### Requirement 24: Frontend API Communication

**User Story:** As a frontend developer, I want all API requests to include authentication, so that protected endpoints work correctly.

#### Acceptance Criteria

1. WHEN making authenticated API requests, THE Frontend SHALL include the JWT token in the Authorization header
2. WHEN an API request returns 401 Unauthorized, THE Frontend SHALL redirect to the login page
3. THE Frontend SHALL use the configured backend API URL for all requests
4. THE Frontend SHALL handle network errors gracefully with user-friendly messages

### Requirement 25: Frontend Habit Management UI

**User Story:** As a user, I want an intuitive interface to manage my habits, so that I can easily create, view, update, and delete habits.

#### Acceptance Criteria

1. WHEN viewing the habits page, THE Frontend SHALL display all user habits in a list
2. WHEN creating a habit, THE Frontend SHALL provide a form with name input
3. WHEN updating a habit, THE Frontend SHALL provide a form pre-filled with current name
4. WHEN deleting a habit, THE Frontend SHALL request confirmation before proceeding
5. THE Frontend SHALL display success and error messages for all habit operations

### Requirement 26: Frontend Daily Tracking UI

**User Story:** As a user, I want a simple way to mark habits as complete, so that I can quickly record my daily progress.

#### Acceptance Criteria

1. WHEN viewing habits, THE Frontend SHALL display a check button for each habit
2. WHEN clicking the check button, THE Frontend SHALL mark the habit as done for today
3. WHEN a habit is already checked for today, THE Frontend SHALL indicate the completed status visually
4. THE Frontend SHALL allow users to mark habits for past dates
5. THE Frontend SHALL update the UI immediately after marking a habit as done

### Requirement 27: Frontend Statistics Display

**User Story:** As a user, I want to see my streaks and statistics, so that I can track my progress and stay motivated.

#### Acceptance Criteria

1. WHEN viewing the statistics page, THE Frontend SHALL display current streak for each habit
2. WHEN viewing the statistics page, THE Frontend SHALL display total completions for each habit
3. THE Frontend SHALL refresh statistics when navigating to the statistics page
4. THE Frontend SHALL display statistics in a clear and readable format

### Requirement 28: Frontend Routing

**User Story:** As a user, I want to navigate between different pages, so that I can access all features of the application.

#### Acceptance Criteria

1. THE Frontend SHALL provide routes for signup, login, habits, and statistics pages
2. THE Frontend SHALL protect authenticated routes from unauthenticated access
3. WHEN accessing a protected route without authentication, THE Frontend SHALL redirect to the login page
4. THE Frontend SHALL provide navigation links between pages

### Requirement 29: Environment Configuration

**User Story:** As a system administrator, I want to configure the application through environment variables, so that I can deploy to different environments.

#### Acceptance Criteria

1. THE Backend SHALL read database connection details from environment variables
2. THE Backend SHALL read JWT secret from environment variables
3. THE Backend SHALL read SMTP configuration from environment variables
4. THE Backend SHALL read server port from environment variables
5. THE Backend SHALL provide default values for development environment

### Requirement 30: Production Readiness

**User Story:** As a system administrator, I want the application to be production-ready, so that it can be deployed reliably.

#### Acceptance Criteria

1. THE Backend SHALL use HTTPS in production environment
2. THE Backend SHALL set appropriate CORS restrictions for production
3. THE Backend SHALL use connection pooling for database connections
4. THE Backend SHALL log errors appropriately without exposing sensitive data
5. THE Backend SHALL handle graceful shutdown

