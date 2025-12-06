# Wendy’s Family Tree – Horse Management System 🐎

A full-stack web application for managing horses, their owners, and multi-generation pedigrees.  
The system provides CRUD operations for horses and owners, advanced search, and a visual family tree view.

This project was implemented as an individual assignment in the **Software Engineering Project (SE PR), Winter Term 2025** at TU Wien.

---

## 📚 Table of Contents

- [Overview](#overview)
- [Domain & Features](#domain--features)
  - [User Features](#user-features)
  - [Technical Features](#technical-features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Running the Application](#running-the-application)
  - [Backend (Spring Boot)](#backend-spring-boot)
  - [Frontend (Angular)](#frontend-angular)
- [Testing](#testing)
- [Logging & Quality](#logging--quality)
- [REST API Design](#rest-api-design)
- [Data Model & Persistence](#data-model--persistence)
- [Work Log](#work-log)
- [Possible Future Improvements](#possible-future-improvements)

---

## 🌍 Overview

The application is a **horse management and pedigree tracking system** consisting of:

- a **Java / Spring Boot 3 backend** exposing a REST API  
- an **Angular 20 frontend** consuming that API  
- an **H2 relational database** for persistence  

Users can:

- create, edit, delete, and search horses
- assign owners
- manage parent relationships
- view and explore a **hierarchical family tree** of a horse’s ancestors

The project was built with a strong focus on:

- clean architecture (layered, interface-based, DI)
- testing across all backend layers
- logging, validation, and usability
- proper Git workflow and CI

---

## 🐴 Domain & Features

### User Features

The application implements the full set of user stories from the assignment specification:

- **Create horses**
  - Store horses with:
    - name (required)
    - description (optional)
    - date of birth (required)
    - biological sex (required)
    - optional image
    - optional single owner
- **Edit horses**
  - Update all attributes of an existing horse
  - Delete a horse directly from the edit view
- **Delete horses**
  - Permanently remove horses from the database
  - Remove all relations without corrupting other horses
  - Remove parent links if the deleted horse is a parent
- **Assign parents**
  - Each horse can have up to two parents
  - Two parents must not share the same biological sex
  - Parents can be set on creation and edited later
- **Horse detail view**
  - Show all data of a single horse
  - Navigate to edit view and delete from here
  - Direct navigation to parent details
- **Search horses**
  - Search by:
    - name
    - description
    - date of birth (older than given date)
    - sex
    - owner (supports partial name search)
  - Combine criteria (logical AND)
  - Empty search lists all horses
  - From search results:
    - open details
    - open edit
    - delete horse
    - open family tree
- **Owner management**
  - Create owners with:
    - name (required)
    - email (optional)
  - List all owners with name + email
- **Family tree view**
  - Show ancestors of a horse as a **collapsible tree**
  - Each node shows name and date of birth
  - Adjustable maximum number of generations
  - Initially fully expanded
  - Navigate to horse details from the tree
  - Delete a horse directly from the tree view

---

### Technical Features

The application also implements the requested **tech stories**, including:

- structured **logging** with log levels and daily rotation
- full **English codebase and documentation (JavaDoc)**
- **time tracking** in this README (see [Work Log](#work-log))
- extensive **backend testing** (REST, service, persistence)
- **input validation** with dedicated validators
- clean **coding conventions** and best practices
- a **sensible Git workflow** with meaningful commit messages
- **CI integration** (pipeline must be green)
- modern **frontend usability** and UX feedback
- REST-conformant API design (status codes, methods, URIs)
- **RDBMS-based persistence** using H2 with demo data for testing

---

## 🛠 Tech Stack

**Backend**

- Java **OpenJDK 25**
- Spring Boot **3.5**
- H2 **2.4.x** (relational database)
- JUnit **6.x**, AssertJ (testing)
- Maven **3** (build & dependency management)

**Frontend**

- Angular **20**
- Node.js **22.20.0**
- NPM **10.9.3**
- Angular CLI

**Tooling & Others**

- Git **2.x**
- Continuous Integration (GitLab CI / similar)
- IntelliJ IDEA as recommended IDE

---

## 🧱 Architecture

The system follows a **layered architecture** with clear separation of concerns:

- **Persistence Layer**
  - DAOs / repositories for all entities
  - Encapsulated SQL and database access
- **Service Layer**
  - Business logic
  - Validation of inputs
  - Transaction boundaries
- **REST Layer**
  - Controllers that expose REST endpoints
  - Maps HTTP requests to service methods
  - Uses DTOs instead of exposing entities directly
- **Frontend (Angular)**
  - Components for views (listing, detail, edit, search, tree)
  - Services calling the REST API
  - Routing for navigation between views

Key architectural choices:

- **Interface pattern** for service and DAO abstractions
- **Dependency Injection** for all cross-layer dependencies
- **DTOs vs Entities**
  - Entities represent database-persisted domain objects
  - DTOs are used for:
    - search criteria
    - creation/update payloads
    - family tree structures
- **No direct coupling**:
  - REST layer never accesses persistence directly
  - All data flows through the service layer

---

## ▶ Running the Application

### Prerequisites

- Java OpenJDK **25**
- Maven **3.x**
- Node.js **22.20.0**
- NPM **10.9.3**

---

### Backend (Spring Boot)

From the `backend` directory:

```bash
# Compile
mvn compile

# Run tests
mvn test

# Run backend (development)
mvn spring-boot:run

# Or build JAR
mvn clean package

# Run with demo data (datagen profile)
java -Dspring.profiles.active=datagen -jar target/<your-artifact-id>.jar
