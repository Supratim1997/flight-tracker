<?php
// api/env_config.php
header('Content-Type: application/json');

$envPath = realpath(__DIR__ . '/../scraper/.env');
if (!$envPath) {
    // If .env doesn't exist yet, target the path where it should be created
    $envPath = __DIR__ . '/../scraper/.env';
}

function parseEnvFile($filePath) {
    $env = [];
    if (!file_exists($filePath)) {
        return $env;
    }
    $lines = file($filePath, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        $line = trim($line);
        if (empty($line) || strpos($line, '#') === 0) {
            continue;
        }
        if (strpos($line, '=') !== false) {
            list($key, $val) = explode('=', $line, 2);
            $key = trim($key);
            $val = trim($val);
            // Remove optional outer quotes
            if ((str_starts_with($val, '"') && str_ends_with($val, '"')) ||
                (str_starts_with($val, "'") && str_ends_with($val, "'"))) {
                $val = substr($val, 1, -1);
            }
            $env[$key] = $val;
        }
    }
    return $env;
}

function updateEnvFile($filePath, $newVars) {
    $existingLines = file_exists($filePath) ? file($filePath, FILE_IGNORE_NEW_LINES) : [];
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
            if (array_key_exists($key, $newVars)) {
                $newLines[] = "{$key}=" . $newVars[$key];
                $updatedKeys[] = $key;
            } else {
                $newLines[] = $line;
            }
        } else {
            $newLines[] = $line;
        }
    }

    // Append any new variables that weren't in the file yet
    foreach ($newVars as $key => $val) {
        if (!in_array($key, $updatedKeys)) {
            $newLines[] = "{$key}={$val}";
        }
    }

    file_put_contents($filePath, implode("\n", $newLines) . "\n");
}

if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $envData = parseEnvFile($envPath);
    echo json_encode([
        'success' => true,
        'data' => $envData
    ]);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $allowedKeys = [
        'DB_HOST', 'DB_USER', 'DB_PASS', 'DB_NAME',
        'SMTP_SERVER', 'SMTP_PORT', 'SMTP_USER', 'SMTP_PASS', 'ALERT_RECIPIENT',
        'TELEGRAM_BOT_TOKEN', 'TELEGRAM_CHAT_ID'
    ];

    $updates = [];
    foreach ($allowedKeys as $key) {
        if (isset($_POST[$key])) {
            $updates[$key] = trim($_POST[$key]);
        }
    }

    try {
        updateEnvFile($envPath, $updates);
        echo json_encode([
            'success' => true,
            'message' => 'Environment configuration updated successfully!'
        ]);
    } catch (Exception $e) {
        echo json_encode([
            'success' => false,
            'error' => 'Failed to write .env file: ' . $e->getMessage()
        ]);
    }
    exit;
}

echo json_encode(['success' => false, 'error' => 'Invalid request method']);
?>
