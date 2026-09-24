package com.flighttracker.util;

public final class AppConstants {

    private AppConstants() {
        // Private constructor to prevent instantiation
    }

    // Application & Security Defaults
    public static final String DEFAULT_SECRET_KEY = "FlightTrackerSecretKey2026#SecureAES";
    public static final String MASKED_PASSWORD = "••••••••";
    public static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    public static final String AES_ALGORITHM = "AES";
    public static final String HASH_ALGORITHM_SHA256 = "SHA-256";

    // Alert Notification Methods
    public static final String ALERT_METHOD_NONE = "NONE";
    public static final String ALERT_METHOD_SMTP = "SMTP";
    public static final String ALERT_METHOD_TELEGRAM = "TELEGRAM";
    public static final String ALERT_METHOD_BOTH = "BOTH";

    // Flight Preferences & Filter Types
    public static final String FLIGHT_TYPE_ALL = "ALL";
    public static final String FLIGHT_TYPE_DIRECT = "DIRECT";
    public static final String FLIGHT_TYPE_LAYOVER = "LAYOVER";

    // Mail & SMTP Defaults
    public static final String DEFAULT_SMTP_HOST = "smtp.gmail.com";
    public static final int DEFAULT_SMTP_PORT = 587;
    public static final String DEFAULT_SMTP_USER = "your_email@gmail.com";
    public static final String DEFAULT_SMTP_PASS = "your_app_password";
    public static final String DEFAULT_ALERT_RECIPIENT = "alert_recipient@example.com";
    public static final String MAIL_TRANSPORT_PROTOCOL = "smtp";
    public static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    public static final String MAIL_SMTP_STARTTLS = "mail.smtp.starttls.enable";
    public static final String MAIL_SMTP_TIMEOUT = "mail.smtp.timeout";
    public static final String MAIL_SMTP_CONN_TIMEOUT = "mail.smtp.connectiontimeout";
    public static final String DEFAULT_MAIL_TIMEOUT_MS = "5000";

    // Telegram API Constants
    public static final String TELEGRAM_API_BASE_URL = "https://api.telegram.org/bot";
    public static final String TELEGRAM_SEND_MESSAGE_PATH = "/sendMessage";
    public static final String TELEGRAM_PARSE_MODE_MARKDOWN = "Markdown";

    // Scraper & External URLs
    public static final String GOOGLE_FLIGHTS_SEARCH_URL = "https://www.google.com/travel/flights?q=";
    public static final String SCRAPER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";
    public static final String SCRAPER_ACCEPT_LANGUAGE = "en-IN,en;q=0.9";
    public static final String SCRAPER_SOURCE_NAME = "Google Flights Live Stream";
    public static final String DIRECT_STOPS_TEXT = "Direct";

    // API Response JSON Keys
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_ERROR = "error";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_DATA = "data";
    public static final String KEY_ID = "id";
    public static final String KEY_CONFIG_ID = "config_id";
    public static final String KEY_OUTPUT = "output";
    public static final String KEY_PREFERRED_DATE = "preferred_date";
    public static final String KEY_BUDGET_THRESHOLD = "budget_threshold";
    public static final String KEY_DEPARTURE_CITY = "departure_city";
    public static final String KEY_ARRIVAL_CITY = "arrival_city";
    public static final String KEY_FLIGHT_TYPE = "flight_type";
    public static final String KEY_FLIGHT_NUMBER = "flight_number";
    public static final String KEY_FLIGHT_DATE = "flight_date";
    public static final String KEY_AIRLINE = "airline";
    public static final String KEY_SOURCE_NAME = "source_name";
    public static final String KEY_SOURCE_URL = "source_url";
    public static final String KEY_ACCESSED_URL = "accessed_url";
    public static final String KEY_ACCESSED_AT = "accessed_at";
    public static final String KEY_PRICE_RECEIVED = "price_received";
    public static final String KEY_MIN_PRICE = "min_price";
    public static final String KEY_DEPARTURE_TIME = "departure_time";
    public static final String KEY_ARRIVAL_TIME = "arrival_time";
    public static final String KEY_IS_DIRECT = "is_direct";
    public static final String KEY_STOPS_INFO = "stops_info";
    public static final String KEY_DAILY_MIN = "daily_min";
    public static final String KEY_ALERT_METHOD = "alertMethod";
    public static final String KEY_SMTP_SERVER = "smtpServer";
    public static final String KEY_SMTP_PORT = "smtpPort";
    public static final String KEY_SMTP_USER = "smtpUser";
    public static final String KEY_ALERT_RECIPIENT_CAMEL = "alertRecipient";
    public static final String KEY_TELEGRAM_CHAT_ID = "telegramChatId";
    public static final String KEY_HAS_SMTP_PASS = "hasSmtpPass";
    public static final String KEY_HAS_TELEGRAM_TOKEN = "hasTelegramToken";

    // System Reset Actions
    public static final String ACTION_RESET_PROFILES = "reset_profiles";
    public static final String ACTION_RESET_SMTP = "reset_smtp";

    // Config & Settings Parameters
    public static final String PARAM_SMTP_SERVER = "SMTP_SERVER";
    public static final String PARAM_SMTP_PORT = "SMTP_PORT";
    public static final String PARAM_SMTP_USER = "SMTP_USER";
    public static final String PARAM_SMTP_PASS = "SMTP_PASS";
    public static final String PARAM_ALERT_RECIPIENT = "ALERT_RECIPIENT";

    // Map / Projections Keys
    public static final String MAP_KEY_FLIGHT_DATE = "flightDate";
    public static final String MAP_KEY_MIN_PRICE = "minPrice";

    // Flight Platform Providers
    public static final String PROVIDER_GOOGLE_FLIGHTS = "Google Flights";
    public static final String PROVIDER_EASEMYTRIP = "EaseMyTrip";
    public static final String PROVIDER_MAKEMYTRIP = "MakeMyTrip";
    public static final String PROVIDER_IXIGO = "Ixigo";
    public static final String PROVIDER_GOIBIBO = "Goibibo";
    public static final String PROVIDER_CLEARTRIP = "Cleartrip";
    public static final String PROVIDER_YATRA = "Yatra";
    public static final String PROVIDER_PAYTM = "Paytm";

    // Official Airline Direct Providers
    public static final String PROVIDER_INDIGO_DIRECT = "IndiGo Direct";
    public static final String PROVIDER_AIR_INDIA_DIRECT = "Air India Direct";
    public static final String PROVIDER_AKASA_DIRECT = "Akasa Air Direct";
    public static final String PROVIDER_SPICEJET_DIRECT = "SpiceJet Direct";
    public static final String PROVIDER_AIR_INDIA_EXPRESS_DIRECT = "Air India Express Direct";
}
