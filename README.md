# ✈️ FlightTracker: Real-Time Flight Price Monitoring System

**FlightTracker** is a modern, full-stack flight monitoring system built with **Spring Boot 3.2 (Java 21)**. It tracks flight prices in real-time across an **11-day window** ($\pm 5$ days around your target travel date) for major Indian domestic routes.

When prices drop below your set budget threshold, the system immediately alerts you via **Email** notifications.

---

## 🏗️ Architecture & Technology Stack

The application is 100% self-contained in a single **Java 21 / Spring Boot** project:

- **Web Frontend**: HTML5, Vanilla CSS / Tailwind CSS, Chart.js *(100% Mobile Browser Compatible)*
- **Backend Framework**: Spring Boot 3.2 (Java 21)
- **Database ORM**: Spring Data JPA / Hibernate (MySQL 8.x)
- **Scraper Engine**: Native Java 21 `HttpClient` & Jackson JSON stream parser
- **Background Scheduler**: Spring `@Scheduled` worker (runs background scrapes every 12 hours automatically)
- **Alert Engine**: Spring Mail (`JavaMailSender`)

---

## ✨ Key Features

- **📱 Mobile Browser Compatible**: Responsive touch targets, iOS Safari auto-zoom prevention, adaptive layout grids, mobile-friendly modals & filter toolbars.
- **🚀 Self-Contained Spring Boot Engine**: Direct Java HTTP stream scraping, embedded Tomcat web server, and automated background price checking—no external scripts required.
- **📊 Interactive Cost Trend Analysis**: Line charts displaying price movements across an 11-day window with budget threshold indicator lines.
- **🔎 Flight Details & Filters**: Sort and filter by price, departure time, airline, direct vs layover flights, and under-budget deals.
- **🔍 Price Audit & Access Logs**: Pop-up audit log table tracking exact URLs accessed during price discovery, timestamp, operating flight ID, and exact price matches.
- **📧 Automated Email Notifications**: Instant alerts delivered when prices fall below target threshold.
- **🔒 Continuous Background Execution**: Background scheduled tasks continue running even when screen is locked (`Win + L`).

---

## 🚀 Step-by-Step Guide: How to Run the Code

### Prerequisites (Tools You Need)
1. **Java JDK 21** or higher:
   - Download & Install from [adoptium.net](https://adoptium.net/) or [oracle.com/java](https://www.oracle.com/java/).
2. **Maven 3.8+** (or use your IDE's built-in Maven tool).
3. **MySQL Server** (running locally on port `3306` via XAMPP or standalone MySQL).

---

### Step 1: Set Up MySQL Database
1. Make sure your MySQL service is running on `localhost:3306`.
2. Open your MySQL client (Command Line, MySQL Workbench, or phpMyAdmin at `http://localhost/phpmyadmin`).
3. Execute SQL to create the database:
   ```sql
   CREATE DATABASE IF NOT EXISTS flight_tracker_db;
   ```

---

### Step 2: Configure Database Credentials
Open `src/main/resources/application.properties` and verify your MySQL credentials:

```properties
# Server Port
server.port=8080

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/flight_tracker_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

*(Note: `spring.jpa.hibernate.ddl-auto` is set to `none` by default to prevent DDL foreign key conflicts with pre-existing table schemas).*

---

### Step 3: Run the Application

#### Option A: Using Maven Command Line
Open your terminal in the root project directory (`d:\Source\Flight Scanner`) and run:

```bash
mvn spring-boot:run
```

#### Option B: Building a Standalone Executable JAR (Production Deployment)
1. Compile the JAR file:
   ```bash
   mvn clean package -DskipTests
   ```
2. Run the executable JAR on any computer with Java 21:
   ```bash
   java -jar target/flight-tracker-1.0.0.jar
   ```

#### Option C: Using IDE (VS Code / IntelliJ IDEA / Eclipse)
1. Open the project root folder in your IDE.
2. Locate `src/main/java/com/flighttracker/FlightTrackerApplication.java`.
3. Right-click and choose **Run / Debug**.

---

### Step 4: Access the Dashboard
Open your web browser (desktop or mobile phone) and navigate to:
👉 **[http://localhost:8080/](http://localhost:8080/)**

---

## 🔍 How to Test the "Run Manual Check" Button

1. Open `http://localhost:8080/` in your browser.
2. Under **Tracking Configuration** (left panel), enter a flight route:
   - **Departure**: `DEL` (New Delhi)
   - **Arrival**: `BOM` (Mumbai)
   - **Preferred Date**: Select a future date (e.g. 2-3 weeks ahead)
   - **Budget Threshold**: `6000`
   - **Flight Type**: `All Flights`
   - Click **Save Profile**.
3. Click **Run Manual Check** in the header.
   - Button changes to **"Running Check..."**.
   - Background Java scraper queries live stream data across the 11-day window ($\pm 5$ days).
   - Once completed, the **Cost Trend Analysis Chart** and **Flight Details Table** automatically refresh with live data.

---

## ✅ System Validation Checklist

- [x] **Active Profiles List**: Saved route appears in the left sidebar with green **ACTIVE** status indicator.
- [x] **Cost Trend Analysis Chart**: Interactive line graph displays price points with budget threshold dashed red line.
- [x] **Real Flight Numbers & Schedules**: Flight details table shows real airlines (IndiGo, Air India, Akasa Air, SpiceJet), flight numbers (e.g. `6E-6921`), and exact times.
- [x] **Book Source Deal**: Click **"Book Deal (₹Price)"** to open the live flight booking page.
- [x] **Access & Audit Logs**: Click **"Logs"** in header to inspect exact URLs accessed during price discovery.
- [x] **Mobile Responsive**: Dashboard automatically resizes and formats seamlessly on mobile screens and tablets.

---

## 🛠️ Frequently Asked Questions (FAQ)

### 1. Will the background scheduler run when my screen is locked?
**Yes.** Locking your screen (`Win + L`) locks the UI but keeps background processes and JVM timer threads running. Make sure Windows power options are set so the PC does not go to **Sleep**.

### 2. "Hibernate CommandAcceptanceException / Foreign Key error"
If Hibernate reports a DDL foreign key constraint error on startup, ensure `spring.jpa.hibernate.ddl-auto=none` is set in `application.properties`.

### 3. Port 8080 is already in use
Change `server.port=8080` in `src/main/resources/application.properties` to another port like `server.port=8085`.
