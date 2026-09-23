import os
import sys
import datetime
import random
import mysql.connector
from dotenv import load_dotenv

import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

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

def generate_mock_flight_data(departure, arrival, date):
    """Generate fake flight data for testing purposes."""
    airlines = ['IndiGo', 'Air India', 'Vistara', 'SpiceJet', 'Akasa Air']
    
    # Generate 1 to 3 flights for the given date
    num_flights = random.randint(1, 3)
    flights = []
    
    for _ in range(num_flights):
        airline = random.choice(airlines)
        flight_num = f"{airline[:2].upper()}-{random.randint(100, 999)}"
        
        # Random times
        dep_hour = random.randint(5, 22)
        dep_minute = random.choice([0, 15, 30, 45])
        
        # Assume 2 hour flight roughly
        arr_hour = (dep_hour + 2) % 24
        
        dep_time = f"{dep_hour:02d}:{dep_minute:02d}:00"
        arr_time = f"{arr_hour:02d}:{dep_minute:02d}:00"
        
        # Random price (fluctuates, sometimes hits below 5000 budget)
        price = round(random.uniform(3500, 12000), 2)
        
        flights.append({
            'airline': airline,
            'flight_number': flight_num,
            'departure_time': dep_time,
            'arrival_time': arr_time,
            'price': price,
            'is_direct': True
        })
        
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
            
            # Fetch mock data
            flights = generate_mock_flight_data(config['departure_city'], config['arrival_city'], current_date)
            
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
            subject = f"✈️ Flight Deal Alert: {config['departure_city']} to {config['arrival_city']} for ₹{flight['price']}"
            body = (
                f"Great news!\n\n"
                f"Flight Details:\n"
                f"- Route: {config['departure_city']} → {config['arrival_city']}\n"
                f"- Date: {flight['flight_date']}\n"
                f"- Airline: {flight['airline']} ({flight['flight_number']})\n"
                f"- Departure: {flight['departure_time']} | Arrival: {flight['arrival_time']}\n"
                f"- Price: ₹{flight['price']} (Budget Threshold: ₹{config['budget_threshold']})\n"
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

