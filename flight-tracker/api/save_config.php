<?php
// api/save_config.php
require_once '../config.php';

header('Content-Type: application/json');

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $departure = $_POST['departure_city'] ?? '';
    $arrival = $_POST['arrival_city'] ?? '';
    $date = $_POST['preferred_date'] ?? '';
    $budget = (int)($_POST['budget_threshold'] ?? 0);
    $flight_type = $_POST['flight_type'] ?? 'ALL';
    $active = isset($_POST['active']) ? (int)$_POST['active'] : 1;
    $id = isset($_POST['id']) ? (int)$_POST['id'] : 0;

    if (empty($departure) || empty($arrival) || empty($date) || $budget <= 0) {
        echo json_encode(['success' => false, 'error' => 'Invalid input']);
        exit;
    }

    try {
        if ($id > 0) {
            $stmt = $pdo->prepare("UPDATE search_configs SET departure_city = ?, arrival_city = ?, preferred_date = ?, budget_threshold = ?, flight_type = ?, active = ? WHERE id = ?");
            $stmt->execute([$departure, $arrival, $date, $budget, $flight_type, $active, $id]);
            echo json_encode(['success' => true, 'message' => 'Config updated']);
        } else {
            $stmt = $pdo->prepare("INSERT INTO search_configs (departure_city, arrival_city, preferred_date, budget_threshold, flight_type, active) VALUES (?, ?, ?, ?, ?, ?)");
            $stmt->execute([$departure, $arrival, $date, $budget, $flight_type, $active]);
            echo json_encode(['success' => true, 'message' => 'Config saved', 'id' => $pdo->lastInsertId()]);
        }
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'error' => $e->getMessage()]);
    }
} else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    try {
        $stmt = $pdo->query("SELECT * FROM search_configs ORDER BY created_at DESC");
        $configs = $stmt->fetchAll();
        echo json_encode(['success' => true, 'data' => $configs]);
    } catch (PDOException $e) {
        echo json_encode(['success' => false, 'error' => $e->getMessage()]);
    }
}
?>
