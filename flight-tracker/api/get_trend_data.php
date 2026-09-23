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
    $stmt = $pdo->prepare("SELECT departure_city, arrival_city, preferred_date, budget_threshold, flight_type FROM search_configs WHERE id = ?");
    $stmt->execute([$config_id]);
    $config = $stmt->fetch();

    if (!$config) {
        echo json_encode(['success' => false, 'error' => 'Config not found']);
        exit;
    }

    $stmt = $pdo->prepare("
        SELECT ph.id, ph.flight_date, ph.price_inr as min_price, ph.airline, ph.flight_number, ph.departure_time, ph.arrival_time, ph.is_direct, ph.stops_info 
        FROM price_history ph
        INNER JOIN (
            SELECT flight_date, MIN(price_inr) as lowest_price 
            FROM price_history 
            WHERE config_id = ? 
            GROUP BY flight_date
        ) min_ph ON ph.flight_date = min_ph.flight_date AND ph.price_inr = min_ph.lowest_price
        WHERE ph.config_id = ?
        GROUP BY ph.flight_date
        ORDER BY ph.flight_date ASC
    ");
    $stmt->execute([$config_id, $config_id]);
    $history = $stmt->fetchAll();

    echo json_encode([
        'success' => true, 
        'data' => $history, 
        'departure_city' => $config['departure_city'],
        'arrival_city' => $config['arrival_city'],
        'budget_threshold' => $config['budget_threshold'],
        'preferred_date' => $config['preferred_date'],
        'flight_type' => $config['flight_type'] ?? 'ALL'
    ]);
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'error' => $e->getMessage()]);
}
?>
