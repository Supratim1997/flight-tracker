<?php
// api/get_access_logs.php
require_once '../config.php';

header('Content-Type: application/json');

$config_id = isset($_GET['config_id']) ? (int)$_GET['config_id'] : 0;

try {
    if ($config_id > 0) {
        $stmt = $pdo->prepare("
            SELECT pal.id, pal.config_id, pal.flight_number, pal.flight_date, pal.airline, pal.source_name, pal.accessed_url, pal.price_received, pal.accessed_at, sc.departure_city, sc.arrival_city 
            FROM price_access_logs pal
            LEFT JOIN search_configs sc ON pal.config_id = sc.id
            WHERE pal.config_id = ?
            ORDER BY pal.id DESC
            LIMIT 100
        ");
        $stmt->execute([$config_id]);
    } else {
        $stmt = $pdo->prepare("
            SELECT pal.id, pal.config_id, pal.flight_number, pal.flight_date, pal.airline, pal.source_name, pal.accessed_url, pal.price_received, pal.accessed_at, sc.departure_city, sc.arrival_city 
            FROM price_access_logs pal
            LEFT JOIN search_configs sc ON pal.config_id = sc.id
            ORDER BY pal.id DESC
            LIMIT 100
        ");
        $stmt->execute();
    }
    
    $logs = $stmt->fetchAll();

    echo json_encode([
        'success' => true,
        'data' => $logs
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'error' => $e->getMessage()]);
}
?>
