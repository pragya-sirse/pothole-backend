# Smart Pothole Detection System

A Spring Boot-based backend platform designed to streamline pothole reporting, complaint tracking, and road maintenance management. The system enables citizens to report potholes, monitor complaint status, and assists authorities in managing and prioritizing road repair activities efficiently.

---

## Overview

Road infrastructure issues such as potholes often go unreported or unresolved due to inefficient complaint management systems. This project provides a centralized platform where users can report potholes, track the progress of their complaints, and receive updates while authorities can manage reports through a structured workflow.

---

## Features

### User Features

- User Registration and Authentication
- Secure Login System
- Report Potholes
- Track Complaint Status
- View Complaint History
- Receive Status Updates and Notifications

### Administrative Features

- Manage Reported Potholes
- Update Complaint Status
- Monitor User Reports
- Prioritize Road Maintenance Requests
- Complaint Resolution Workflow

### System Features

- RESTful API Architecture
- Database Persistence
- Email Notification Support
- Dockerized Deployment
- CI/CD Integration with GitHub Actions

---

## Tech Stack

| Category | Technology |
|-----------|------------|
| Language | Java |
| Framework | Spring Boot |
| Security | Spring Security |
| ORM | Hibernate / JPA |
| Database | MySQL |
| Build Tool | Maven |
| Containerization | Docker |
| CI/CD | GitHub Actions |
| Version Control | Git & GitHub |

---

## System Architecture

```text
User
 │
 ▼
REST API Layer
 │
 ▼
Spring Boot Application
 │
 ├── Authentication Module
 ├── User Management Module
 ├── Pothole Reporting Module
 ├── Complaint Tracking Module
 └── Notification Module
 │
 ▼
Database Layer (MySQL)
```

---

## Core Modules

### Authentication Module

- User Registration
- User Login
- Secure Access Control

### User Management Module

- User Profile Management
- Complaint History Management

### Pothole Reporting Module

- Create New Reports
- Store Pothole Information
- Maintain Report Records

### Complaint Tracking Module

- Track Complaint Progress
- Status Updates
- Resolution Tracking

### Notification Module

- Automated Notifications
- Email Updates
- Complaint Status Alerts

---

## Project Structure

```text
src
 ├── controller
 ├── service
 ├── repository
 ├── entity
 ├── dto
 ├── security
 ├── exception
 └── config
```

---

## Getting Started

### Clone Repository

```bash
git clone https://github.com/pragya-sirse/Smart-Pothole-Detection-System.git
```

### Build Project

```bash
mvn clean install
```

### Run Application

```bash
mvn spring-boot:run
```

---

## Docker Deployment

Build Docker Image

```bash
docker build -t pothole-system .
```

Run Container

```bash
docker run -p 8080:8080 pothole-system
```

---

## API Highlights

### Authentication APIs

- Register User
- Login User

### Complaint APIs

- Create Complaint
- Get Complaint Details
- Update Complaint Status
- View Complaint History

### User APIs

- User Profile Management
- Complaint Tracking

---

## Future Enhancements

- Real-Time Location Tracking
- GIS and Map Integration
- Image-Based Pothole Detection
- AI-Assisted Severity Classification
- Analytics Dashboard
- Mobile Application Integration

---

## Key Highlights

- Designed a scalable backend architecture using Spring Boot.
- Implemented complaint management workflows for road maintenance tracking.
- Integrated database persistence using JPA and MySQL.
- Containerized the application using Docker.
- Configured automated build pipelines using GitHub Actions.
- Developed a maintainable layered architecture following backend engineering best practices.

---

## Author

**Pragya Sirse**

GitHub: https://github.com/pragya-sirse
LinkedIn: https://www.linkedin.com/in/pragya-sirse-938007256/
