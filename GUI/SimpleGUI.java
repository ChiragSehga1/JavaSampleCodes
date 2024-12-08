import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class SwingOrders extends JFrame {
    private final Controller controller;
    private final Customer customer;
    private final Map<String, MenuItem> menu;
    private final Object lock = new Object();
    private DefaultTableModel tableModel;
    private JTable itemTable;
    private List<Order> orders;
    public static SwingGUI pass;
    private JButton switchButton;

    public SwingOrders(SwingGUI pass) {
        this.pass = pass;
        this.controller = pass.controller;
        this.customer = pass.customer;
        this.menu = controller.menu;
        this.orders = new ArrayList<>();

        // Initialize orders list
        if (customer != null) {
            for (String id : customer.history) {
                orders.add(controller.sortedOrders.get(id));
            }
            if (customer.ongoingOrder != null) {
                orders.add(customer.ongoingOrder);
            }
        } else {
            orders = new ArrayList<>(Admin.getInstance().pendingOrders.values());
            orders.addAll(Admin.getInstance().sortedOrders.values());
        }

        initializeUI();
    }

    public SwingOrders(Controller controller, Customer customer) {
        this.controller = controller;
        this.customer = customer;
        this.menu = controller.menu;
        this.orders = new ArrayList<>();

        // Initialize orders list
        if (customer != null) {
            for (String id : customer.history) {
                orders.add(controller.sortedOrders.get(id));
            }
            if (customer.ongoingOrder != null) {
                orders.add(customer.ongoingOrder);
            }
        } else {
            orders = new ArrayList<>(Admin.getInstance().pendingOrders.values());
            orders.addAll(Admin.getInstance().sortedOrders.values());
        }

        initializeUI();
    }

    public void waitForClose() {
        synchronized (lock) {
            try {
                lock.wait(); // Block until the window is closed
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Waiting for window close interrupted: " + e.getMessage());
            }
        }
    }

    private void switchGUI(){
        this.setVisible(false);
        pass.refresh();
        pass.setVisible(true);
    }

    public void initializeUI() {
        String font = "Segoe UI";
        UIManager.put("Label.font", new Font(font, Font.PLAIN, 14));
        UIManager.put("Button.font", new Font(font, Font.PLAIN, 14));

        setTitle("Pending Orders");
        setSize(1200, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Trigger the `windowClosed` event
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Add window listener to notify waiting threads
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                synchronized (lock) {
                    lock.notify();
                }
            }
        });

        // Initialize table model
        tableModel = new DefaultTableModel(new String[]{"OrderID", "CustomerID", "Status", "Items Summary"}, 0);
        itemTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                String status = (String) tableModel.getValueAt(row, 2); // Status column

                if ("Pending".equalsIgnoreCase(status)) {
                    component.setBackground(Color.RED); // Pending orders in red
                    component.setForeground(Color.WHITE);
                } else if ("Completed".equalsIgnoreCase(status)) {
                    component.setBackground(Color.GREEN); // Completed orders in green
                    component.setForeground(Color.BLACK);
                } else {
                    component.setBackground(Color.BLUE); // Default text color
                }

                return component;
            }
        };
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setFont(new Font(font, Font.PLAIN, 14));
        itemTable.setRowHeight(25);
        itemTable.setFillsViewportHeight(true);

        // Add mouse listener for item selection
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int selectedRow = itemTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
                        Order selectedOrder = fetchOrderById(orderId);
                        if (selectedOrder != null) {
                            showOrderDetails(selectedOrder);
                        }
                    }
                }
            }
        });

        // Add components to the frame
        JScrollPane tableScrollPane = new JScrollPane(itemTable);
        add(tableScrollPane, BorderLayout.CENTER);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        switchButton = new JButton("Go to Menu");
        switchButton.addActionListener(e -> switchGUI());
        topPanel.add(switchButton);
        add(topPanel, BorderLayout.SOUTH);
        // Display initial orders
        displayAllOrders();
    }

    private Order fetchOrderById(String orderId) {
        Order get = null;
        if (customer == null) {
            get = controller.pendingOrders.get(orderId);
            if (get == null) {
                get = controller.sortedOrders.get(orderId);
            }
        } else {
            if (Objects.equals(orderId, customer.ongoingOrder.orderID)) {
                return customer.ongoingOrder;
            }
            return controller.sortedOrders.get(orderId);
        }
        return get;
    }

    private void displayAllOrders() {
        tableModel.setRowCount(0);
        for (Order order : orders) {
            String itemsSummary = order.getOrder().entrySet().stream()
                    .map(entry -> entry.getValue() + "x " + menu.get(entry.getKey()).name)
                    .collect(Collectors.joining(", "));
            tableModel.addRow(new Object[]{order.orderID, order.custID, order.status, itemsSummary});
        }
    }

    public void refreshOrders() {
        orders.clear();
        if (customer != null) {
            for (String id : customer.history) {
                orders.add(controller.sortedOrders.get(id));
            }
            if (customer.ongoingOrder != null) {
                orders.add(customer.ongoingOrder);
            }
        } else {
            orders.addAll(Admin.getInstance().pendingOrders.values());
            orders.addAll(Admin.getInstance().sortedOrders.values());
        }
        displayAllOrders(); // Re-populate the table
    }

    private void showOrderDetails(Order order) {
        JDialog dialog = new JDialog(this, "Order Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);

        // Panel for order details
        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        if (order != null) {
            detailsPanel.add(new JLabel("Order ID: " + order.orderID));
            detailsPanel.add(new JLabel("Customer ID: " + order.custID));
            JLabel statusLabel = new JLabel("Status: " + order.status);
            statusLabel.setForeground(Color.BLUE); // Set the text color to blue
            detailsPanel.add(statusLabel);
            detailsPanel.add(new JLabel("Instructions: " + (order.instructions != null ? order.instructions : "N/A")));

            if (order.getOrder() != null) {
                for (Map.Entry<String, Integer> entry : order.getOrder().entrySet()) {
                    MenuItem food = menu.get(entry.getKey());
                    Integer quantity = entry.getValue();
                    detailsPanel.add(new JLabel(food.itemID + " | " + food.name + " x " + quantity));
                }
            }

            detailsPanel.add(new JLabel("Total: ₹" + order.calculateTotalCost()));
        }

        // Scrollable view for order details
        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        dialog.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        if (customer == null && !order.status.equalsIgnoreCase("Completed")) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton completeButton = new JButton("Mark as Completed");

            // Add action listeners for the buttons
            completeButton.addActionListener(e -> {
                // Mark the order as completed
                Admin.getInstance().pendingOrders.remove(order.orderID);
                Admin.getInstance().sortedOrders.put(order.orderID, order);
                order.setStatus("Completed");
                Customer cust = controller.users.get(order.custID);
                if (cust != null) {
                    cust.ongoingOrder = null;
                    cust.history.add(order.orderID);
                }

                // Refresh the orders list and table
                refreshOrders();

                // Close the dialog
                dialog.dispose();
            });
            buttonPanel.add(completeButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
        }
        dialog.setVisible(true);
    }

}
