import os
import sys
import datetime
import random
import mysql.connector
from dotenv import load_dotenv

import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# Ensure UTF-8 output encoding for Windows stdout compatibility (emojis support)
if hasattr(sys.stdout, 'reconfigure'):
    sys.stdout.reconfigure(encoding='utf-8')

# Load environment variables
load_dotenv()

DB_HOST = os.getenv('DB_HOST', 'localhost')
DB_USER = os.getenv('DB_USER', 'root')
DB_PASS = os.getenv('DB_PASS', '')
DB_NAME = os.getenv('DB_NAME', 'flight_tracker_db')

def get_db_connection():
    try:
        conn = mysql.connector.connect(
            host=DB_HOST,
            user=DB_USER,
            password=DB_PASS,
            database=DB_NAME
        )
        return conn
    except mysql.connector.Error as err:
        print(f"Error connecting to MySQL: {err}")
        sys.exit(1)

def fetch_live_or_mock_flights(departure, arrival, date):
    """
    Fetches real live flight prices if SERPAPI_KEY / FLIGHT_API_KEY is configured in .env,
    otherwise uses route-tailored realistic flight schedules and pricing baselines.
    """
    serpapi_key = os.getenv('SERPAPI_KEY') or os.getenv('FLIGHT_API_KEY')
    date_str = date.strftime("%Y-%m-%d") if isinstance(date, (datetime.date, datetime.datetime)) else str(date)
    
    if serpapi_key and serpapi_key != 'your_amadeus_or_serpapi_key':
        try:
            url = f"https://serpapi.com/search.json?engine=google_flights&departure_id={departure}&arrival_id={arrival}&outbound_date={date_str}&currency=INR&hl=en&api_key={serpapi_key}"
            resp = requests.get(url, timeout=10)
            if resp.status_code == 200:
                data = resp.json()
                results = []
                best_flights = data.get('best_flights', []) + data.get('other_flights', [])
                for f in best_flights[:5]:
                    flight_info = f.get('flights', [{}])[0]
                    airline = flight_info.get('airline', 'IndiGo')
                    flight_num = flight_info.get('flight_number', '6E-101')
                    dep_time = flight_info.get('departure_token', '08:00:00')
                    price = float(f.get('price', 5000))
                    results.append({
                        'airline': airline,
                        'flight_number': flight_num,
                        'departure_time': '08:00:00',
                        'arrival_time': '10:15:00',
                        'price': price,
                        'is_direct': len(f.get('flights', [])) == 1
                    })
                if results:
                    return results
        except Exception as e:
            print(f"⚠️ Live Flight API error: {e}, falling back to realistic mock engine.")

    # Realistic Route Schedule & Pricing Engine
    route_key = f"{departure.upper()}-{arrival.upper()}"
    
    # Real-world baseline pricing tiers for Indian domestic air travel (in INR)
    route_baselines = {
        'DEL-BOM': 5400, 'BOM-DEL': 5400,
        'BOM-CCU': 7200, 'CCU-BOM': 7200,
        'PNQ-CCU': 7600, 'CCU-PNQ': 7600,
        'DEL-BLR': 5800, 'BLR-DEL': 5800,
        'BOM-BLR': 4200, 'BLR-BOM': 4200,
        'DEL-PNQ': 5100, 'PNQ-DEL': 5100,
        'DEL-CCU': 6500, 'CCU-DEL': 6500,
    }
    
    base_price = route_baselines.get(route_key, 6500)
    
    # Real-world flight inventory templates for Indian domestic carriers
    real_schedules = [
        {'airline': 'IndiGo', 'code': '6E-205', 'dep': '06:15:00', 'arr': '08:30:00', 'mult': 0.95},
        {'airline': 'IndiGo', 'code': '6E-531', 'dep': '11:40:00', 'arr': '13:55:00', 'mult': 1.05},
        {'airline': 'Air India', 'code': 'AI-675', 'dep': '08:45:00', 'arr': '11:00:00', 'mult': 1.10},
        {'airline': 'Vistara', 'code': 'UK-995', 'dep': '17:20:00', 'arr': '19:35:00', 'mult': 1.15},
        {'airline': 'Akasa Air', 'code': 'QP-1102', 'dep': '14:10:00', 'arr': '16:25:00', 'mult': 0.92},
        {'airline': 'SpiceJet', 'code': 'SG-8169', 'dep': '20:30:00', 'arr': '22:45:00', 'mult': 0.88},
    ]
    
    # Pick 2-3 realistic flights for the date
    seed_val = int(datetime.datetime.strptime(date_str, "%Y-%m-%d").timestamp()) if isinstance(date_str, str) else 100
    random.seed(seed_val + hash(route_key))
    
    selected_schedules = random.sample(real_schedules, random.randint(2, 3))
    flights = []
    
    for s in selected_schedules:
        price_variation = random.uniform(-300, 400)
        final_price = round(max(2500, base_price * s['mult'] + price_variation), 2)
        
        flights.append({
            'airline': s['airline'],
            'flight_number': s['code'],
            'departure_time': s['dep'],
            'arrival_time': s['arr'],
            'price': final_price,
            'is_direct': True
        })
        
    random.seed()
    return flights


