
import java.sql.*;
import java.util.Scanner;

public class ProductManagement {

 
static final String URL =
        "jdbc:oracle:thin:@10.9.45.205:1521/XEPDB1";

static final String USER = "SYSTEM";
static final String PASSWORD = "admin123";


    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            Class.forName("oracle.jdbc.OracleDriver");

            try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {

                System.out.println("Database connected successfully!");
                System.out.println("Database URL: " + con.getMetaData().getURL());
                System.out.println("Connected user: " + con.getSchema());

                // Display database and container names
                try (Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery(
                                "SELECT SYS_CONTEXT('USERENV','DB_NAME'), " +
                                        "SYS_CONTEXT('USERENV','CON_NAME') FROM DUAL")) {

                    if (rs.next()) {
                        System.out.println("Database name: " + rs.getString(1));
                        System.out.println("Container name: " + rs.getString(2));
                    }
                }

                // Check whether PRODUCTS is accessible
                try (Statement st = con.createStatement();
                        ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM PRODUCTS")) {

                    if (rs.next()) {
                        System.out.println("PRODUCTS table is accessible.");
                        System.out.println("Existing product count: " + rs.getInt(1));
                    }
                }

                while (true) {
                    System.out.println("\n--- PRODUCT MANAGEMENT ---");
                    System.out.println("1. Insert Product");
                    System.out.println("2. Search Product");
                    System.out.println("3. Update Product");
                    System.out.println("4. Delete Product");
                    System.out.println("5. Exit");
                    System.out.print("Enter choice: ");

                    int choice;

                    try {
                        choice = Integer.parseInt(sc.nextLine().trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Enter a number from 1 to 5.");
                        continue;
                    }

                    try {
                        switch (choice) {
                            case 1:
                                insert(con);
                                break;
                            case 2:
                                search(con);
                                break;
                            case 3:
                                update(con);
                                break;
                            case 4:
                                delete(con);
                                break;
                            case 5:
                                System.out.println("Exiting application...");
                                return;
                            default:
                                System.out.println("Invalid choice!");
                        }
                    } catch (SQLException e) {
                        System.out.println("Database operation failed!");
                        System.out.println("Error code: " + e.getErrorCode());
                        System.out.println("Message: " + e.getMessage());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Input error: " + e.getMessage());
                    }
                }
            }

        } catch (ClassNotFoundException e) {
            System.out.println("Oracle JDBC driver not found!");
            System.out.println("Check ojdbc17.jar in the classpath.");
        } catch (SQLException e) {
            System.out.println("Database connection or table check failed!");
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println("Message: " + e.getMessage());
        }
    }

    static int readInt(String message) {
        System.out.print(message);

        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid whole number.");
        }
    }

    static double readDouble(String message) {
        System.out.print(message);

        try {
            double value = Double.parseDouble(sc.nextLine().trim());

            if (!Double.isFinite(value)) {
                throw new IllegalArgumentException("Enter a valid price.");
            }

            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Enter a valid price.");
        }
    }

    // INSERT PRODUCT
    static void insert(Connection con) throws SQLException {
        int id = readInt("Enter product ID: ");

        System.out.print("Enter product name: ");
        String name = sc.nextLine().trim();

        double price = readDouble("Enter price: ");
        int quantity = readInt("Enter quantity: ");

        if (id <= 0 || name.isEmpty() || price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid product details.");
        }

        String sql = "INSERT INTO PRODUCTS (ID, NAME, PRICE, QUANTITY) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setDouble(3, price);
            ps.setInt(4, quantity);

            if (ps.executeUpdate() > 0) {
                System.out.println("Product inserted successfully!");
            }
        }
    }

    // SEARCH PRODUCT
    static void search(Connection con) throws SQLException {
        int id = readInt("Enter product ID to search: ");

        String sql = "SELECT ID, NAME, PRICE, QUANTITY FROM PRODUCTS WHERE ID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\n--- PRODUCT DETAILS ---");
                    System.out.println("ID: " + rs.getInt("ID"));
                    System.out.println("Name: " + rs.getString("NAME"));
                    System.out.println("Price: " + rs.getDouble("PRICE"));
                    System.out.println("Quantity: " + rs.getInt("QUANTITY"));
                } else {
                    System.out.println("Product not found!");
                }
            }
        }
    }

    // UPDATE PRODUCT
    static void update(Connection con) throws SQLException {
        int id = readInt("Enter product ID to update: ");

        System.out.print("Enter new product name: ");
        String name = sc.nextLine().trim();

        double price = readDouble("Enter new price: ");
        int quantity = readInt("Enter new quantity: ");

        if (name.isEmpty() || price < 0 || quantity < 0) {
            throw new IllegalArgumentException("Invalid product details.");
        }

        String sql = "UPDATE PRODUCTS SET NAME = ?, PRICE = ?, QUANTITY = ? " +
                "WHERE ID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, quantity);
            ps.setInt(4, id);

            if (ps.executeUpdate() > 0) {
                System.out.println("Product updated successfully!");
            } else {
                System.out.println("Product not found!");
            }
        }
    }

    // DELETE PRODUCT
    static void delete(Connection con) throws SQLException {
        int id = readInt("Enter product ID to delete: ");

        String sql = "DELETE FROM PRODUCTS WHERE ID = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);

            if (ps.executeUpdate() > 0) {
                System.out.println("Product deleted successfully!");
            } else {
                System.out.println("Product not found!");
            }
        }
    }
}
