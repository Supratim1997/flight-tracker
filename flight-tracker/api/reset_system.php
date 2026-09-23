<?php
// api/reset_system.php
require_once '../config.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'error' => 'Invalid request method']);
    exit;
}

$action = $_POST['action'] ?? '';

if ($action === 'reset_profiles') {
    try {
        $pdo->exec("SET FOREIGN_KEY_CHECKS=0;");
        $pdo->exec("TRUNCATE TABLE alert_logs;");
        $pdo->exec("TRUNCATE TABLE price_access_logs;");
        $pdo->exec("TRUNCATE TABLE price_history;");
        $pdo->exec("TRUNCATE TABLE search_configs;");
        $pdo->exec("SET FOREIGN_KEY_CHECKS=1;");

        echo json_encode([
            'success' => true,
            'message' => 'All flight profiles, price history, and alert logs have been completely reset!'
        ]);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'error' => $e->getMessage()]);
    }
    exit;
}

if ($action === 'reset_smtp') {
    $envPath = realpath(__DIR__ . '/../scraper/.env');
    if (!$envPath || !file_exists($envPath)) {
        $envPath = __DIR__ . '/../scraper/.env';
    }

    $defaultSmtpVars = [
        'SMTP_SERVER' => 'smtp.gmail.com',
        'SMTP_PORT' => '587',
        'SMTP_USER' => 'your_email@gmail.com',
        'SMTP_PASS' => 'your_app_password',
        'ALERT_RECIPIENT' => 'alert_recipient@example.com'
    ];

    try {
        $existingLines = file_exists($envPath) ? file($envPath, FILE_IGNORE_NEW_LINES) : [];
        $updatedKeys = [];
        $newLines = [];

        foreach ($existingLines as $line) {
            $trimmed = trim($line);
            if (empty($trimmed) || strpos($trimmed, '#') === 0) {
                $newLines[] = $line;
                continue;
            }
            if (strpos($trimmed, '=') !== false) {
                list($key, $val) = explode('=', $trimmed, 2);
                $key = trim($key);
                if (array_key_exists($key, $defaultSmtpVars)) {
                    $newLines[] = "{$key}=" . $defaultSmtpVars[$key];
                    $updatedKeys[] = $key;
                } else {
                    $newLines[] = $line;
                }
            } else {
                $newLines[] = $line;
            }
        }

        foreach ($defaultSmtpVars as $key => $val) {
            if (!in_array($key, $updatedKeys)) {
                $newLines[] = "{$key}={$val}";
            }
        }

        file_put_contents($envPath, implode("\n", $newLines) . "\n");

        echo json_encode([
            'success' => true,
            'message' => 'SMTP configuration has been reset to default template values!'
        ]);
    } catch (Exception $e) {
        echo json_encode(['success' => false, 'error' => 'Failed to reset SMTP config: ' . $e->getMessage()]);
    }
    exit;
}

echo json_encode(['success' => false, 'error' => 'Invalid action parameter']);
?>
