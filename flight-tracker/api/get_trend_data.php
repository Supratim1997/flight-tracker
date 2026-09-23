<?php
// api/get_trend_data.php
require_once '../config.php';

header('Content-Type: application/json');

$config_id = isset($_GET['config_id']) ? (int)$_GET['config_id'] : 0;

if ($config_id <= 0) {
    echo json_encode(['success' => false, 'error' => 'Invalid config ID']);
    exit;
}

try {
    $stmt = $pdo->prepare("SELECT departure_city, arrival_city, preferred_date, budget_threshold FROM search_configs WHERE id = ?");
    $stmt->execute([$config_id]);
    $config = $stmt->fetch();

    if (!$config) {
        echo json_encode(['success' => false, 'error' => 'Config not found']);
        exit;
    }

    $stmt = $pdo->prepare("
        SELECT flight_date, MIN(price_inr) as min_price, airline, flight_number, departure_time, arrival_time 
        FROM price_history 
        WHERE config_id = ? AND is_direct = 1
        GROUP BY flight_date 
        ORDER BY flight_date ASC
    ");
    $stmt->execute([$config_id]);
    $history = $stmt->fetchAll();

    echo json_encode([
        'success' => true, 
        'data' => $history, 
        'departure_city' => $config['departure_city'],
        'arrival_city' => $config['arrival_city'],
        'budget_threshold' => $config['budget_threshold'],
        'preferred_date' => $config['preferred_date']
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'error' => $e->getMessage()]);
}
?>
