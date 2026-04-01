# Implementation Plan: Habit Tracker Phase 1 (MVP)

## Overview

This implementation plan breaks down the Habit Tracker Phase 1 MVP into discrete coding tasks following the commit-based development approach from the design document. The system will be built incrementally across 10 phases: User Authentication, Email Verification, JWT Security, Habit CRUD, Daily Tracking, Streak Calculation, Testing, Frontend, and Documentation. Each task builds on previous work and includes specific requirement references for traceability.

## Tasks

- [ ] 1. User Authentication - Setup and Signup
  - [x] 1.1 Create user repository and password encoder configuration
    - Create `UserRepository` interface extending `JpaRepository<User, Long>`
    - Add `findByEmail(email: String): User?` method to UserRepository
    - Configure `BCryptPasswordEncoder` bean in security configuration
    - Create `SecurityConfig` class with password encoder bean
    - _Requirements: 1.2, 11.1, 11.4_
    - _Commit: feat(auth): add user repository and password encoder_

  - [x] 1.2 Implement signup endpoint with validation
    - Create `SignupRequest` DTO with email and password validation annotations
    - Create `MessageResponse` DTO for success messages
    - Create `AuthService` class with `signup(email: String, password: String): User` method
    - Implement password hashing using BCrypt in signup method
    - Create `AuthController` with POST /auth/signup endpoint
    - Add exception handler for duplicate email (409 Conflict)
    - Add exception handler for validation errors (400 Bad Request)
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.8, 14.1, 14.2, 14.3, 14.4_
    - _Commit: feat(auth): implement signup endpoint_

  - [x] 1.3 Write property test for password hashing
    - **Property 1: Password Security**
    - **Validates: Requirements 1.2, 1.8, 11.1**
    - Test that passwords are never stored in plaintext
    - Test that BCrypt hashes are always 60 characters
    - Test that same password produces different hashes (Property 8)

  - [x] 1.4 Write property test for BCrypt hash uniqueness
    - **Property 8: BCrypt Hash Uniqueness**
    - **Validates: Requirements 11.4**
    - Test that hashing same password multiple times produces different hashes
    - Test that all hashes can be verified with original password

- [x] 2. User Authentication - Login with JWT
  - [x] 2.1 Add JWT dependencies and create JWT utility
    - Add JWT dependencies to build.gradle.kts (jjwt-api, jjwt-impl, jjwt-jackson)
    - Create `JwtUtil` component with secret and expiration from application.yaml
    - Implement `generateToken(userId: Long, email: String): String` method
    - Implement `validateToken(token: String): Boolean` method
    - Implement `extractUserId(token: String): Long` method
    - Implement `extractEmail(token: String): String` method
    - _Requirements: 3.4, 3.5, 3.6, 12.1, 12.2, 12.5_
    - _Commit: feat(auth): implement login endpoint_

  - [x] 2.2 Implement login endpoint with credential validation
    - Create `LoginRequest` DTO with email and password validation
    - Create `LoginResponse` DTO with accessToken field
    - Add `login(email: String, password: String): String` method to AuthService
    - Implement user lookup by email in login method
    - Add verified status check (throw UnverifiedUserException if false)
    - Add password validation using BCrypt matches
    - Generate and return JWT token on successful login
    - Add POST /auth/login endpoint to AuthController
    - Create exception handlers for InvalidCredentialsException (401) and UnverifiedUserException (403)
    - _Requirements: 3.1, 3.2, 3.3, 3.8, 17.1, 17.3_
    - _Commit: feat(auth): implement login endpoint_

  - [x] 2.3 Write property test for email verification requirement
    - **Property 2: Email Verification Required**
    - **Validates: Requirements 3.3**
    - Test that unverified users cannot login
    - Test that verified users can login with correct credentials

  - [x] 2.4 Write property test for JWT token validity
    - **Property 6: JWT Token Validity**
    - **Validates: Requirements 3.4, 3.5, 3.6, 3.7, 12.3, 12.4**
    - Test that generated tokens contain correct claims (userId, email)
    - Test that tokens have valid signatures
    - Test that token validation works correctly

  - [x] 2.5 Write unit tests for login flow
    - Test successful login returns JWT token
    - Test invalid credentials return 401
    - Test unverified user returns 403
    - Test password comparison uses BCrypt

