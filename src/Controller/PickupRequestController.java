package Controller;

import Database.DatabaseConnection;
import Model.PickupRequest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PickupRequestController {

    private final Connection connection;

    public PickupRequestController() {
        this.connection = DatabaseConnection.getConnection();
    }

    // CREATE: Tambah permintaan baru
    public void addRequest(PickupRequest request) throws SQLException {
        String query = "INSERT INTO pickup_requests (request_id, user_id, courier_id, status, points, waste_type) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, request.getRequestId()); // Request ID diambil dari input pengguna
            stmt.setString(2, request.getUserId());
            stmt.setString(3, request.getCourierId());
            stmt.setString(4, request.getStatus());
            stmt.setInt(5, request.getPoints());
            stmt.setString(6, request.getWasteType()); // Tambah waste type

            stmt.executeUpdate();
        }
    }

    // Ambil semua User ID dari tabel Users
    public List<String> getAllUserIds() throws SQLException {
        List<String> userIds = new ArrayList<>();
        String query = "SELECT user_id FROM Users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                userIds.add(rs.getString("user_id"));
            }
        }
        return userIds;
    }

    // Ambil semua Courier ID dari tabel Couriers
    public List<String> getAllCourierIds() throws SQLException {
        List<String> courierIds = new ArrayList<>();
        String query = "SELECT courier_id FROM Couriers";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                courierIds.add(rs.getString("courier_id"));
            }
        }
        return courierIds;
    }

    // READ: Ambil semua data permintaan
    public List<PickupRequest> getAllRequests() throws SQLException {
        List<PickupRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM pickup_requests";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                requests.add(new PickupRequest(
                    rs.getString("request_id"),
                    rs.getString("user_id"),
                    rs.getString("courier_id"),
                    rs.getString("status"),
                    rs.getInt("points"),
                    rs.getString("waste_type")  // Tambah waste type
                ));
            }
        }
        return requests;
    }


    // DELETE: Hapus data permintaan berdasarkan request_id
    public void deleteRequest(String requestId) throws SQLException {
        String query = "DELETE FROM pickup_requests WHERE request_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, requestId);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted == 0) {
                throw new SQLException("No request found with ID: " + requestId);
            }
        }
    }

    public List<PickupRequest> getRequestsByStatus(String status) throws SQLException {
        List<PickupRequest> requests = new ArrayList<>();
        String query = "SELECT * FROM pickup_requests WHERE status = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PickupRequest request = new PickupRequest(
                        rs.getString("request_id"),
                        rs.getString("user_id"),
                        rs.getString("courier_id"),
                        rs.getString("status"),
                        rs.getInt("points"),
                        rs.getString("waste_type")
                );
                requests.add(request);
            }
        }
        return requests;
    }

    public List<PickupRequest> trackRequest(String requestId, String userId, String courierId, String wasteType) throws SQLException {
        List<PickupRequest> requests = new ArrayList<>();
        StringBuilder query = new StringBuilder("SELECT * FROM pickup_requests WHERE 1=1");

        if (!requestId.isEmpty()) query.append(" AND request_id = ?");
        if (!userId.isEmpty()) query.append(" AND user_id = ?");
        if (!courierId.isEmpty()) query.append(" AND courier_id = ?");
        if (!wasteType.isEmpty()) query.append(" AND waste_type = ?");

        try (PreparedStatement stmt = connection.prepareStatement(query.toString())) {
            int index = 1;
            if (!requestId.isEmpty()) stmt.setString(index++, requestId);
            if (!userId.isEmpty()) stmt.setString(index++, userId);
            if (!courierId.isEmpty()) stmt.setString(index++, courierId);
            if (!wasteType.isEmpty()) stmt.setString(index++, wasteType);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PickupRequest request = new PickupRequest(
                        rs.getString("request_id"),
                        rs.getString("user_id"),
                        rs.getString("courier_id"),
                        rs.getString("status"),
                        rs.getInt("points"),
                        rs.getString("waste_type")
                );
                requests.add(request);
            }
        }
        return requests;
    }

    public int getTotalPointsForCourier(String courierId) throws SQLException {
        String query = "SELECT SUM(points) AS total_points FROM pickup_requests WHERE courier_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, courierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("total_points");
                } else {
                    return 0; // Return 0 if there's no data
                }
            }
        }
    }


    public void updateRequest(PickupRequest request) throws SQLException {
        String query = "UPDATE pickup_requests SET user_id = ?, courier_id = ?, status = ?, points = ?, waste_type = ? WHERE request_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, request.getUserId());
            stmt.setString(2, request.getCourierId());
            stmt.setString(3, request.getStatus());
            stmt.setInt(4, request.getPoints());
            stmt.setString(5, request.getWasteType());
            stmt.setString(6, request.getRequestId());

            stmt.executeUpdate();
        }
    }
}
