Wendy’s Family Tree is a full-stack web application for managing horses, owners, and their family relationships.
Developed as part of a software engineering course at TU Wien, the project demonstrates modern Spring Boot backend design, Angular frontend development, input validation, REST architecture, and automated testing.

Key Features:

Full CRUD management for horses and owners

Parent–child relationship tracking (mother, father, offspring)

Comprehensive validation (age rules, parent sex constraints, no self-parenting, etc.)

Search and filter capabilities (name, description, sex, owner name, date filters, limit)

Detailed horse view including owner mapping and family info

SQL-based seed data via a dedicated datagen profile

Complete integration test suite (all tests passing)

Technology Stack:

Backend: Spring Boot (Java), JDBC, H2 Database

Frontend: Angular, TypeScript

Testing: JUnit 5, AssertJ, MockMvc

Build Tools: Maven

Additional: Checkstyle, RESTful API design, DTO/DAO patterns

**Backend & Frontend:**
```bash
cd backend
mvn clean package
mvn spring-boot:run -Dspring-boot.run.profiles=datagen


cd frontend
npm ci
npm run start
