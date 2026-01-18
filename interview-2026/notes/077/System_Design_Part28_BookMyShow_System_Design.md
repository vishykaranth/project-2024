# BOOKMYSHOW System Design | FANDANGO System Design | Software Architecture for Online Ticket Booking

## Overview

Online ticket booking systems must handle seat selection, concurrent bookings, payment processing, and prevent double-booking. This requires careful concurrency control and inventory management.

## System Requirements

- Browse movies/theaters
- Select seats
- Book tickets (prevent double-booking)
- Process payments
- Send confirmations
- Handle cancellations

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Ticket Booking Architecture                      │
└─────────────────────────────────────────────────────────┘

Clients              API Gateway          Services
    │                        │                        │
    ├─► Web App             │                        │
    ├─► Mobile App          │                        │
    └─► Kiosk               │                        │
        │                        │                        │
        └───Requests─────────────>│                        │
            │                    │                        │
            │                    ├───Show Service───────>│
            │                    ├───Seat Service───────>│
            │                    ├───Booking Service────>│
            │                    ├───Payment Service───>│
            │                    └───Notification──────>│
            │                    │                        │
```

## Core Components

### 1. Show Service
- Movie/show information
- Showtimes
- Theater details
- Availability

### 2. Seat Selection Service
- Seat map rendering
- Seat availability
- Seat locking mechanism
- Concurrency control

### 3. Booking Service
- Create bookings
- Reserve seats
- Handle concurrent requests
- Transaction management

### 4. Payment Service
- Process payments
- Payment gateway integration
- Refund handling
- Payment confirmation

## Seat Booking Flow

```
┌─────────────────────────────────────────────────────────┐
│         Seat Booking Flow                               │
└─────────────────────────────────────────────────────────┘

1. User selects seats
    │
    ▼
2. Lock seats (temporary, 5-10 min)
    │
    ▼
3. User proceeds to payment
    │
    ▼
4. Process payment
    │
    ├─► Success: Confirm booking, unlock seats
    └─► Failure: Release lock
```

## Concurrency Control

```
┌─────────────────────────────────────────────────────────┐
│         Preventing Double-Booking                        │
└─────────────────────────────────────────────────────────┘

User A              Seat Service          User B
    │                        │                        │
    │───Lock Seat 1─────────>│                        │
    │                        │                        │
    │<──Locked───────────────│                        │
    │                        │                        │
    │                        │<──Lock Seat 1──────────│
    │                        │                        │
    │                        │<──Already Locked──────│
    │                        │                        │
```

## Database Design

```sql
Shows:
- show_id (PK)
- movie_id (FK)
- theater_id (FK)
- show_time
- total_seats

Seats:
- seat_id (PK)
- show_id (FK)
- seat_number
- row
- status (available/locked/booked)
- locked_by
- locked_until

Bookings:
- booking_id (PK)
- user_id (FK)
- show_id (FK)
- seat_ids[]
- total_amount
- status
- booking_time
```

## Summary

BookMyShow System:
- **Components**: Show, Seat, Booking, Payment services
- **Concurrency**: Seat locking mechanism
- **Payment**: Gateway integration
- **Features**: Seat selection, booking, payments, confirmations