def main():
    conn = get_db_connection()
    cursor = conn.cursor(dictionary=True)
    
    print(f"[{datetime.datetime.now()}] Starting flight data scrape...")
    
    # 1. Get active configs
    cursor.execute("SELECT * FROM search_configs WHERE active = 1")
    configs = cursor.fetchall()
    
    if not configs:
        print("No active tracking configurations found.")
        return
        
    for config in configs:
        print(f"Processing config {config['id']}: {config['departure_city']} to {config['arrival_city']} (Target: {config['preferred_date']})")
        print(f"Budget: {config['budget_threshold']}")
        
        target_date = config['preferred_date']
        # Convert date string or datetime.date to datetime.date object if needed
        if isinstance(target_date, str):
            target_date = datetime.datetime.strptime(target_date, "%Y-%m-%d").date()
            
        budget = float(config['budget_threshold'])
        
        # 11-day window (-5 to +5)
        for i in range(-5, 6):
            current_date = target_date + datetime.timedelta(days=i)
            
            # Fetch live API or realistic route flight data
            flights = fetch_live_or_mock_flights(config['departure_city'], config['arrival_city'], current_date)
            
            for f in flights:
                # 2. Insert into price_history
                insert_sql = """
                    INSERT INTO price_history 
                    (config_id, flight_date, airline, flight_number, departure_time, arrival_time, price_inr, is_direct) 
                    VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                """
                cursor.execute(insert_sql, (
                    config['id'], 
                    current_date, 
                    f['airline'], 
                    f['flight_number'], 
                    f['departure_time'], 
                    f['arrival_time'], 
                    f['price'], 
                    f['is_direct']
                ))
                flight_id = cursor.lastrowid
                
                # 3. Check Budget Threshold
                if f['price'] <= budget:
                    # Check if we already alerted for this config & flight_date in the last 24h
                    check_alert_sql = """
                        SELECT id FROM alert_logs 
                        WHERE config_id = %s 
                        AND triggered_price = %s 
                        AND sent_at > DATE_SUB(NOW(), INTERVAL 24 HOUR)
                    """
                    # For simplicity in mock data, we just check if ANY alert was sent for this config in 24h
                    # Or we check specifically if this exact flight date got alerted in 24h
                    cursor.execute("""
                        SELECT id FROM alert_logs 
                        WHERE config_id = %s 
                        AND sent_at > DATE_SUB(NOW(), INTERVAL 24 HOUR)
                        AND message LIKE %s
                    """, (config['id'], f"%{current_date}%"))
                    
                    recent_alert = cursor.fetchone()
                    
                    if not recent_alert:
                        # Log alert
                        msg = f"Price drop alert! {f['airline']} {f['flight_number']} on {current_date} is now ₹{f['price']}"
                        cursor.execute("""
                            INSERT INTO alert_logs (config_id, flight_id, alert_type, message, triggered_price)
                            VALUES (%s, %s, %s, %s, %s)
                        """, (config['id'], flight_id, 'SYSTEM_MOCK', msg, f['price']))
                        
                        f['flight_date'] = current_date
                        send_alert(config, f)
                    else:
                        print(f"ℹ️ Alert already logged for {current_date} in the last 24h. Skipping email to prevent spam.")

        
        conn.commit()
    
    cursor.close()
    conn.close()
def send_alert(config, flight):
    """Sends email alert via SMTP when a price drop is detected."""
    smtp_server = os.getenv('SMTP_SERVER')
    smtp_port = int(os.getenv('SMTP_PORT', 587))
    smtp_user = os.getenv('SMTP_USER')
    smtp_pass = os.getenv('SMTP_PASS')
    recipient = os.getenv('ALERT_RECIPIENT')
    print(f"🚨 ALERT! Price dropped to ₹{flight['price']} for {config['departure_city']} -> {config['arrival_city']} on {flight['flight_date']}")
    # If SMTP credentials are configured, send an email
    if smtp_user and smtp_pass and recipient:
        try:
            booking_url = f"https://www.google.com/travel/flights?q=flights+from+{config['departure_city']}+to+{config['arrival_city']}+on+{flight['flight_date']}"
            body = (
                f"Great news!\n\n"
                f"Flight Details:\n"
                f"- Route: {config['departure_city']} → {config['arrival_city']}\n"
                f"- Date: {flight['flight_date']}\n"
                f"- Airline: {flight['airline']} ({flight['flight_number']})\n"
                f"- Departure: {flight['departure_time']} | Arrival: {flight['arrival_time']}\n"
                f"- Price: ₹{flight['price']} (Budget Threshold: ₹{config['budget_threshold']})\n\n"
                f"🔗 Book Flight on Google Flights:\n{booking_url}\n"
            )
            msg = MIMEMultipart()
            msg['From'] = smtp_user
            msg['To'] = recipient
            msg['Subject'] = subject
            msg.attach(MIMEText(body, 'plain'))
            with smtplib.SMTP(smtp_server, smtp_port) as server:
                server.starttls()
                server.login(smtp_user, smtp_pass)
                server.sendmail(smtp_user, recipient, msg.as_string())
            print(f"✅ Email notification sent to {recipient}")
        except Exception as e:
            print(f"❌ Failed to send SMTP alert: {e}")

if __name__ == "__main__":
    main()

