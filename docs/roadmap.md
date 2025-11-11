# Project Roadmap: Collectibles Store - Authentication Feature

## Objectives

### General Objective
To enhance the Collectibles Store API by designing, implementing, and testing a secure user authentication system using stored procedures, while following best practices for development on a separate branch to ensure production stability.

### Specific Objectives
1.  **Design and Implement Database Schema:** Create the necessary database tables (e.g., `users`, `roles`) and stored procedures for managing user authentication and metadata.
2.  **Develop Authentication API:** Build the backend logic and RESTful API endpoints in Java for user registration, login, and session management using the Spark framework.
3.  **Ensure Code Quality with Unit Tests:** Implement a comprehensive suite of unit tests with JUnit for the new authentication module, aiming for at least 90% code coverage to guarantee reliability.
4.  **Produce Comprehensive Documentation:** Create detailed documentation for the new feature, including API endpoints, database schema, setup instructions, and maintain a clean, well-organized GitHub repository.

## Timeline and Milestones

### Sprint 1: Database and Core Logic (Nov 6, 2025 - Nov 7, 2025)
-   Finalize the database schema design for user authentication.
-   Implement tables (`users`, etc.) and relationships in the MySQL database.
-   Develop and test all required stored procedures for user management (e.g., `sp_createUser`, `sp_findUserByEmail`).
-   Create Java data models and initial Data Access Objects (DAO) for the new tables.
-   Work will be done in a new feature branch (e.g., `feature/authentication`).

### Sprint 2: API Development and Testing (Nov 8, 2025 - Nov 10, 2025)
-   Develop the RESTful API endpoints for registration, login, and token validation.
-   Integrate the authentication logic with the existing Spark framework application.
-   Write comprehensive unit tests for all services, DAOs, and utility classes using JUnit.
-   Verify code coverage for the new Java module, ensuring it meets the 90% target.
-   Ensure all JUnit tests pass successfully.

### Sprint 3: Documentation and Refinement (Nov 11, 2025 - Nov 12, 2025)
-   Draft complete project documentation for the new authentication feature.
-   Document the API endpoints using a tool like Swagger or in Markdown.
-   Add detailed comments within the Java code and stored procedures.
-   Update the main `README.md` to include instructions on the new feature.
-   Prepare the feature branch for merging into the main branch.

### Final Delivery (Nov 13-16, 2025)
-   Complete final integration testing.
-   Finalize all project documentation and peer-review for clarity.
-   Submit the final project, including a Pull Request from the feature branch to the main branch.

## Deliverables

### Sprint 1
-   **SQL Scripts**: Scripts for creating new tables and stored procedures.
-   **Feature Branch**: A new branch in the GitHub repository for development.
-   **Java Models/DAOs**: Core Java classes for the database interaction.

### Sprint 2
-   **Authentication API Endpoints**: Functional endpoints for user management.
-   **JUnit Test Suite**: A suite of unit tests for the authentication module.
-   **Code Coverage Report**: A report showing at least 90% test coverage for the new code.

### Sprint 3
-   **API Documentation**: Detailed documentation of all new endpoints.
-   **Updated GitHub Repository**: A clean repository with the new feature branch ready for review.

### Final Delivery
-   **Pull Request**: A well-documented Pull Request to merge the feature into the main branch.
-   **Final Project**: The complete, tested, and documented authentication feature integrated into the API.

## Project Gantt Chart
![Project Gantt Chart](project_gantt.png)

## Technologies and Tools
-   **Java**: Core language for the API.
-   **Spark Framework**: Web framework for building the RESTful API.
-   **MySQL**: Database for storing user and collectibles data.
-   **Stored Procedures**: For all database interactions related to authentication.
-   **JUnit**: Framework for unit testing the Java module.
-   **Git & GitHub**: For version control and repository management in a feature branch.
-   **Render**: Platform where the production environment is hosted.

## Stakeholders
-   **Aldo Calderon**: Lead Developer.
-   **API Consumers**: Users or frontend applications that will use the authentication service.

## Risk Management

| Risk                                     | Impact | Probability | Mitigation Strategy                                                                                             |
| ---------------------------------------- | ------ | ----------- | --------------------------------------------------------------------------------------------------------------- |
| Stored procedures are difficult to debug | Medium | Medium      | Develop procedures incrementally. Ensure comprehensive unit testing of the Java code that calls the procedures. |
| Security vulnerabilities in auth logic   | High   | Medium      | Follow security best practices (e.g., password hashing with bcrypt, secure token generation). Conduct peer reviews. |
| Breaking changes to the existing API     | High   | Low         | Develop entirely on a separate feature branch. Perform thorough integration testing before merging to main.      |
| Delays in API development                | Medium | Medium      | Define a clear contract for the API endpoints and database schema early in Sprint 1. Prioritize core functionality. |