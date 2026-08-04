import { Component, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

type StationResponse = {
  id: number;
  name: string;
  stationOrder: number;
};

type SeatResponse = {
  id: number;
  seatNumber: string;
  coachNumber: string;
  coachType: string;
};

type BookingResponse = {
  id: number;
  passengerName: string;
  seatId: number;
  seatNumber: string;
  coachNumber: string;
  coachType: string;
  origin: string;
  destination: string;
  fare: number;
  bookingTime: string;
};

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="app-shell">
      <section class="hero-panel container py-5">
        <div class="hero-copy">
          <p class="eyebrow">Segment-based reservations</p>
          <h1>Colombo Fort to Badulla seat booking</h1>
          <p class="hero-text">
            Reserve the same physical seat for multiple non-overlapping legs, with fares based only on the distance travelled.
          </p>
        </div>

        <div class="stats-grid">
          <div class="stat-card">
            <span class="stat-label">Stations loaded</span>
            <strong>{{ stations.length }}</strong>
          </div>
          <div class="stat-card">
            <span class="stat-label">Available seats</span>
            <strong>{{ availableSeats.length }}</strong>
          </div>
          <div class="stat-card">
            <span class="stat-label">Recent bookings</span>
            <strong>{{ recentBookings.length }}</strong>
          </div>
        </div>
      </section>

      <section class="container pb-5">
        <div class="surface-card p-4 p-md-5">
          <div class="row g-4 align-items-end">
            <div class="col-lg-3">
              <label class="form-label">Origin</label>
              <select class="form-select form-select-lg" [(ngModel)]="originName" (change)="onSegmentChange()">
                <option *ngFor="let station of stations" [value]="station.name">{{ station.name }}</option>
              </select>
            </div>
            <div class="col-lg-3">
              <label class="form-label">Destination</label>
              <select class="form-select form-select-lg" [(ngModel)]="destinationName" (change)="onSegmentChange()">
                <option *ngFor="let station of stations" [value]="station.name">{{ station.name }}</option>
              </select>
            </div>
            <div class="col-lg-3">
              <label class="form-label">Passenger</label>
              <input class="form-control form-control-lg" [(ngModel)]="passengerName" placeholder="Passenger name" />
            </div>
            <div class="col-lg-3 d-grid">
              <button class="btn btn-primary btn-lg" (click)="searchAvailableSeats()" [disabled]="loading">
                {{ loading ? 'Checking...' : 'Check availability' }}
              </button>
            </div>
          </div>

          <div class="alert alert-warning mt-4" *ngIf="errorMessage">{{ errorMessage }}</div>
          <div class="alert alert-success mt-4" *ngIf="successMessage">{{ successMessage }}</div>

          <div class="availability-panel mt-4">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h2 class="section-title mb-1">Available reserved seats</h2>
                <p class="section-subtitle mb-0">{{ originName }} → {{ destinationName }}</p>
              </div>
              <span class="badge text-bg-light">Fare: LKR {{ estimatedFare }}</span>
            </div>

            <div class="seat-grid" *ngIf="availableSeats.length; else emptySeats">
              <button
                class="seat-chip"
                *ngFor="let seat of availableSeats"
                (click)="bookSeat(seat.id)"
                [disabled]="bookingInProgress"
              >
                <span>{{ seat.seatNumber }}</span>
                <small>{{ seat.coachNumber }} · {{ seat.coachType }}</small>
              </button>
            </div>

            <ng-template #emptySeats>
              <div class="empty-state">
                <h3>No seats available for this leg</h3>
                <p>Try a different segment or wait for an adjacent booking to clear the seat.</p>
              </div>
            </ng-template>
          </div>
        </div>

        <div class="row g-4 mt-4">
          <div class="col-lg-7">
            <div class="surface-card p-4 h-100">
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                  <h2 class="section-title mb-1">Recent bookings</h2>
                  <p class="section-subtitle mb-0">Latest confirmed reservations from the backend</p>
                </div>
                <button class="btn btn-outline-secondary btn-sm" (click)="loadBookings()">Refresh</button>
              </div>

              <div class="booking-list" *ngIf="recentBookings.length; else noBookings">
                <article class="booking-card" *ngFor="let booking of recentBookings">
                  <div>
                    <strong>{{ booking.passengerName }}</strong>
                    <div class="booking-meta">{{ booking.origin }} → {{ booking.destination }}</div>
                  </div>
                  <div class="text-end">
                    <div class="booking-seat">{{ booking.coachNumber }} / {{ booking.seatNumber }}</div>
                    <div class="booking-fare">LKR {{ booking.fare }}</div>
                  </div>
                </article>
              </div>

              <ng-template #noBookings>
                <div class="empty-state compact">
                  <h3>No bookings yet</h3>
                  <p>Create a booking to see it appear here.</p>
                </div>
              </ng-template>
            </div>
          </div>

          <div class="col-lg-5">
            <div class="surface-card p-4 h-100">
              <h2 class="section-title mb-1">How it works</h2>
              <p class="section-subtitle">Why the same seat can be sold twice</p>
              <ul class="feature-list">
                <li>Bookings are stored as station ranges, not just a seat flag.</li>
                <li>Overlaps are blocked with a seat-level database lock.</li>
                <li>Adjacent segments can reuse the same seat without conflict.</li>
                <li>Fares are based on station distance only.</li>
              </ul>
            </div>
          </div>
        </div>
      </section>
    </div>
  `
})
export class HomeComponent implements OnInit {
  readonly apiBaseUrl = 'http://localhost:8080/api';

  stations: StationResponse[] = [];
  originName = 'Colombo Fort';
  destinationName = 'Kandy';
  passengerName = 'Demo Passenger';
  availableSeats: SeatResponse[] = [];
  recentBookings: BookingResponse[] = [];
  loading = false;
  bookingInProgress = false;
  errorMessage = '';
  successMessage = '';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadStations();
    this.loadBookings();
  }

  get estimatedFare(): number {
    const origin = this.stations.find((station) => station.name === this.originName);
    const destination = this.stations.find((station) => station.name === this.destinationName);
    if (!origin || !destination || destination.stationOrder <= origin.stationOrder) {
      return 0;
    }
    return (destination.stationOrder - origin.stationOrder) * 150;
  }

  onSegmentChange() {
    this.errorMessage = '';
    this.successMessage = '';
  }

  loadStations() {
    this.http.get<StationResponse[]>(`${this.apiBaseUrl}/stations`).subscribe({
      next: (stations) => {
        this.stations = stations;
        if (stations.length) {
          this.originName = stations[0].name;
          this.destinationName = stations[Math.min(3, stations.length - 1)].name;
          this.searchAvailableSeats();
        }
      },
      error: () => {
        this.errorMessage = 'Could not load stations from the server.';
      }
    });
  }

  loadBookings() {
    this.http.get<BookingResponse[]>(`${this.apiBaseUrl}/bookings`).subscribe({
      next: (bookings) => {
        this.recentBookings = bookings;
      },
      error: () => {
        this.errorMessage = 'Could not load booking history.';
      }
    });
  }

  searchAvailableSeats() {
    if (this.originName === this.destinationName) {
      this.errorMessage = 'Origin and destination must be different.';
      this.availableSeats = [];
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.http.get<SeatResponse[]>(`${this.apiBaseUrl}/bookings/available?origin=${encodeURIComponent(this.originName)}&destination=${encodeURIComponent(this.destinationName)}`).subscribe({
      next: (seats) => {
        this.availableSeats = seats;
        this.loading = false;
        this.successMessage = seats.length ? 'Available seats loaded.' : 'No seats available for this segment.';
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Could not load available seats.';
      }
    });
  }

  bookSeat(seatId: number) {
    this.bookingInProgress = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.http.post<BookingResponse>(`${this.apiBaseUrl}/bookings`, {
      passengerName: this.passengerName.trim(),
      origin: this.originName,
      destination: this.destinationName,
      seatId
    }).subscribe({
      next: () => {
        this.bookingInProgress = false;
        this.successMessage = 'Booking created successfully.';
        this.searchAvailableSeats();
        this.loadBookings();
      },
      error: (error) => {
        this.bookingInProgress = false;
        this.errorMessage = this.extractErrorMessage(error, 'Booking could not be completed.');
      }
    });
  }

  private extractErrorMessage(error: unknown, fallback: string): string {
    const response = error as { error?: { message?: string } | string };
    if (typeof response?.error === 'string') {
      return response.error;
    }
    if (response?.error && typeof response.error === 'object' && 'message' in response.error && response.error.message) {
      return response.error.message;
    }
    return fallback;
  }
}
