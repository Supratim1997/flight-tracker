<?php
// api/trigger_scrape.php
header('Content-Type: application/json');

$scraper_path = realpath(__DIR__ . '/../scraper/tracker.py');
$scraper_dir = realpath(__DIR__ . '/../scraper');

if (!$scraper_path || !$scraper_dir) {
    echo json_encode(['success' => false, 'error' => 'Scraper script not found']);
    exit;
}

$activate_bat = $scraper_dir . '\venv\Scripts\activate.bat';

if (file_exists($activate_bat)) {
    // Activate virtual environment first before running Python
    $command = "cd /d \"{$scraper_dir}\" && call \"{$activate_bat}\" && python \"{$scraper_path}\"";
} else {
    $command = "cd /d \"{$scraper_dir}\" && python \"{$scraper_path}\"";
}

$output = shell_exec($command . ' 2>&1');

echo json_encode([
    'success' => true,
    'message' => 'Scraper execution triggered',
    'output' => $output
]);
?>
