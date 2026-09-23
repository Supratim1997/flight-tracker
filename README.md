# ✈️ FlightTracker: Real-Time Flight Price Monitoring System

**FlightTracker** is a beginner-friendly, full-stack flight monitoring system designed to run on any computer using **XAMPP** and **Python**.

It monitors flight prices across an **11-day window** ($\pm 5$ days around your target travel date) for your specified routes. When prices drop below your budget threshold, it immediately alerts you via **Email** and **Telegram**.

---

## 💡 How It Works (In Plain English)

- **The Dashboard (Web App)**: Runs locally in your web browser (via XAMPP Apache). Allows you to set up flight routes, set budget targets, view price trend graphs, filter live flight deals, and access direct booking links.
- **The Database (MySQL)**: Stores your tracking profiles, historical prices, alert logs, and source link access history.
- **The Scraper Engine (Python)**: Queries live flight data streams, updates prices in the database, and sends email/telegram alerts when prices drop below your budget threshold.

---

## 📋 Prerequisites (Tools You Need)

Before starting, make sure you have these two free tools installed on your computer:

1. **XAMPP** (Provides local Apache Web Server & MySQL Database):
   - Download & Install from: [apachefriends.org](https://www.apachefriends.org/)
2. **Python 3.8 or higher**:
   - Download & Install from: [python.org](https://www.python.org/)
   - ⚠️ **IMPORTANT during installation**: Check the box that says **"Add Python to PATH"**.

---

## 🚀 Step-by-Step Setup Guide (For Beginners)

### Step 1: Copy Project Files to XAMPP Web Directory
Place this `flight-tracker` project folder inside your XAMPP `htdocs` directory:
- **Windows default path**: `C:\xampp\htdocs\flight-tracker`
- **Mac default path**: `/Applications/XAMPP/htdocs/flight-tracker`

---

### Step 2: Start Apache and MySQL in XAMPP
1. Open the **XAMPP Control Panel**.
2. Click **Start** next to **Apache**.
3. Click **Start** next to **MySQL**.
*(Both module indicators should turn green).*

---

### Step 3: Set Up the Database
1. Open your web browser and go to: `http://localhost/phpmyadmin`
2. Click on **Import** in the top menu.
3. Click **Choose File** and select `database.sql` located inside the `flight-tracker` project folder.
4. Scroll to the bottom and click **Import** (or **Go**).
*(This automatically creates the `flight_tracker_db` database and all required tables: `search_configs`, `price_history`, `price_access_logs`, and `alert_logs`).*

---

### Step 4: Set Up the Python Scraper Engine
1. Open your terminal or command prompt (Command Prompt / PowerShell on Windows, Terminal on Mac).
2. Navigate to the `scraper` folder inside the project directory:
   ```bash
   cd C:\xampp\htdocs\flight-tracker\scraper
   ```
3. (Optional but recommended) Create and activate a Python virtual environment:
   ```bash
   # Create virtual environment:
   python -m venv venv

   # Activate on Windows:
   .\venv\Scripts\activate

   # Activate on Mac/Linux:
   source venv/bin/activate
   ```
4. Install required Python packages:
   ```bash
   pip install -r requirements.txt
   ```

---

### Step 5: Configure Email & Alert Settings
1. Open your browser to: `http://localhost/flight-tracker/`
2. Click the **Settings ⚙️** button in the top header.
3. Under the **📧 SMTP Email** tab:
   - Click a provider preset button (**Gmail** or **Outlook**).
   - Enter your **Sender Email** and **App Password**. *(For Gmail, generate a 16-character App Password at [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)).*
   - Enter your **Alert Recipient Email**.
   - Click **Test SMTP Connection** to verify your email setup.
   - Click **Save Settings**.

---

## 🔍 How to Test the "Run Manual Check" Button

1. Open `http://localhost/flight-tracker/` in your web browser.
2. In the left panel (**Tracking Configuration**), fill in a flight route:
   - **Departure**: `DEL` (New Delhi)
   - **Arrival**: `BOM` (Mumbai)
   - **Preferred Date**: Select a date (e.g., 2-3 weeks in the future)
   - **Budget Threshold**: `6000`
   - **Flight Type**: `All Flights (Direct & Layovers)`
   - Click **Save Profile**.
3. Now, click the **Run Manual Check** button in the top-right header:
   - The button text will change to **"Running Check..."**.
   - The Python background worker will run in the background, querying live stream data for your target route across an 11-day window ($\pm 5$ days).
   - Once scraping finishes, the button reverts back to **"Run Manual Check"**.
   - The **Cost Trend Analysis Chart** and **Flight Details Table** will automatically refresh to display the latest prices.

---

## ✅ How to Validate if the Application Is Working Properly

Follow this checklist to confirm everything is running 100% correctly:

### Validation Checklist:
- [ ] **1. Active Profiles List**: Your saved flight profile (e.g. `DEL → BOM`) appears in the left panel under **Active Profiles** with a green **ACTIVE** status dot.
- [ ] **2. Cost Trend Analysis Chart**: The interactive line graph displays price data points across the 11-day window, with a dashed red line marking your budget threshold.
- [ ] **3. Real Flight Numbers & Schedules**: The **Flight Details Table** shows real airline names (IndiGo, Air India, Akasa Air, SpiceJet), exact flight numbers (e.g., `6E-6921`, `AI-2951`, `QP-1563`), accurate departure/arrival times, and color-coded stops badges (*Direct* vs *1 Stop*).
- [ ] **4. Book Source Deal Link (Exact Price Match)**: Click the **"Book Source Deal (₹Price)"** button for any flight row. It opens the exact live source search page in a new browser tab where the exact price displayed on the dashboard is shown.
- [ ] **5. Other OTAs Menu**: Hover over the **"Others"** dropdown button next to any flight row to access direct search links for MakeMyTrip (`MMT ↗`) and EaseMyTrip (`EMT ↗`).
- [ ] **6. Price Access & Audit Logs**: Click the **"Access Logs"** button in the top header. A pop-up audit log table will appear showing:
  - Timestamp of scrape execution.
  - Operating Flight ID.
  - Airline, Route, and Target Date.
  - Exact Price Received.
  - Clickable **"Open Access Link ↗"** hyperlink to open the exact URL accessed during price discovery.
- [ ] **7. Email Notifications**: If the scraped price is at or below your set budget threshold, check your recipient email inbox for an automated alert containing full flight details and booking link.

---

## 🛠️ Troubleshooting & Frequently Asked Questions

### 1. "XAMPP Apache or MySQL won't start!"
- **Cause**: Port 80 (Apache) or Port 3306 (MySQL) might be in use by another application (like Skype or IIS).
- **Fix**: In XAMPP Control Panel, click **Config -> Service & Port Settings** and change Apache port to `8080`. You can then access the app at `http://localhost:8080/flight-tracker/`.

### 2. "Run Manual Check button shows an error or doesn't update data."
- **Cause**: Python is missing required packages or MySQL password credentials differ.
- **Fix**: Open command prompt, navigate to `C:\xampp\htdocs\flight-tracker\scraper`, and run `python tracker.py` manually to view detailed log messages.

### 3. "The price on MakeMyTrip (MMT) differs slightly from the dashboard."
- **Explanation**: The dashboard price is extracted directly from the live flight stream (accessible via **"Book Source Deal"**). Third-party OTAs (like MakeMyTrip or EaseMyTrip) may display minor price variances ($\pm$ ₹50-500) due to dynamic convenience fees, seat inventory changes, or payment gateway taxes added during checkout.

---

## ⚠️ Scope & System Limitations

1. **Domestic Flight Focus**: Autocomplete airport listings and baselines are optimized for major Indian commercial domestic airports.
2. **Local MySQL Service Dependency**: MySQL service must be running for the Python scraper worker and PHP web application to log prices and alert history.
3. **Single Recipient Email per Profile**: Email alert notifications are dispatched to the single designated recipient email configured in **Settings**.

---

## 🏗️ Architecture & Technology Stack

- **Web Frontend**: HTML5, Vanilla CSS / Tailwind CSS, Chart.js
- **Backend API**: PHP 8.x
- **Database**: MySQL (`search_configs`, `price_history`, `price_access_logs`, `alert_logs`)
- **Scraper / Alert Engine**: Python 3 (`requests`, `mysql-connector-python`, `smtplib`)
