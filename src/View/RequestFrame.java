package View;

import Controller.PickupRequestController;
import Model.PickupRequest;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.SQLException;
import java.util.List;

public class RequestFrame extends JPanel {
    private JPanel mainPanel;
    private PickupRequestController controller;
    private DefaultTableModel tableModel;
    private String currentStatusFilter = null;

    public RequestFrame(JPanel mainPanel) {
        this.mainPanel = mainPanel;
        this.controller = new PickupRequestController();
        initializeUI();
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                refreshData();
            }
        });
        
        loadData(null);
    }

    public void refreshData() {
        loadData(currentStatusFilter);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        JLabel lblRequest = new JLabel("Requests", SwingConstants.CENTER);
        lblRequest.setFont(new Font("Arial", Font.BOLD, 24));
        add(lblRequest, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[] {
                        "ID Permintaan",
                        "ID Pengguna",
                        "ID Kurir",
                        "Status",
                        "Poin",
                        "Jenis Sampah"
                },
                0
        );
        JTable table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 10, 10));
        JButton btnViewAll = new JButton("View All Requests");
        JButton btnViewCompleted = new JButton("View Completed Requests");
        JButton btnViewPending = new JButton("View Pending Requests");
        JButton btnViewOngoing = new JButton("View Ongoing Requests");
        JButton btnTrackRequest = new JButton("Track Request");
        JButton btnBack = new JButton("Back to Dashboard");

        buttonPanel.add(btnViewAll);
        buttonPanel.add(btnViewCompleted);
        buttonPanel.add(btnViewPending);
        buttonPanel.add(btnViewOngoing);
        buttonPanel.add(btnTrackRequest);
        buttonPanel.add(btnBack);

        add(buttonPanel, BorderLayout.SOUTH);

        btnViewAll.addActionListener(e -> loadData(null));
        btnViewCompleted.addActionListener(e -> loadData("Completed"));
        btnViewPending.addActionListener(e -> loadData("Pending"));
        btnViewOngoing.addActionListener(e -> loadData("Ongoing"));
        btnTrackRequest.addActionListener(e -> trackRequest());
        btnBack.addActionListener(e -> navigateBack());
    }

    private void loadData(String statusFilter) {
        this.currentStatusFilter = statusFilter;
        try {
            List<PickupRequest> requests;
            if (statusFilter == null) {
                requests = controller.getAllRequests();
            } else {
                requests = controller.getRequestsByStatus(statusFilter);
            }
            tableModel.setRowCount(0);
            for (PickupRequest request : requests) {
                tableModel.addRow(new Object[] {
                        request.getRequestId(),
                        request.getUserId(),
                        request.getCourierId(),
                        request.getStatus(),
                        request.getPoints(),
                        request.getWasteType()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void trackRequest() {
        JTextField requestIdField = new JTextField();
        JComboBox<String> userIdComboBox = new JComboBox<>();
        JComboBox<String> courierIdComboBox = new JComboBox<>();
        JTextField wasteTypeField = new JTextField();

        try {
            List<String> userIds = controller.getAllUserIds();
            List<String> courierIds = controller.getAllCourierIds();

            for (String userId : userIds) {
                userIdComboBox.addItem(userId);
            }
            for (String courierId : courierIds) {
                courierIdComboBox.addItem(courierId);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to load users or couriers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Object[] fields = {
                "Request ID:", requestIdField,
                "User ID:", userIdComboBox,
                "Courier ID:", courierIdComboBox,
                "Waste Type:", wasteTypeField
        };

        int option = JOptionPane.showConfirmDialog(this, fields, "Track Request", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String requestId = requestIdField.getText().trim();
                String userId = (String) userIdComboBox.getSelectedItem();
                String courierId = (String) courierIdComboBox.getSelectedItem();
                String wasteType = wasteTypeField.getText().trim();

                if (requestId.isEmpty() && userId == null && courierId == null && wasteType.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "At least one field must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                List<PickupRequest> results = controller.trackRequest(requestId, userId, courierId, wasteType);
                tableModel.setRowCount(0);
                for (PickupRequest request : results) {
                    tableModel.addRow(new Object[] {
                            request.getRequestId(),
                            request.getUserId(),
                            request.getCourierId(),
                            request.getStatus(),
                            request.getPoints(),
                            request.getWasteType()
                    });
                }

                if (results.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No matching requests found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Failed to track request: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void navigateBack() {
        CardLayout cardLayout = (CardLayout) mainPanel.getLayout();
        cardLayout.show(mainPanel, "Dashboard");
    }
}
