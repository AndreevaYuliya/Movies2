# 📚 Movie Catalog Service

### Spring Boot • PostgreSQL • Liquibase • REST API • Many-to-One • File Upload • Reports

A Spring Boot REST API service built for managing **Movies** (Entity 1)
and **Directors** (Entity 2).\
The system demonstrates a **many-to-one relationship**, supports JSON
import, pagination with filtering,\
CSV/Excel report generation, and uses Liquibase for schema management.

------------------------------------------------------------------------

# 🧩 Domain Model

## 🎬 Entity 1: Movie

  Field        Type      Required   Description
  ------------ --------- ---------- ----------------------------------
  id           Long      yes        Primary key
  title        String    yes        Movie title
  year         Integer   yes        Release year
  genres       String    no         Optional, comma-separated values
  directorId   Long      yes        Foreign key to Director

## 🎭 Entity 2: Director

  Field   Type     Required       Description
  ------- -------- -------------- ---------------
  id      Long     yes            Primary key
  name    String   yes + unique   Director name

------------------------------------------------------------------------

# 🔗 Many-to-One Relationship Explained

A director may have **many movies**, but each movie references exactly
**one director**.

    Director 1 ─┬── Movie A
                ├── Movie B
                └── Movie C

JPA mapping:

``` java
@ManyToOne
@JoinColumn(name = "director_id", nullable = false)
private Director director;
```

------------------------------------------------------------------------

# 🚀 Features

✔ CRUD endpoints for Movies\
✔ CRUD endpoints for Directors\
✔ Many-to-one relationship with FK\
✔ JSON file upload → validate → bulk insert\
✔ Filtering & pagination at DB level\
✔ CSV / Excel report generation\
✔ Liquibase-based schema creation + initial data\
✔ Integration tests for all endpoints\
✔ PostgreSQL (no Docker required)

------------------------------------------------------------------------

# 🛢 PostgreSQL Setup (No Docker)

### 1. Install PostgreSQL

Download:\
https://www.postgresql.org/download/

During setup configure:

-   **User:** postgres\
-   **Password:** postgres\
-   **Port:** 5432

### 2. Create database

In pgAdmin or psql:

``` sql
CREATE DATABASE movies;
```

### 3. Application configuration (`application.yml`)

``` yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/movies
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: none

  liquibase:
    enabled: true
```

------------------------------------------------------------------------

# 📦 Liquibase Migration

Liquibase automatically:

-   Creates tables for Movie & Director\
-   Adds FK constraints\
-   Adds unique constraints (director name)\
-   Creates indexes for pagination filters\
-   Inserts initial Director values

Example structure:

    src/main/resources/db/changelog/
     ├── master.xml
     ├── changes/
     │   ├── 001-create-tables.xml
     │   ├── 002-insert-directors.xml

------------------------------------------------------------------------

# 📡 REST API Endpoints

## 🎬 Movies (Entity 1)

### ➕ Create Movie

`POST /api/movies`

### 🔍 Get Movie by ID

`GET /api/movies/{id}`

Returns director object inside movie.

### ✏ Update Movie

`PUT /api/movies/{id}`

### 🗑 Delete Movie

`DELETE /api/movies/{id}`

### 📃 List Movies (Filtering + Pagination)

`POST /api/movies/_list`

### 📊 Export Report

`POST /api/movies/_report`

### 📁 JSON Upload

`POST /api/movies/upload`

------------------------------------------------------------------------

## 🎭 Directors (Entity 2)

### 📃 List All

`GET /api/directors`

### ➕ Create Director

`POST /api/directors`

### ✏ Update

`PUT /api/directors/{id}`

### 🗑 Delete

`DELETE /api/directors/{id}`

------------------------------------------------------------------------

# 🧪 Integration Tests

✔ Spring Boot Test\
✔ MockMvc\
✔ Liquibase test schema\
✔ Validation tests\
✔ Upload tests\
✔ Report export tests

Run tests:

``` sh
mvn test
```

------------------------------------------------------------------------

# ▶ Run the Application

``` sh
mvn spring-boot:run
```

App starts with:

-   PostgreSQL connection\
-   Liquibase migrations\
-   REST API available at:\
    `http://localhost:8080/api/...`

------------------------------------------------------------------------

# 📥 Sample JSON for Upload

Place in:

    src/main/resources/sample/movies.json

------------------------------------------------------------------------

# 🎯 Summary

This project demonstrates:

-   Clean REST API architecture\
-   Many-to-one relationship modeling\
-   PostgreSQL schema versioning\
-   Efficient filtering & pagination\
-   Report generation\
-   JSON import workflow\
-   Full integration test coverage
