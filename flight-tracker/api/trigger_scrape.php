<?php
// api/trigger_scrape.php
header('Content-Type: application/json');

// Get the absolute path to the Python scraper script
$scraper_path = realpath(__DIR__ . '/../scraper/tracker.py');
$python_executable = 'python'; // or full path to python, e.g., 'C:\\path\\to\\python.exe' if needed

if (!$scraper_path) {
    echo json_encode(['success' => false, 'error' => 'Scraper script not found']);
    exit;
}

// In a real production system, this should be executed asynchronously
// e.g., redirecting output to > /dev/null 2>&1 & on Linux, or using start /B on Windows.
// For demonstration, we'll run it synchronously and capture output.
$command = escapeshellcmd("$python_executable \"$scraper_path\"");
$output = shell_exec($command . ' 2>&1');

echo json_encode([
    'success' => true,
    'message' => 'Scraper execution triggered',
    'output' => $output
]);
?>
