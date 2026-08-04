# Train Seat Booking System

A modern booking system for Sri Lanka's Colombo Fort–Badulla railway line that allows flexible seat reservations. Passengers can book the same physical seat for different parts of the journey, paying only for the distance they actually travel.

## What this solves

Traditional train booking systems lock a seat for the entire journey, even when passengers get off halfway. This means:
- Seats sit empty for long stretches while other coaches are overcrowded
- Short-distance passengers pay unfair prices (effectively subsidizing empty seats)
- Revenue is lost because seats can't be resold once the train departs

This system lets a single seat be booked by multiple passengers for non-overlapping segments. For example, one person travels Colombo Fort → Kandy, and another travels Kandy → Badulla on the same physical seat. Each pays only for their portion of the journey.

## How it works

**Segment-based booking**
- Bookings are stored as station ranges (origin to destination)
- A seat is available if no existing booking overlaps the requested segment
- Adjacent segments can reuse the same seat without conflict

**Fare calculation**
- Fares are based on the number of stations between origin and destination
- A base fare per station is configurable (default: 150 LKR per station)
- No more paying for empty seats you won't use

**Concurrency handling**
- Database locks prevent double-booking when multiple users try to book the same seat simultaneously
- Transactions ensure data consistency even under heavy load

## Design decisions

**Why station ranges instead of a simple "booked" flag?**
A simple boolean flag would block the entire seat for the full journey. By storing the actual origin and destination stations, we can detect overlaps and allow non-conflicting bookings on the same seat.

**Why pessimistic locking?**
When two users try to book the same seat at the same time, we need to ensure only one succeeds. Pessimistic locking (locking the seat row during the transaction) guarantees this without complex retry logic.

**Why configurable parameters?**
Railway routes change—new stations are added, coaches are added or removed. By making stations, coach counts, and seat capacity configurable through application properties, the system can adapt without code changes.

## Alternatives considered

**Blocking the entire seat for the full journey**
This was the original approach but rejected because it wastes capacity and is unfair to short-distance travelers who end up subsidizing empty seats.

**Complex seat-map with time intervals**
A more sophisticated model could track exact departure times and allow bookings for specific train runs. While this would be more realistic, it adds significant complexity that wasn't necessary for the core requirement of demonstrating segment-based booking logic.

## Tech stack

- **Backend**: Spring Boot 3 + Java 17 + MySQL + JPA/Hibernate
- **Frontend**: Angular 18 + TypeScript + Bootstrap
- **Deployment**: Docker Compose

## How to run

### Prerequisites
- Docker Desktop installed
- MySQL running on your machine (port 3307)
- Git for version control

### Setup

1. Clone the repository:
   ```bash
   git clone <repository-url>
   cd Train-Ticket-Booking-System-LDF
   ```

2. Create your environment file:
   ```bash
   Copy-Item .env.example .env
   ```

3. Edit `.env` with your MySQL credentials:
   ```bash
   MYSQL_ROOT_PASSWORD=your-root-password
   MYSQL_DATABASE=train_booking
   MYSQL_USER=your-mysql-username
   MYSQL_PASSWORD=your-mysql-password
   SERVER_PORT=8080
   ```

4. Start the application:
   ```bash
   docker compose up --build
   ```

5. Open your browser:
   - Frontend: http://localhost:4200
   - Backend API: http://localhost:8080

**Note:** Ensure MySQL is running on port 3307 before starting the application. The database and initial data (stations, coaches, seats) are created automatically by the DataSeeder on first startup.

## Configuration

The system behavior is controlled through `backend/src/main/resources/application.yml`:

```yaml
app:
  booking:
    base-fare-per-station: 150              # Fare per station in LKR
    route:
      stations: Colombo Fort,Ragama,...     # Station list in order
      reserved-coach-count: 3               # Number of reserved coaches
      total-coach-count: 8                  # Total coaches
      seats-per-coach: 3                    # Seats per coach
```

To add a new station or change coach capacity, update these values and restart the application.

## API endpoints

**Stations**
- `GET /api/stations` - List all stations in order

**Bookings**
- `GET /api/bookings` - List all bookings
- `POST /api/bookings` - Create a new booking
  ```json
  {
    "passengerName": "John Doe",
    "origin": "Colombo Fort",
    "destination": "Kandy",
    "seatId": 1
  }
  ```
- `GET /api/bookings/available?origin=X&destination=Y` - Find available seats for a route
- `DELETE /api/bookings` - Clear all bookings (for testing)

## Challenges faced

**Overlap detection logic**
Getting the SQL query right for detecting overlapping bookings required careful consideration of edge cases (adjacent bookings, same origin/destination). The final query checks if any existing booking on the seat starts before the requested destination and ends after the requested origin.

**Concurrency handling**
Ensuring that multiple users can attempt to book the same seat simultaneously without double-booking required implementing pessimistic locking at the database level. This guarantees that only one transaction can modify a seat at a time.

## Security notes

- Database credentials are stored in `.env` which is gitignored
- `.env.example` is provided as a template with placeholder values
- Never commit actual credentials to version control

## Future improvements

While the core requirements are met, here are potential enhancements:
- Seat map visualization showing which segments are booked
- Waitlist functionality for fully booked segments
- Admin dashboard for revenue and occupancy analytics
- Real-time availability updates using WebSockets
- User authentication and booking history per user
