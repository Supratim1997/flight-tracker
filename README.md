# ✈️ FlightTracker: Multi-Platform Flight Price Monitoring System

**FlightTracker** is a modern, full-stack flight monitoring system built with **Spring Boot 3.2 (Java 21)**. It tracks flight prices in real-time across an **11-day window** ($\pm 5$ days around your target travel date) for major Indian domestic routes across **12 major flight booking platforms & official airline websites**.

When flight prices drop below your set budget threshold, the system immediately alerts you via **Email (SMTP)** and/or **Telegram Bot** notifications according to each tracking profile's preferences.

---

## 🏗️ Architecture & Technology Stack

The application is 100% self-contained in a single **Java 21 / Spring Boot** project:

- **Web Frontend**: HTML5, Vanilla CSS / Tailwind CSS, Chart.js *(100% Mobile & Desktop Compatible)*
- **Backend Framework**: Spring Boot 3.2 (Java 21)
- **Multi-Provider Scraper Engine**: `CompletableFuture` parallel worker pool querying 12 flight providers concurrently:
  - **OTAs & Aggregators**: Google Flights, MakeMyTrip, EaseMyTrip, Ixigo, Goibibo, Cleartrip, Yatra, Paytm Travel
  - **Official Airlines Direct**: IndiGo Direct, Air India Direct, Akasa Air Direct, SpiceJet Direct, Air India Express Direct
- **Database ORM**: Spring Data JPA / Hibernate (MySQL 8.x)
- **Constants Architecture**: Centralized `AppConstants` registry in `com.flighttracker.util`
- **Security & Cryptography**: AES-256 (ECB/PKCS5Padding) for per-profile password & token encryption
- **Background Scheduler**: Spring `@Scheduled` worker (runs background scrapes automatically every 12 hours)
- **Alert Engine**: Dynamic per-profile `JavaMailSenderImpl` (Email) & Telegram Bot API (`https://api.telegram.org/bot<TOKEN>/sendMessage`)

---

## ✨ Key Features

- **🌐 Multi-Platform Flight Discovery**: Aggregates live flight prices from 12 major platforms including Google Flights, MakeMyTrip, EaseMyTrip, Ixigo, Goibibo, Cleartrip, Yatra, Paytm Travel, and official carrier websites (IndiGo, Air India, Akasa Air, SpiceJet).
- **⚡ Parallel Async Execution Engine**: Uses a multi-threaded `CompletableFuture` worker pool so all 12 flight providers are queried simultaneously in parallel within a ~3-second timeout window.
- **✏️ Customer Profile Management & Edit**: Easily add, edit, or delete customer tracking profiles (`SearchConfig`). Includes dedicated **Edit Profile** buttons on sidebar cards and the main dashboard header panel.
- **📱 Mobile & Desktop Browser Compatible**: Responsive touch targets, adaptive layout grids, mobile-friendly modals & filter toolbars.
- **🔔 Multi-Channel Per-Profile Alerts**: Choose notification method per profile:
  - `NONE`: Dashboard tracking only
  - `SMTP`: Email notifications
  - `TELEGRAM`: Instant Telegram Bot messages
  - `BOTH`: Dual Email + Telegram alerts
- **🔒 AES-256 Credential Encryption**: All per-profile SMTP passwords and Telegram Bot tokens are stored securely in MySQL with AES-256 encryption.
- **⚡ Interactive Connection Testing**: Test SMTP email delivery and Telegram Bot API connectivity on demand directly from the UI.
- **📊 Interactive Cost Trend Analysis**: Line charts displaying price movements across an 11-day window with budget threshold indicator lines.
- **🔎 Flight Details & Filters**: Sort and filter by price, departure time, airline, direct vs layover flights, and under-budget deals.
- **🔍 Price Audit & Access Logs**: Pop-up audit log table tracking exact URLs accessed during price discovery, timestamp, operating flight ID, provider source name, and exact price matches.

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
4. Execute SQL to ensure `source_name` column is present:
   ```sql
   USE flight_tracker_db;
   ALTER TABLE price_history ADD COLUMN IF NOT EXISTS source_name VARCHAR(100) DEFAULT 'Google Flights';
   ```

---

### Step 2: Configure Database Credentials
Open `src/main/resources/application.properties` and verify your MySQL credentials and server port:

```properties
# Server Port
server.port=8090

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/flight_tracker_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=
```

---

### Step 3: Run the Application

#### Option A: Using Maven Command Line
Open your terminal in the root project directory (`d:\Source\Flight Scanner`) and run:

```bash
mvn spring-boot:run
```

#### Option B: Building a Standalone Executable JAR
1. Compile the JAR file:
   ```bash
   mvn clean package -DskipTests
   ```
2. Run the executable JAR on any computer with Java 21:
   ```bash
   java -jar target/flight-tracker-1.0.0.jar
   ```

---

### Step 4: Access the Dashboard
Open your web browser (desktop or mobile phone) and navigate to:
👉 **[http://localhost:8090/](http://localhost:8090/)**

---

## ✅ System Validation Checklist

- [x] **Multi-Platform Scraper**: Searches Google Flights, EaseMyTrip, MakeMyTrip, Ixigo, Goibibo, Cleartrip, Yatra, Paytm Travel, and official carrier websites.
- [x] **Parallel Async Engine**: CompletableFuture worker pool queries 12 providers concurrently within a ~3s window.
- [x] **Customer Profile Editing**: Click ✏️ **Edit Profile** on any sidebar card or dashboard header to update customer preferences and alert credentials.
- [x] **Central AppConstants**: Standardized constant management in `com.flighttracker.util.AppConstants`.
- [x] **AES-256 Encryption**: Encrypts and decrypts credentials without storing plaintext passwords in Git or database logs.
- [x] **Cost Trend Analysis Chart**: Interactive line graph displays price points with budget threshold dashed red line.
- [x] **Book Source Deal & Direct Links**: Direct booking links to Google Flights, MakeMyTrip, EaseMyTrip, Ixigo, Goibibo, Cleartrip, Yatra, Paytm, and official airlines.
- [x] **Mobile Responsive**: Dashboard automatically resizes and formats seamlessly on mobile screens and tablets.

