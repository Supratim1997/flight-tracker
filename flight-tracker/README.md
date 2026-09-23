# FlightTracker

A premium, full-stack flight monitoring system designed to run locally on XAMPP with a background Python worker.

## Prerequisites
- XAMPP (Apache & MySQL)
- Python 3.8+

## Setup Instructions

### 1. Database Setup
1. Open XAMPP Control Panel and start **Apache** and **MySQL**.
2. Open phpMyAdmin (usually `http://localhost/phpmyadmin`).
3. Import the `database.sql` file located in the root of this project. This will create the `flight_tracker_db` and all required tables.

### 2. PHP Web Dashboard
1. Ensure this folder (`flight-tracker`) is placed inside your XAMPP `htdocs` directory (e.g., `C:\xampp\htdocs\flight-tracker`).
2. Verify `config.php` has the correct database credentials (defaults to `root` with no password).
3. Access the dashboard in your browser: `http://localhost/flight-tracker/`

### 3. Python Background Scraper
The Python script is responsible for querying flight prices across an 11-day window around your target dates and sending alerts.

1. Navigate to the scraper directory:
   ```bash
   cd scraper
   ```
2. Create and activate a virtual environment (optional but recommended):
   ```bash
   python -m venv venv
   # Windows:
   venv\Scripts\activate
   # Mac/Linux:
   source venv/bin/activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
### 4. Alerting Configuration (SMTP & Telegram)

You can configure alert notifications either directly through the **Web Dashboard UI** or by manually editing the `scraper/.env` file.

---

#### Option A: Via Web Dashboard UI (Recommended)

1. Open your browser to `http://localhost/flight-tracker/`.
2. Click the **Settings ⚙️** button in the top header.
3. **SMTP Email Setup**:
   - Select the **📧 SMTP Email** tab.
   - Click the preset button (**Gmail** or **Outlook**) or enter your custom SMTP host/port.
   - Enter your **SMTP Username** and **16-Character App Password**.
   - Enter your **Alert Recipient Email**.
   - Click **Test SMTP Connection** to send an instant test email and confirm setup.
   - Click **Save Settings**.
4. **Telegram Setup**:
   - Select the **📱 Telegram Alerts** tab.
   - Enter your `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID`.
   - Click **Save Settings**.

---

#### Option B: Manual `.env` File Setup

Copy `scraper/.env.example` to `scraper/.env` and populate your credentials:

```ini
# Alerting Config (SMTP)
SMTP_SERVER=smtp.gmail.com
SMTP_PORT=587
SMTP_USER=your_email@gmail.com
SMTP_PASS=your_16_character_app_password
ALERT_RECIPIENT=recipient_email@example.com

# Alerting Config (Telegram)
TELEGRAM_BOT_TOKEN=your_telegram_bot_token
TELEGRAM_CHAT_ID=your_telegram_chat_id
```

##### 📧 Step-by-Step Gmail SMTP Guide:
1. Go to your **[Google Account Security Page](https://myaccount.google.com/security)** and ensure **2-Step Verification** is turned **ON**.
2. Visit **[Google App Passwords](https://myaccount.google.com/apppasswords)**.
3. Generate a new App Password named `FlightTracker`.
4. Copy the **16-character password** (remove any spaces) and paste it into `SMTP_PASS`.

##### 📱 Step-by-Step Telegram Bot Setup Guide:
1. Open Telegram and search for **`@BotFather`**.
2. Send `/newbot` and follow the prompts to create your bot. Copy the HTTP API token provided (`TELEGRAM_BOT_TOKEN`).
3. Search for **`@userinfobot`** in Telegram and send `/start` to retrieve your numeric `chat_id` (`TELEGRAM_CHAT_ID`).
4. (Optional) Send a message to your newly created bot to start the chat session.

---

### 5. Automated Scheduling (Windows Task Scheduler)
To run the scraper automatically in the background every 12 hours:
1. Open **Task Scheduler**.
2. Click **Create Task**.
3. Name it `FlightTracker Scraper`.
4. Under **Triggers**, add a new trigger to run Daily, and check "Repeat task every: 12 hours".
5. Under **Actions**, set:
   - **Program/script**: Path to your python executable (e.g., `C:\path\to\flight-tracker\scraper\venv\Scripts\python.exe`)
   - **Add arguments**: `tracker.py`
   - **Start in**: `C:\path\to\flight-tracker\scraper\`

You can also trigger a manual check anytime directly from the Web Dashboard using the **Run Manual Check** button.

## Architecture & Tech Stack
- **Frontend**: HTML5, Tailwind CSS (CDN), Chart.js
- **Backend API**: PHP 8.x
- **Database**: MySQL
- **Scraper / Alert Engine**: Python 3 (requests, mysql-connector, smtplib)

