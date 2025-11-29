# Interview Scheduler Backend

A Spring Boot REST API for managing interview scheduling with weekly availability patterns, slot generation, and booking management.

## Tech Stack

- Java 21
- Spring Boot 3.4.12
- Spring Data JPA
- MySQL (production) / H2 (testing)
- Lombok
- Spring Retry

## Features

- **Weekly Availability**: Interviewers define recurring weekly schedules (e.g., Monday 9-5, Wednesday 10-4)
- **Slot Generation**: Automatically generate interview slots for the next N weeks based on availability
- **Booking System**: Candidates can book available slots with validation rules
- **Race Condition Handling**: Pessimistic locking and retry mechanism for concurrent bookings
- **Pagination**: Both offset-based and cursor-based pagination support
- **Scheduled Tasks**: Automatic expiration of past available slots

## API Endpoints

### Health Check
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/slots/health` | Health check endpoint |

### Interviewers
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/interviewers` | Create interviewer with weekly availability |
| GET | `/api/interviewers` | Get all interviewers |
| GET | `/api/interviewers/{id}` | Get interviewer by ID |
| DELETE | `/api/interviewers/{id}` | Delete interviewer |

### Candidates
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/candidates` | Create candidate |
| GET | `/api/candidates` | Get all candidates |
| GET | `/api/candidates/{id}` | Get candidate by ID |
| GET | `/api/candidates/email/{email}` | Get candidate by email |
| DELETE | `/api/candidates/{id}` | Delete candidate |

### Slots
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/slots/generate` | Generate slots for interviewer |
| GET | `/api/slots/available` | Get available slots (offset pagination) |
| GET | `/api/slots/available/cursor` | Get available slots (cursor pagination) |
| GET | `/api/slots/available/interviewer/{id}` | Get available slots for interviewer |
| GET | `/api/slots/{id}` | Get slot by ID |

### Bookings
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/bookings` | Book a slot |
| GET | `/api/bookings/{id}` | Get booking by ID |
| PUT | `/api/bookings/{id}` | Update/reschedule booking |
| DELETE | `/api/bookings/{id}` | Cancel booking |
| POST | `/api/bookings/{id}/confirm` | Confirm booking |
| GET | `/api/bookings/candidate/{id}` | Get bookings by candidate |
| GET | `/api/bookings/slot/{id}` | Get booking by slot |

## Business Rules

1. **One Booking Per Candidate**: A candidate can only have one active booking within the available time window
2. **Max Interviews Per Week**: Interviewers have a configurable maximum number of interviews per week
3. **No Past Bookings**: Cannot book slots that have already passed
4. **Slot Status Flow**: AVAILABLE → BOOKED → CONFIRMED (or CANCELLED)

## Running the Application

### Prerequisites
- Java 21
- MySQL 8.0+
- Gradle

### Configuration

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/interview_scheduler
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Build and Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The server starts at `http://localhost:8080`

### Running Tests

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

## Sample API Usage

### Create an Interviewer

```bash
curl -X POST http://localhost:8080/api/interviewers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Smith",
    "email": "john.smith@company.com",
    "maxInterviewsPerWeek": 10,
    "slotDurationMinutes": 60,
    "weeklyAvailabilities": [
      {
        "dayOfWeek": "MONDAY",
        "startTime": "09:00",
        "endTime": "17:00"
      },
      {
        "dayOfWeek": "WEDNESDAY",
        "startTime": "10:00",
        "endTime": "16:00"
      }
    ]
  }'
```

### Generate Slots

```bash
curl -X POST http://localhost:8080/api/slots/generate \
  -H "Content-Type: application/json" \
  -d '{
    "interviewerId": 1,
    "weeksToGenerate": 2
  }'
```

### Create a Candidate

```bash
curl -X POST http://localhost:8080/api/candidates \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "email": "jane.doe@email.com",
    "phoneNumber": "+1-555-1234"
  }'
```

### Book a Slot

```bash
curl -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{
    "slotId": 1,
    "candidateId": 1,
    "bookingNotes": "Technical interview"
  }'
```

### Get Available Slots (Cursor Pagination)

```bash
curl "http://localhost:8080/api/slots/available/cursor?cursor=0&limit=10"
```

## Project Structure

```
src/main/java/com/interview_scheduler/backend/
├── config/          # Configuration classes
├── controller/      # REST controllers
├── dto/
│   ├── request/     # Request DTOs
│   └── response/    # Response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions
├── repository/      # Spring Data repositories
└── service/
    └── impl/        # Service implementations
```
