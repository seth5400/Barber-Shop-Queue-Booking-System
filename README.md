# Barber-Shop-Queue-Booking-System

A web-based system for managing barber shop appointments and queues efficiently. Built with **Spring Boot** for backend and **Thymeleaf** templates for frontend.

## About This Project
This project is a collaborative work between me and my friend. It was developed as part of our shared learning experience to create a functional Barber Shop Queue Booking System.

## Features
- User registration and login system
- Appointment booking with barber selection
- Admin dashboard for managing services and users
- Barber dashboard for viewing schedules and appointments
- Reporting and analytics for admin
- Responsive and user-friendly design

## Installation

### Prerequisites
- Java 11 or higher
- Maven
- MySQL (or another compatible database)

### Steps
1. Clone the repository:
   ```bash
   git clone <repository_url>

2. Navigate to the project directory
   ```bash
   cd Project_HairK_version4

3. Set up the database
   - Configure your database details in src/main/resources/application.properties
   - Run the necessary SQL migrations if provided.

4. Build and run the project:
   ```bash
   mvn spring-boot:run

5. Access the application
   - Open your browser and navigate to http://localhost:8080
  
## Project Structure
- **Controllers**: Handle HTTP requests and route them to services.
- **Services**: Business logic and data manipulation.
- **Models**: Represent database entities.
- **Repositories**: Interface for data access.
- **Resources**: Static files and templates for frontend.
