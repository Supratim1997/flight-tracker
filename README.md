# ✈️ FlightTracker: Real-Time Flight Price Monitoring System

**FlightTracker** is a modern, full-stack flight monitoring system built with **Spring Boot 3.2 (Java 21)**. It tracks flight prices in real-time across an **11-day window** ($\pm 5$ days around your target travel date) for major Indian domestic routes.

When prices drop below your set budget threshold, the system immediately alerts you via **Email (SMTP)** and/or **Telegram Bot** notifications according to each tracking profile's preferences.

---

## 🏗️ Architecture & Technology Stack

The application is 100% self-contained in a single **Java 21 / Spring Boot** project:

- **Web Frontend**: HTML5, Vanilla CSS / Tailwind CSS, Chart.js *(100% Mobile Browser Compatible)*
- **Backend Framework**: Spring Boot 3.2 (Java 21)
- **Database ORM**: Spring Data JPA / Hibernate (MySQL 8.x)
- **Security & Cryptography**: AES-256 (ECB/PKCS5Padding) for per-profile password & token encryption
- **Scraper Engine**: Native Java 21 `HttpClient` & Jackson JSON stream parser
- **Background Scheduler**: Spring `@Scheduled` worker (runs background scrapes every 12 hours automatically)
- **Alert Engine**: Dynamic per-profile `JavaMailSenderImpl` (Email) & Telegram Bot API (`https://api.telegram.org/bot<TOKEN>/sendMessage`)

---

## ✨ Key Features

- **📱 Mobile Browser Compatible**: Responsive touch targets, iOS Safari auto-zoom prevention, adaptive layout grids, mobile-friendly modals & filter toolbars.
- **🚀 Self-Contained Spring Boot Engine**: Direct Java HTTP stream scraping, embedded Tomcat web server, and automated background price checking—no external scripts required.
- **🔔 Multi-Channel Per-Profile Alerts**: Choose notification method per profile:
  - `NONE`: Dashboard tracking only
  - `SMTP`: Email notifications
  - `TELEGRAM`: Instant Telegram Bot messages
  - `BOTH`: Dual Email + Telegram alerts
- **🔒 AES-256 Credential Encryption**: All per-profile SMTP passwords and Telegram Bot tokens are stored securely in MySQL with AES-256 encryption.
- **⚡ Interactive Connection Testing**: Test SMTP email delivery and Telegram Bot API connectivity on demand directly from the UI.
- **⚙️ Active Profile Settings Modal**: Click "Settings" in the header to view, test, and edit notification credentials for the currently selected active profile.
- **📊 Interactive Cost Trend Analysis**: Line charts displaying price movements across an 11-day window with budget threshold indicator lines.
- **🔎 Flight Details & Filters**: Sort and filter by price, departure time, airline, direct vs layover flights, and under-budget deals.
- **🔍 Price Audit & Access Logs**: Pop-up audit log table tracking exact URLs accessed during price discovery, timestamp, operating flight ID, and exact price matches.
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
4. Execute the provided `schema.sql` script (located in the project root) to create/update tables with per-profile alert notification columns:
   ```sql
   USE flight_tracker_db;

   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS alert_method VARCHAR(20) DEFAULT 'NONE';
   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_server VARCHAR(100) NULL;
   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_port INT DEFAULT 587;
   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_user VARCHAR(100) NULL;
   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS smtp_pass_encrypted VARCHAR(255) NULL;
   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS alert_recipient VARCHAR(100) NULL;
   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS telegram_bot_token_encrypted VARCHAR(255) NULL;
   ALTER TABLE search_configs ADD COLUMN IF NOT EXISTS telegram_chat_id VARCHAR(50) NULL;
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
👉 **[http://localhost:8090/](http://localhost:8090/)**

---

## 🔍 How to Test Profile Alerts & Connections

1. Open `http://localhost:8090/` in your browser.
2. Click **Settings** in the header or edit a route profile.
3. Select an **Alert Notification** channel (`Email Alert (SMTP)` or `Telegram Alert`).
4. Enter credentials:
   - For **SMTP**: Enter Host (e.g. `smtp.gmail.com`), Port (`587`), Sender Email, App Password, and Recipient Email.
   - For **Telegram**: Enter Bot Token (`123456789:ABC...`) and Chat ID (`-100123...`).
5. Click **⚡ Test SMTP Connection** or **⚡ Test Telegram Connection** to verify live test delivery.
6. Click **Save Profile Credentials**.

---

## ✅ System Validation Checklist

- [x] **Active Profiles List**: Saved route appears in the left sidebar with active status indicator and alert badge (`📧 Email`, `📱 Telegram`, `📧+📱 Both`).
- [x] **Active Profile Settings Modal**: Header **Settings** button dynamically loads and pre-fills credentials of the selected active profile.
- [x] **AES-256 Encryption**: Encrypts and decrypts credentials without storing plaintext passwords in Git or database logs.
- [x] **Cost Trend Analysis Chart**: Interactive line graph displays price points with budget threshold dashed red line.
- [x] **Real Flight Numbers & Schedules**: Flight details table shows real airlines (IndiGo, Air India, Akasa Air, SpiceJet), flight numbers (e.g. `6E-6921`), and exact times.
- [x] **Book Source Deal**: Click **"Book Deal (₹Price)"** to open the live flight booking page.
- [x] **Access & Audit Logs**: Click **"Logs"** in header to inspect exact URLs accessed during price discovery.
- [x] **Mobile Responsive**: Dashboard automatically resizes and formats seamlessly on mobile screens and tablets.

---

## 🛠️ Frequently Asked Questions (FAQ)

### 1. Will the background scheduler run when my screen is locked?
**Yes.** Locking your screen (`Win + L`) locks the UI but keeps background processes and JVM timer threads running. Make sure Windows power options are set so the PC does not go to **Sleep**.

### 2. How to create a Telegram Bot for alerts?
1. Open Telegram and search for `@BotFather`.
2. Send `/newbot` and follow instructions to get your **Bot Token**.
3. Add the bot to your channel/group or message it directly, then obtain your **Chat ID** (via `@userinfobot`).

### 3. Hibernate CommandAcceptanceException / Foreign Key error
If Hibernate reports a DDL foreign key constraint error on startup, ensure `spring.jpa.hibernate.ddl-auto=none` is set in `application.properties` and apply `schema.sql`.
