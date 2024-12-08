import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class SwingGUI extends JFrame {
    public final Controller controller;
    public final Customer customer;
    private final Map<String, MenuItem> menu;
    private final Object lock = new Object();
    private JTextArea cartTextArea;
    private DefaultListModel<MenuItem> listModel;
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> viewComboBox;
    private JTextField searchField;
    private JButton searchButton;
    private JComboBox<String> sortComboBox;
    private JButton clearButton;
    private JButton checkoutButton;
    private JButton switchButton;
    public static SwingOrders pass;

    public SwingGUI(Controller controller, Customer customer) {
        this.menu = controller.menu;
        this.controller = controller;
        this.customer = customer;
        pass = new SwingOrders(this);
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

    // Method to refresh the UI components
    public void refresh() {
        // Refresh the menu table
        pass.refreshOrders();
        displayAllItems();

        // Update the cart display if the customer is not null
        if (customer != null) {
            updateCartDisplay();
        }

        // Optionally, reset the search field and sorting combo box
        searchField.setText("");
        sortComboBox.setSelectedIndex(0);
    }

    public void initializeUI() {
        String font = "Segoe UI";
        UIManager.put("Label.font", new Font(font, Font.PLAIN, 14));
        UIManager.put("Button.font", new Font(font, Font.PLAIN, 14));

        if (customer == null) {
            setTitle("Welcome Admin - Close Window to Continue");
        } else {
            setTitle("Welcome " + customer.name);
        }
        setSize(1200, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Trigger the `windowClosed` event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                synchronized (lock) {
                    lock.notify(); // Notify the waiting thread
                }
            }
        });
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        listModel = new DefaultListModel<>();
        tableModel = new DefaultTableModel(new String[]{"Item ID", "Name", "Price", "Category", "Availability"}, 0);
        itemTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                String status = (String) tableModel.getValueAt(row, 4); // Status column

                if ("Unavailable".equalsIgnoreCase(status)) {
                    component.setBackground(Color.RED); // Pending orders in red
                    component.setForeground(Color.WHITE);
                } else if ("Available".equalsIgnoreCase(status)) {
                    component.setBackground(Color.WHITE); // Completed orders in green
                    component.setForeground(Color.BLACK);
                } else {
                    component.setBackground(Color.BLUE); // Default text color
                    component.setForeground(Color.WHITE);
                }
                return component;
            }
        };
        itemTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        itemTable.setFont(new Font(font, Font.PLAIN, 14));
        itemTable.setRowHeight(25);
        itemTable.setFillsViewportHeight(true);
        itemTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int selectedRow = itemTable.getSelectedRow();
                    if (selectedRow != -1) {
                        String itemId = (String) tableModel.getValueAt(selectedRow, 0);
                        MenuItem selectedItem = menu.get(itemId);
                        if (selectedItem != null) {
                            showItemDetails(selectedItem);
                        }
                    }
                }
            }
        });

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        viewComboBox = new JComboBox<>(new String[]{"View Items", "Search by Category", "Sort by Price"});
        searchPanel.add(viewComboBox);

        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        searchField = new JTextField();
        searchField.addActionListener(new SearchActionListener());
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    searchButton.doClick();
                }
            }
        });
        searchPanel.add(searchField);

        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        searchButton = new JButton("Search");
        searchButton.addActionListener(new SearchActionListener());
        searchPanel.add(searchButton);

        searchPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        sortComboBox = new JComboBox<>(new String[]{"Increasing", "Decreasing"});
        sortComboBox.addActionListener(new SearchActionListener());
        searchPanel.add(sortComboBox);

        topPanel.add(searchPanel);

        add(topPanel, BorderLayout.NORTH);

        add(new JScrollPane(itemTable), BorderLayout.CENTER);

        // Cart Text Area Panel
        cartTextArea = new JTextArea();
        cartTextArea.setEditable(false);
        cartTextArea.setFont(new Font("Arial", Font.PLAIN, 14));
        cartTextArea.setBorder(BorderFactory.createTitledBorder("Shopping Cart"));
        JScrollPane cartScrollPane = new JScrollPane(cartTextArea);
        cartScrollPane.setPreferredSize(new Dimension(300, 600));
        add(cartScrollPane, BorderLayout.EAST);

        JPanel cartButtonPanel = new JPanel();
        cartButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        clearButton = new JButton("Clear Cart");
        clearButton.addActionListener(e -> clearCart());
        cartButtonPanel.add(clearButton);

        checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> checkout());
        cartButtonPanel.add(checkoutButton);

        switchButton = new JButton("Go to Orders");
        switchButton.addActionListener(e -> switchOrder());
        cartButtonPanel.add(switchButton);
        // Add the cart button panel to the East side under the cart display
        add(cartButtonPanel, BorderLayout.SOUTH);

        displayAllItems();
        if(customer!=null) {
            updateCartDisplay();
        }
    }

    private void switchOrder(){
        this.setVisible(false);
        pass.refreshOrders();
        pass.setVisible(true);
    }
    private void displayAllItems() {
        tableModel.setRowCount(0);
        menu.values().stream()
                .sorted(Comparator.comparing(MenuItem::getName))
                .filter(item -> !item.availability.equalsIgnoreCase("Removed"))
                .forEach(item -> tableModel.addRow(new Object[]{item.getItemID(), item.getName(), item.getPrice(), item.getCategory(), item.getAvailability()}));
    }

    private void searchItemsByKeyword(String keyword) {
        String lowerCaseKeyword = keyword.toLowerCase();
        tableModel.setRowCount(0);
        menu.values().stream()
                .filter(item -> (item.getName().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getDescription().toLowerCase().contains(lowerCaseKeyword) ||
                        item.getCategory().toLowerCase().contains(lowerCaseKeyword)) &&
                        !item.availability.equalsIgnoreCase("Removed"))
                .sorted(Comparator.comparing(MenuItem::getName))
                .forEach(item -> tableModel.addRow(new Object[]{item.getItemID(), item.getName(), item.getPrice(), item.getCategory(), item.getAvailability()}));
    }

    private void searchItemsByCategory(String category) {
        tableModel.setRowCount(0);
        menu.values().stream()
                .filter(item -> item.getCategory().equalsIgnoreCase(category) && !item.availability.equalsIgnoreCase("Removed"))
                .sorted(Comparator.comparing(MenuItem::getName))
                .forEach(item -> tableModel.addRow(new Object[]{item.getItemID(), item.getName(), item.getPrice(), item.getCategory(), item.getAvailability()}));
    }

    private void sortItemsByPrice(boolean ascending) {
        tableModel.setRowCount(0);
        List<MenuItem> items = new ArrayList<>(menu.values());
        items.sort(Comparator.comparing(MenuItem::getPrice));
        if (!ascending) {
            Collections.reverse(items);
        }
        items.stream()
                .filter(item -> !item.availability.equalsIgnoreCase("Removed"))
                .forEach(item -> tableModel.addRow(new Object[]{item.getItemID(), item.getName(), item.getPrice(), item.getCategory(), item.getAvailability()}));
    }

    private void showItemDetails(MenuItem item) {
        JDialog dialog = new JDialog(this, "Item Details", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        detailsPanel.add(new JLabel("Item ID: " + item.getItemID()));
        detailsPanel.add(new JLabel("Name: " + item.getName()));
        detailsPanel.add(new JLabel("Price: " + item.getPrice()));
        detailsPanel.add(new JLabel("Category: " + item.getCategory()));
        detailsPanel.add(new JLabel("Description: " + item.getDescription()));
        detailsPanel.add(new JLabel("Availability: " + item.getAvailability()));

        dialog.add(detailsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        JButton reviewsButton = new JButton("View Reviews");
        reviewsButton.addActionListener(e -> viewReviews(item));
        JButton addToCartButton = new JButton("Add to Cart");
        addToCartButton.addActionListener(e -> addToCart(item.getItemID()));
        JButton removeFromCartButton = new JButton("Remove");
        removeFromCartButton.addActionListener(e -> removeFromCart(item.getItemID()));
        if (customer != null) {
            buttonPanel.add(removeFromCartButton);
            buttonPanel.add(addToCartButton);
        }
        buttonPanel.add(reviewsButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private void removeFromCart(String itemID) {
        if (customer.cart.remove(itemID) != null) {
            JOptionPane.showMessageDialog(null, "Item removed from cart", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Item not in cart", "Error", JOptionPane.ERROR_MESSAGE);
        }
        updateCartDisplay();
    }

    private void viewReviews(MenuItem item) {
        StringBuilder reviewsText = new StringBuilder("Reviews:\n");
        for (Review review : item.reviews) {
            reviewsText.append(review.toString()).append("\n");
        }
        JOptionPane.showMessageDialog(this, reviewsText.toString(), "Reviews for " + item.getName(), JOptionPane.INFORMATION_MESSAGE);
    }

    public void addToCart(String itemID) {
        if (customer != null) {
            MenuItem item = menu.get(itemID);
            if (item == null) {
                JOptionPane.showMessageDialog(null, "Item does not exist", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (item.getAvailability().equalsIgnoreCase("Unavailable")) {
                JOptionPane.showMessageDialog(null, "Item is Unavailable", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (customer.cart.containsKey(itemID)) {
                if (itemID.equalsIgnoreCase("VIP001")) {
                    JOptionPane.showMessageDialog(null, "Only one of the VIP pass is required", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                customer.cart.put(itemID, customer.cart.get(itemID) + 1);
                updateCartDisplay();
                return;
            }
            customer.cart.put(itemID, 1);
            updateCartDisplay();
        } else {
            JOptionPane.showMessageDialog(null, "Admin has no cart", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to update the customer.cart display
    public void updateCartDisplay() {
        StringBuilder cartContents = new StringBuilder("---- YOUR CART ----\n");
        if (customer.cart.isEmpty()) {
            cartContents.append("Your cart is empty.\n");
        } else {
            double total = 0.0;
            for (Map.Entry<String, Integer> entry : customer.cart.entrySet()) {
                String itemID = entry.getKey();
                int quantity = entry.getValue();
                MenuItem item = menu.get(itemID);
                double itemTotal = item.getPrice() * quantity;
                total += itemTotal;

                cartContents.append(item.getItemID())
                        .append(" | ")
                        .append(item.getName())
                        .append("........")
                        .append(quantity)
                        .append(" x ")
                        .append(item.getPrice())
                        .append(" = ")
                        .append(String.format("₹%.2f", itemTotal))
                        .append("\n");
            }
            cartContents.append("\nTOTAL: ")
                    .append(String.format("₹%.2f", total))
                    .append("\n");
        }
        cartTextArea.setText(cartContents.toString());
    }

    // Modify the clearCart and checkout methods
    private void clearCart() {
        if (customer != null) {
            customer.cart.clear();
            updateCartDisplay();
            JOptionPane.showMessageDialog(this, "Cart cleared!", "Cart", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void checkout() {
        if (customer != null && !customer.cart.isEmpty()) {
            if (customer.ongoingOrder != null) {
                if (customer.ongoingOrder.status.equalsIgnoreCase("Completed")) {
                    customer.ongoingOrder = null;
                } else {
                    JOptionPane.showMessageDialog(this, "Please wait for the ongoing order to complete before placing another order!", "Checkout", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            double totalAmount = 0.0;
            StringBuilder receipt = new StringBuilder("---- RECEIPT ----\n");

            for (Map.Entry<String, Integer> entry : customer.cart.entrySet()) {
                String itemID = entry.getKey();
                int quantity = entry.getValue();
                MenuItem item = menu.get(itemID);
                double itemTotal = item.getPrice() * quantity;
                totalAmount += itemTotal;

                receipt.append(item.getItemID())
                        .append(" | ")
                        .append(item.getName())
                        .append("........")
                        .append(quantity)
                        .append(" x ")
                        .append(item.getPrice())
                        .append(" = ")
                        .append(String.format("₹%.2f", itemTotal))
                        .append("\n");
            }

            receipt.append("\nTOTAL: ")
                    .append(String.format("₹%.2f", totalAmount))
                    .append("\n");

            // Display receipt and confirm purchase
            int response = JOptionPane.showConfirmDialog(this, receipt.toString() + "\nProceed to checkout?", "Checkout", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                JOptionPane.showMessageDialog(this, "Thank you for your purchase!", "Checkout", JOptionPane.INFORMATION_MESSAGE);
                if (customer.cart.containsKey("VIP001")) { // if you bought the VIP in this order, it will be implemented right away
                    Memory.getInstance().users.get(customer.custID).isVIP = true;
                    if (customer.cart.size() == 1) {
                        Order order = new Order(customer.cart, "Completed", "None", customer.custID);
                        customer.ongoingOrder = null;
                        customer.history.add(order.orderID);
                        Admin.getInstance().sortedOrders.put(order.orderID, order);
                        customer.cart = new HashMap<>();
                        updateCartDisplay();
                        return;
                    }
                }
                Order order = new Order(customer.cart, "Pending", "GUI Order", customer.custID);
                customer.ongoingOrder = order;
                customer.pendingOrders.put(order.orderID, order);
                customer.cart = new HashMap<>();
                updateCartDisplay();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Your cart is empty. Please add items before checking out.", "Checkout", JOptionPane.WARNING_MESSAGE);
        }
    }

    private class SearchActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String selection = (String) viewComboBox.getSelectedItem();
            String input = searchField.getText().trim();
            if ("View Items".equals(selection)) {
                if (!input.isEmpty()) {
                    searchItemsByKeyword(input);
                } else {
                    displayAllItems();
                }
            } else if ("Search by Category".equals(selection)) {
                if (!input.isEmpty()) {
                    searchItemsByCategory(input);
                }
            } else if ("Sort by Price".equals(selection)) {
                boolean ascending = "Increasing".equals(sortComboBox.getSelectedItem());
                sortItemsByPrice(ascending);
            }
        }
    }

}

