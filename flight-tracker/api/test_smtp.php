<?php
// api/test_smtp.php
header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Invalid request method']);
    exit;
}

$server = $_POST['SMTP_SERVER'] ?? '';
$port = (int)($_POST['SMTP_PORT'] ?? 587);
$user = $_POST['SMTP_USER'] ?? '';
$pass = $_POST['SMTP_PASS'] ?? '';
$recipient = $_POST['ALERT_RECIPIENT'] ?? '';

if (empty($server) || empty($user) || empty($pass) || empty($recipient)) {
    echo json_encode(['success' => false, 'error' => 'Please fill in all SMTP fields (Server, Port, User, Password, Recipient).']);
    exit;
}

function testSmtpPurePhp($server, $port, $user, $pass, $recipient) {
    $timeout = 10;
    
    // Connect to server
    if ($port == 465) {
        $socket = @fsockopen("ssl://{$server}", $port, $errno, $errstr, $timeout);
    } else {
        $socket = @fsockopen($server, $port, $errno, $errstr, $timeout);
    }

    if (!$socket) {
        return "Could not connect to SMTP server {$server}:{$port} - {$errstr} ({$errno})";
    }

    $read = function() use ($socket) {
        $response = "";
        while ($line = fgets($socket, 512)) {
            $response .= $line;
            if (substr($line, 3, 1) == " ") break;
        }
        return $response;
    };

    $send = function($cmd) use ($socket, $read) {
        fputs($socket, $cmd . "\r\n");
        return $read();
    };

    $res = $read();
    if (substr($res, 0, 3) != '220') {
        fclose($socket);
        return "Server greeting failed: " . trim($res);
    }

    // EHLO
    $res = $send("EHLO " . gethostname());
    
    // STARTTLS if port 587 or 25
    if ($port != 465) {
        $res = $send("STARTTLS");
        if (substr($res, 0, 3) != '220') {
            fclose($socket);
            return "STARTTLS failed: " . trim($res);
        }
        // Enable TLS encryption on stream
        $cryptoMethod = STREAM_CRYPTO_METHOD_TLS_CLIENT;
        if (defined('STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT')) {
            $cryptoMethod |= STREAM_CRYPTO_METHOD_TLSv1_2_CLIENT;
        }
        if (defined('STREAM_CRYPTO_METHOD_TLSv1_3_CLIENT')) {
            $cryptoMethod |= STREAM_CRYPTO_METHOD_TLSv1_3_CLIENT;
        }
        $cryptoResult = @stream_socket_enable_crypto($socket, true, $cryptoMethod);
        if (!$cryptoResult) {
            fclose($socket);
            return "Failed to establish TLS encryption stream with {$server}.";
        }
        // Re-EHLO after TLS
        $res = $send("EHLO " . gethostname());
    }

    // AUTH LOGIN
    $res = $send("AUTH LOGIN");
    if (substr($res, 0, 3) != '334') {
        fclose($socket);
        return "AUTH LOGIN failed: " . trim($res);
    }

    $res = $send(base64_encode($user));
    if (substr($res, 0, 3) != '334') {
        fclose($socket);
        return "Username rejected: " . trim($res);
    }

    $res = $send(base64_encode($pass));
    if (substr($res, 0, 3) != '235') {
        fclose($socket);
        return "Authentication failed (Bad Username or App Password): " . trim($res);
    }

    // MAIL FROM
    $res = $send("MAIL FROM: <{$user}>");
    if (substr($res, 0, 3) != '250') {
        fclose($socket);
        return "MAIL FROM rejected: " . trim($res);
    }

    // RCPT TO
    $res = $send("RCPT TO: <{$recipient}>");
    if (substr($res, 0, 3) != '250' && substr($res, 0, 3) != '251') {
        fclose($socket);
        return "Recipient rejected: " . trim($res);
    }

    // DATA
    $res = $send("DATA");
    if (substr($res, 0, 3) != '354') {
        fclose($socket);
        return "DATA command rejected: " . trim($res);
    }

    // Build RFC 2822 compliant email headers
    $domain = strpos($user, '@') !== false ? explode('@', $user)[1] : 'gmail.com';
    $messageId = "<" . time() . "." . uniqid() . "@" . $domain . ">";
    $date = date("r");

    $headers = [
        "Date: {$date}",
        "From: FlightTracker Alert System <{$user}>",
        "To: <{$recipient}>",
        "Subject: =?UTF-8?B?" . base64_encode("✈️ FlightTracker SMTP Test Successful!") . "?=",
        "Message-ID: {$messageId}",
        "MIME-Version: 1.0",
        "Content-Type: text/plain; charset=UTF-8",
        "Content-Transfer-Encoding: 8bit"
    ];

    $body = "Congratulations!\r\n\r\n" .
            "Your FlightTracker SMTP email configuration is working perfectly.\r\n" .
            "You will receive automated flight deal notifications at this email address.\r\n";

    $emailData = implode("\r\n", $headers) . "\r\n\r\n" . $body . "\r\n.";

    $res = $send($emailData);
    if (substr($res, 0, 3) != '250') {
        fclose($socket);
        return "Failed to queue test message: " . trim($res);
    }

    $send("QUIT");
    fclose($socket);
    return true;
}

$result = testSmtpPurePhp($server, $port, $user, $pass, $recipient);

if ($result === true) {
    echo json_encode([
        'success' => true,
        'message' => "Test email successfully sent to {$recipient}!"
    ]);
} else {
    echo json_encode([
        'success' => false,
        'error' => $result
    ]);
}
?>
