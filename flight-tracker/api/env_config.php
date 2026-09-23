<?php
// api/env_config.php
header('Content-Type: application/json');

$envPath = realpath(__DIR__ . '/../scraper/.env');
$exampleEnvPath = realpath(__DIR__ . '/../scraper/.env.example');

if (!$envPath || !file_exists($envPath)) {
    $envPath = __DIR__ . '/../scraper/.env';
}

function parseEnvFile($filePath, $fallbackPath = null) {
    $targetFile = file_exists($filePath) ? $filePath : $fallbackPath;
    $env = [];
    if (!$targetFile || !file_exists($targetFile)) {
        return $env;
    }
    $lines = file($targetFile, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
    foreach ($lines as $line) {
        $line = trim($line);
        if (empty($line) || strpos($line, '#') === 0) {
            continue;
        }
        if (strpos($line, '=') !== false) {
            list($key, $val) = explode('=', $line, 2);
            $key = trim($key);
            $val = trim($val);
            // Remove optional outer quotes (PHP 7 & 8 compatible)
            $first = substr($val, 0, 1);
            $last = substr($val, -1);
            if (($first === '"' && $last === '"') || ($first === "'" && $last === "'")) {
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
    $envData = parseEnvFile($envPath, $exampleEnvPath);
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
