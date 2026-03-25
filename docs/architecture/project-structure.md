# Project Structure

## Goal

This project keeps a single Spring Boot application for now, but the package layout follows the long-term ticketing plan.
The intent is to let us implement each slice without reshuffling packages later.

## Package Layout

- `com.ticketaca.global`
  - shared configuration, exceptions, base entities, constants
- `com.ticketaca.auth`
  - email sign-up, Kakao login, JWT, refresh token, identities
- `com.ticketaca.event`
  - events, sessions, seat maps, inventory, public catalog queries
- `com.ticketaca.queue`
  - waiting room, admission token, queue policies
- `com.ticketaca.booking`
  - seat hold, booking workflow, order creation
- `com.ticketaca.payment`
  - payment confirmation, webhook, refund, idempotency
- `com.ticketaca.admin`
  - internal APIs for event, session, seat, queue policy management
- `com.ticketaca.notification`
  - async notification consumers and outbound delivery

## Dependency Direction

- Domain packages should depend on `global`, not on each other through controllers.
- Cross-domain interaction should happen through services, events, or explicit interfaces.
- Queue, booking, and payment are the first candidates for later service extraction.

## Current Focus

This repository only prepares the structure and shared dependencies.
Actual domain implementation should continue slice by slice, starting with event/admin read-write foundations.