- [x] 3. Email Verification System
  - [x] 3.1 Create email verification token entity and repository
    - Create `EmailVerificationToken` entity with userId, token, expiryDate fields
    - Add unique constraint on token field
    - Create `EmailVerificationTokenRepository` interface
    - Add `findByToken(token: String): EmailVerificationToken?` method
    - Update signup method to generate verification token with 24-hour expiry
    - Use UUID.randomUUID() for token generation
    - _Requirements: 1.6, 2.5_
    - Commit: feat(auth): add email verification token entity_

  - [x] 3.2 Implement email service for verification emails
    - Create `EmailService` class
    - Add SMTP configuration to application.yaml (host, port, username, password)
    - Implement `sendVerificationEmail(email: String, token: String)` method
    - Format email with verification link containing token
    - Handle email sending failures gracefully (log error, don't fail signup)
    - Integrate email sending into signup flow
    - _Requirements: 1.7, 18.1, 18.2, 18.3, 18.4_
    - Commit: feat(auth): implement email service_

  - [x] 3.3 Implement email verification endpoint
    - Add `verifyEmail(token: String)` method to AuthService
    - Implement token lookup and expiry validation
    - Update user verified status to true on successful verification
    - Delete verification token after successful verification
    - Add POST /auth/verify-email endpoint with token query parameter
    - Create exception handler for InvalidTokenException (400)
    - _Requirements: 2.1, 2.2, 2.3, 2.4_
    - Commit: feat(auth): implement email verification endpoint_

  - [x] 3.4 Write property test for verification token cleanup
    - **Property 10: Verification Token Cleanup**
    - **Validates: Requirements 2.4**
    - Test that tokens are deleted after successful verification
    - Test that tokens cannot be reused

  - [x] 3.5 Write unit tests for email verification
    - Test valid token verifies user
    - Test expired token returns 400
    - Test invalid token returns 400
    - Test token is deleted after verification

- [x] 4. JWT Security Configuration
  - [x] 4.1 Configure JWT authentication filter and Spring Security
    - Create `JwtAuthenticationFilter` extending OncePerRequestFilter
    - Implement token extraction from Authorization header
    - Implement token validation and user authentication in filter
    - Set authentication in SecurityContext
    - Configure Spring Security to use JWT filter
    - Add @AuthenticationPrincipal support for extracting userId
    - Configure security to permit auth endpoints and require authentication for others
    - _Requirements: 3.7, 3.8, 15.4_
    - _Commit: feat(auth): configure JWT authentication filter_

  - [x] 4.2 Write integration test for JWT authentication
    - Test authenticated requests include valid JWT
    - Test requests without JWT return 401
    - Test requests with invalid JWT return 401
    - Test requests with expired JWT return 401

- [x] 5. Checkpoint - Ensure authentication works end-to-end
  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Habit CRUD Operations - Entity and Repository
  - [x] 6.1 Create habit entity and repository
    - Create `FrequencyType` enum with DAILY value
    - Create `Habit` entity with id, userId, name, frequencyType, createdAt fields
    - Add validation: name not blank, max 100 characters
    - Set frequencyType default to DAILY
    - Create `HabitRepository` interface extending JpaRepository
    - Add `findByIdAndUserId(id: Long, userId: Long): Habit?` method
    - Add `findByUserId(userId: Long): List<Habit>` method
    - _Requirements: 4.1, 4.3, 4.4, 4.6, 20.1, 20.3_
    - _Commit: feat(habits): add habit entity and repository_

- [x] 7. Habit CRUD Operations - Service and Endpoints
  - [x] 7.1 Implement habit service with CRUD operations
    - Create `HabitService` class
    - Implement `createHabit(userId: Long, name: String, frequencyType: FrequencyType): Habit`
    - Validate frequencyType is DAILY (throw exception for others)
    - Implement `getHabits(userId: Long): List<Habit>`
    - Implement `updateHabit(habitId: Long, userId: Long, name: String?): Habit`
    - Implement `deleteHabit(habitId: Long, userId: Long)` with ownership validation
    - Add ownership validation (throw NotFoundException if habit not owned by user)
    - _Requirements: 4.1, 4.5, 5.1, 5.2, 6.1, 6.2, 6.3, 6.4, 7.1, 7.3, 15.1, 15.2, 15.3, 15.5, 20.2_
    - _Commit: feat(habits): implement habit CRUD endpoints_

  - [x] 7.2 Create habit controller with REST endpoints
    - Create `CreateHabitRequest` DTO with name validation
    - Create `UpdateHabitRequest` DTO with optional name
    - Create `HabitResponse` DTO with id, name, frequencyType, createdAt
    - Create `HabitController` class
    - Add GET /habits endpoint (returns List<HabitResponse>)
    - Add POST /habits endpoint (returns HabitResponse, 201 Created)
    - Add PATCH /habits/{id} endpoint (returns HabitResponse)
    - Add DELETE /habits/{id} endpoint (returns 204 No Content)
    - Extract userId from @AuthenticationPrincipal in all endpoints
    - Add exception handler for NotFoundException (404)
    - _Requirements: 4.2, 4.3, 4.4, 5.3, 5.4, 6.3, 6.4, 6.5, 7.4, 14.1, 14.3, 14.4, 17.1, 17.3_
    - _Commit: feat(habits): implement habit CRUD endpoints_

  - [x] 7.3 Write property test for habit ownership
    - **Property 4: Habit Ownership**
    - **Validates: Requirements 5.2, 6.2, 7.3, 8.3, 15.1, 15.2, 15.3, 15.5**
    - Test that users can only access their own habits
    - Test that users cannot modify other users' habits
    - Test that users cannot delete other users' habits

  - [x] 7.4 Write property test for habit creation ownership
    - **Property 9: Habit Creation Ownership**
    - **Validates: Requirements 4.5**
    - Test that created habits are always associated with creator's userId

  - [x] 7.5 Write unit tests for habit CRUD
    - Test create habit with valid name
    - Test create habit with blank name returns 400
    - Test create habit with name > 100 chars returns 400
    - Test get habits returns only user's habits
    - Test update habit with valid name
    - Test update habit not owned returns 404
    - Test delete habit removes habit and records

- [x] 8. Daily Habit Tracking
  - [x] 8.1 Create habit record entity and repository
    - Create `RecordStatus` enum with DONE value
    - Create `HabitRecord` entity with id, habitId, date, status fields
    - Add unique constraint on (habitId, date) combination
    - Use LocalDate type for date field
    - Set status default to DONE
    - Create `HabitRecordRepository` interface
    - Add `findByHabitIdAndDate(habitId: Long, date: LocalDate): HabitRecord?` method
    - Add `findByHabitIdOrderByDateDesc(habitId: Long): List<HabitRecord>` method
    - _Requirements: 8.6, 8.7, 13.2, 21.1, 21.3_
    - _Commit: feat(habits): add habit record entity_

  - [x] 8.2 Implement habit check endpoint with upsert logic
    - Add `checkHabit(habitId: Long, userId: Long, date: LocalDate): HabitRecord` to HabitService
    - Verify habit ownership before creating record
    - Implement upsert logic: check if record exists, update if yes, create if no
    - Create `CheckHabitRequest` DTO with date field validation
    - Create `HabitRecordResponse` DTO with habitId, date, status
    - Add POST /habits/{id}/check endpoint to HabitController
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 14.5, 21.2_
    - _Commit: feat(habits): implement habit check endpoint_

  - [x] 8.3 Write property test for one record per habit per day
    - **Property 3: One Record Per Habit Per Day**
    - **Validates: Requirements 8.2, 8.6, 13.2**
    - Test that checking same habit twice on same date updates existing record
    - Test that only one record exists per (habitId, date) combination
    - Test that database constraint prevents duplicates

  - [x] 8.4 Write property test for LocalDate timezone safety
    - **Property 7: LocalDate Timezone Safety**
    - **Validates: Requirements 8.7, 9.5, 19.1, 19.2, 19.3**
    - Test that date calculations work correctly across month boundaries
    - Test that date.minusDays(n).plusDays(n) == date
    - Test that LocalDate operations are timezone-independent

  - [x] 8.5 Write unit tests for daily tracking
    - Test check habit creates new record
    - Test check habit updates existing record (upsert)
    - Test check habit for non-owned habit returns 404
    - Test check habit without authentication returns 401
    - Test invalid date format returns 400

- [x] 9. Checkpoint - Ensure habit tracking works correctly
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 10. Streak Calculation Algorithm
  - [x] 10.1 Implement streak calculation service
    - Create `StatsService` class
    - Implement `calculateStreak(habitId: Long): Int` method
    - Fetch habit records sorted by date descending
    - Traverse backwards from today using LocalDate.minusDays(1)
    - Count consecutive days where status is DONE
    - Handle case where today is not completed (start from yesterday)
    - Stop counting at first gap in dates
    - Return 0 for habits with no records
    - Ensure streak is always non-negative and <= total records
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6, 9.7, 19.3, 19.4_
    - _Commit: feat(stats): implement streak calculation algorithm_

  - [x] 10.2 Write property test for streak calculation correctness
    - **Property 5: Streak Calculation Correctness**
    - **Validates: Requirements 9.1, 9.2, 9.3, 9.6, 9.7**
    - Test that streak is always non-negative
    - Test that streak never exceeds total records
    - Test that streak represents consecutive days from today or yesterday
    - Test that gaps in dates break the streak

  - [x] 10.3 Write unit tests for streak calculation
    - Test streak with consecutive days from today
    - Test streak with consecutive days from yesterday (today not done)
    - Test streak stops at gap
    - Test streak returns 0 for no records
    - Test streak handles month/year boundaries correctly

- [ ] 11. Statistics and Analytics Endpoint
  - [x] 11.1 Implement statistics endpoint with dynamic streak calculation
    - Create `HabitStats` DTO with habitId, habitName, currentStreak, totalCompletions
    - Create `StatsResponse` DTO with list of HabitStats
    - Implement `calculateStats(userId: Long): StatsResponse` in StatsService
    - Fetch all habits for user
    - For each habit, calculate streak dynamically and count total completions
    - Create `StatsController` with GET /stats endpoint
    - Extract userId from @AuthenticationPrincipal
    - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_
    - _Commit: feat(stats): implement stats endpoint_

  - [x] 11.2 Write property test for statistics completeness
    - **Property 12: Statistics Completeness**
    - **Validates: Requirements 10.1, 5.1**
    - Test that stats include all user habits
    - Test that stats do not include other users' habits

  - [x] 11.3 Write property test for dynamic streak calculation
    - **Property 13: Dynamic Streak Calculation**
    - **Validates: Requirements 10.4, 10.5**
    - Test that streaks are calculated from records, not stored
    - Test that streak changes when new records are added

  - [x] 11.4 Write unit tests for statistics endpoint
    - Test stats return data for all user habits
    - Test stats include correct streak and total completions
    - Test stats without authentication returns 401

- [x] 12. Checkpoint - Ensure statistics work correctly
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 13. Backend Testing - Unit Tests
  - [x] 13.1 Write comprehensive unit tests for AuthService
    - Test signup creates user with hashed password and verified=false
    - Test signup generates verification token with 24-hour expiry
    - Test signup with duplicate email throws exception
    - Test verifyEmail updates verified status to true
    - Test verifyEmail with expired token throws exception
    - Test verifyEmail deletes token after use
    - Test login with valid credentials returns JWT
    - Test login with invalid credentials throws exception
    - Test login with unverified user throws exception
    - _Requirements: 1.1, 1.2, 1.3, 1.6, 2.1, 2.2, 2.3, 2.4, 3.1, 3.2, 3.3_

  - [x] 13.2 Write comprehensive unit tests for HabitService
    - Test createHabit with DAILY frequency
    - Test createHabit with non-DAILY frequency throws exception
    - Test getHabits returns only user's habits
    - Test updateHabit updates name
    - Test updateHabit for non-owned habit throws exception
    - Test deleteHabit removes habit
    - Test deleteHabit removes associated records (cascading)
    - Test checkHabit creates new record
    - Test checkHabit updates existing record
    - Test checkHabit for non-owned habit throws exception
    - _Requirements: 4.1, 4.5, 5.1, 5.2, 6.1, 6.2, 7.1, 7.2, 7.3, 8.1, 8.2, 8.3_

  - [x] 13.3 Write comprehensive unit tests for StatsService
    - Test calculateStreak with consecutive days from today
    - Test calculateStreak with consecutive days from yesterday
    - Test calculateStreak stops at gap
    - Test calculateStreak returns 0 for no records
    - Test calculateStreak handles month boundaries
    - Test calculateStats returns all user habits
    - Test calculateStats includes correct streaks and totals
    - _Requirements: 9.1, 9.2, 9.3, 9.4, 10.1, 10.2, 10.4_

  - [x] 13.4 Write unit tests for JwtUtil
    - Test generateToken creates valid JWT with correct claims
    - Test validateToken returns true for valid token
    - Test validateToken returns false for expired token
    - Test validateToken returns false for tampered token
    - Test extractUserId returns correct user ID
    - Test extractEmail returns correct email
    - _Requirements: 3.4, 3.5, 3.6, 3.7, 12.3, 12.4, 12.5_

- [x] 14. Backend Testing - Integration Tests
  - [x] 14.1 Write integration test for complete user journey
    - Test signup → verify email → login → create habit → check habit → get stats
    - Verify JWT token is returned and works for authenticated requests
    - Verify streak calculation is correct after checking habits
    - Use real database (H2 or Testcontainers with MariaDB)
    - _Requirements: 1.1, 2.1, 3.1, 4.1, 8.1, 10.1_

  - [x] 14.2 Write integration test for authorization enforcement
    - Test user A cannot access user B's habits
    - Test user A cannot modify user B's habits
    - Test user A cannot delete user B's habits
    - Test user A cannot check user B's habits
    - _Requirements: 15.1, 15.2, 15.3, 15.5_

  - [x] 14.3 Write integration test for cascading deletion
    - Test deleting habit also deletes all associated records
    - Verify referential integrity is maintained
    - _Requirements: 7.2, 13.4_

- [x] 15. Checkpoint - Ensure all backend tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [x] 16. Frontend - Project Setup and Authentication
  - [x] 16.1 Initialize React project with routing and API client
    - Create React app using Vite
    - Install dependencies: react-router-dom, axios
    - Configure axios base URL to backend API
    - Create axios interceptor to include JWT token in requests
    - Set up React Router with routes for signup, login, habits, stats
    - _Requirements: 24.1, 24.3, 28.1_
    - _Commit: feat(frontend): initialize React project_

  - [x] 16.2 Create authentication context and token storage
    - Create AuthContext with login, logout, and user state
    - Implement JWT token storage in localStorage
    - Implement token retrieval on page refresh
    - Implement automatic logout on token expiration
    - Create ProtectedRoute component for authenticated routes
    - _Requirements: 23.1, 23.2, 23.3, 23.4, 28.2, 28.3_
    - _Commit: feat(frontend): implement auth pages_

  - [x] 16.3 Create signup and login pages
    - Create SignupPage with email and password inputs
    - Add form validation for email format and password length
    - Handle signup API call and display success/error messages
    - Create LoginPage with email and password inputs
    - Handle login API call and store JWT token
    - Redirect to habits page on successful login
    - Create EmailVerificationPage for verification success message
    - _Requirements: 23.1, 24.2, 25.5_
    - _Commit: feat(frontend): implement auth pages_

- [ ] 17. Frontend - Habit Management UI
  - [ ] 17.1 Create habit list page with CRUD operations
    - Create HabitsPage component
    - Fetch and display all habits on mount
    - Create HabitForm component for creating new habits
    - Add create habit functionality with API call
    - Add edit habit functionality with inline form
    - Add delete habit functionality with confirmation dialog
    - Display success and error messages for all operations
    - Handle 401 errors by redirecting to login
    - _Requirements: 25.1, 25.2, 25.3, 25.4, 25.5, 24.2, 24.4_
    - _Commit: feat(frontend): implement habit management UI_

  - [ ] 17.2 Add daily tracking functionality to habit list
    - Add check button next to each habit
    - Implement check habit API call for today's date
    - Display visual indicator for habits completed today
    - Add date picker to mark habits for past dates
    - Update UI immediately after checking habit
    - Handle errors gracefully with user-friendly messages
    - _Requirements: 26.1, 26.2, 26.3, 26.4, 26.5, 24.4_
    - _Commit: feat(frontend): implement habit management UI_

- [ ] 18. Frontend - Statistics Dashboard
  - [ ] 18.1 Create statistics page with streak display
    - Create StatsPage component
    - Fetch statistics on mount
    - Display habit name, current streak, and total completions for each habit
    - Add visual formatting for streaks (e.g., fire emoji for active streaks)
    - Refresh statistics when navigating to page
    - Add basic styling for readability
    - Handle 401 errors by redirecting to login
    - _Requirements: 27.1, 27.2, 27.3, 27.4, 24.2, 24.4_
    - _Commit: feat(frontend): implement stats dashboard_

  - [ ] 18.2 Add navigation between pages
    - Create navigation bar component
    - Add links to Habits and Stats pages
    - Add logout button that clears token and redirects to login
    - Display current user email in navigation
    - Add basic styling for navigation
    - _Requirements: 28.1, 28.4, 23.4_
    - _Commit: feat(frontend): implement stats dashboard_

- [ ] 19. Checkpoint - Ensure frontend works end-to-end
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 20. Production Readiness and Configuration
  - [ ] 20.1 Configure environment variables and production settings
    - Create application-prod.yaml for production profile
    - Move all sensitive configuration to environment variables
    - Configure database connection from env vars (DB_URL, DB_USERNAME, DB_PASSWORD)
    - Configure JWT secret from env var (JWT_SECRET)
    - Configure SMTP settings from env vars (MAIL_HOST, MAIL_PORT, etc.)
    - Configure server port from env var (SERVER_PORT)
    - Add default values for development environment
    - _Requirements: 29.1, 29.2, 29.3, 29.4, 29.5, 12.2_
    - _Commit: chore: prepare for production deployment_

  - [ ] 20.2 Configure CORS and HTTPS for production
    - Update CORS configuration to read allowed origins from env var
    - Restrict CORS to production frontend domain only in production
    - Configure HTTPS redirect for production
    - Add Strict-Transport-Security header
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 30.1, 30.2_
    - _Commit: chore: prepare for production deployment_

  - [ ] 20.3 Configure database connection pooling and error logging
    - Configure HikariCP connection pool settings in application.yaml
    - Set maximum pool size, minimum idle, connection timeout
    - Configure error logging without exposing sensitive data
    - Add graceful shutdown configuration
    - _Requirements: 30.3, 30.4, 30.5, 17.4_
    - _Commit: chore: prepare for production deployment_

  - [ ] 20.4 Add database indexes for performance
    - Create migration or schema update for indexes
    - Add index on users.email (already enforced by unique constraint)
    - Add index on habits.user_id
    - Add index on habit_records.habit_id
    - Add index on habit_records.date
    - Verify composite unique index on habit_records(habit_id, date)
    - _Requirements: 22.1, 22.2, 22.3, 22.4, 22.5_
    - _Commit: chore: prepare for production deployment_

- [ ] 21. Documentation and Deployment
  - [ ] 21.1 Create comprehensive API documentation
    - Document all API endpoints with request/response examples
    - Document authentication flow with JWT
    - Document error responses and status codes
    - Add examples for common use cases
    - Document environment variables
    - _Requirements: 17.1, 17.2, 17.3_
    - _Commit: docs: add API documentation and README_

  - [ ] 21.2 Create setup and deployment instructions
    - Write README with project overview
    - Add prerequisites (Java 17, MariaDB, Node.js)
    - Add backend setup instructions
    - Add frontend setup instructions
    - Add environment variable configuration guide
    - Add database setup instructions
    - Add instructions for running in development
    - Add instructions for production deployment
    - _Requirements: 29.1, 29.2, 29.3, 29.4, 29.5_
    - _Commit: docs: add API documentation and README_

- [ ] 22. Final Checkpoint - Complete system verification
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- Tasks marked with `*` are optional testing tasks and can be skipped for faster MVP delivery
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- Integration tests validate end-to-end flows
- The commit messages follow conventional commit format: `<type>(<scope>): <description>`
- Each major task includes the commit message from the design document's implementation plan
