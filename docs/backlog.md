# Project Backlog: Spark in Java Web Application Development

## User Stories

### User Story 1
**As a** store administrator  
**I want to** manage users (create, read, update, delete) via an API  
**So that** I can control who has access to the system

**Acceptance Criteria:**
- [ ] API exposes endpoints for user CRUD operations
- [ ] Requests return appropriate status codes and error messages
- [ ] User data is validated and persisted

### User Story 2
**As a** store visitor  
**I want to** browse and filter collectible items  
**So that** I can easily find items of interest

**Acceptance Criteria:**
- [x] Website shows a list of collectible items (✅ Completed Sprint 2)
- [x] Visitors can filter items by category or price (✅ Completed Sprint 2)
- [x] Filtering updates the displayed items without reloading the page (✅ Completed Sprint 2 - Client-side filtering)

### User Story 3
**As a** store administrator  
**I want to** add, edit, and remove item offers through a web form  
**So that** I can keep the store's inventory up to date

**Acceptance Criteria:**
- [x] Admin panel with item management form is present (✅ Completed Sprint 2)
- [x] Item changes are reflected in the item list (✅ Completed Sprint 2)
- [x] Input validation and error handling are implemented (✅ Completed Sprint 2)

### User Story 4
**As a** store visitor  
**I want to** see real-time price updates for items  
**So that** I can make informed purchasing decisions based on current prices

**Acceptance Criteria:**
- [x] Price changes are pushed instantly to the website via WebSockets (✅ Completed Sprint 3)
- [x] No manual refresh is required to view updates (✅ Completed Sprint 3)
- [x] Real-time system is robust against failures (✅ Completed Sprint 3 - Auto-reconnect implemented)

## Requirements Tracking Table 1: User Stories and Requirements

| User Story | Requirements |
|------------|-------------|
| User Story 1 | - Requirement 1.1: API endpoints for user CRUD<br>- Requirement 1.2: Input validation<br>- Requirement 1.3: Error handling and status codes |
| User Story 2 | - Requirement 2.1: Item browsing view<br>- Requirement 2.2: Filtering logic<br>- Requirement 2.3: Dynamic client updates |
| User Story 3 | - Requirement 3.1: Admin item management form<br>- Requirement 3.2: Inventory update logic<br>- Requirement 3.3: Validation and feedback |
| User Story 4 | - Requirement 4.1: Real-time price update via WebSockets<br>- Requirement 4.2: Resilience to connection issues |

## Requirements Tracking Table 2: Prioritized Requirements

| Requirements | Stages | Time Estimation (hours) | Deliverables | Status |
|-------------|--------|------------------------|--------------|--------|
| Requirement 1.1: API endpoints for user CRUD | Sprint 1 | 5 | User API implemented | ✅ |
| Requirement 1.2: Input validation | Sprint 1 | 3 | User API validation | ✅ |
| Requirement 1.3: Error handling and status codes | Sprint 1 | 3 | User API error handling | ✅ |
| Requirement 2.1: Item browsing view | Sprint 2 | 4 | Item list page | ✅ |
| Requirement 2.2: Filtering logic | Sprint 2 | 3 | Filtering functionality | ✅ |
| Requirement 2.3: Dynamic client updates | Sprint 2 | 3 | Live item update mechanism | ✅ (Client-side filtering) |
| Requirement 3.1: Admin item management form | Sprint 2 | 5 | Admin form for item management | ✅ |
| Requirement 3.2: Inventory update logic | Sprint 2 | 3 | Inventory update | ✅ |
| Requirement 3.3: Validation and feedback | Sprint 2 | 2 | Form validation and feedback | ✅ |
| Requirement 4.1: Real-time price update via WebSockets | Sprint 3 | 6 | WebSocket price update | ✅ |
| Requirement 4.2: Resilience to connection issues | Sprint 3 | 2 | Robust real-time system | ✅ |
