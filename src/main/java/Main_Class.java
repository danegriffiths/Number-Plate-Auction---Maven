import java.sql.*;
import java.util.Scanner;

public class Main_Class {

    /**
     * Main_Class constructor, which launches a menu of options for user interaction.
     * @throws SQLException if connection to PostgreSQL database unobtainable.
     */
    private Main_Class() throws SQLException {

        Connection conn = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Number_Plate_Auction_Database", "dane", "password");
            System.out.println("Opened database successfully");

        } catch (Exception ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            System.exit(0);
        }

        Scanner scan = new Scanner(System.in);
        Menu menuItem = new Menu("Number Plate Auction.", scan);
        menuItem.addOption("Display all the registrations available.");
        menuItem.addOption("Display filtered registrations available.");
        menuItem.addOption("Add a registration and guide price.");
        menuItem.addOption("Delete a registration.");
        menuItem.addOption("Quit the auction database");

        boolean done;
        do {
            done = false;
            int menuSelection = menuItem.executeMenu();
            System.out.println();

            switch (menuSelection) {
                case 1:
                    showAllPlatesWithPrice(conn);
                    break;
                case 2:
                    showFilteredPlatesWithPrice(conn);
                    break;
                case 3:
                    addNumberPlateToAuction(conn);
                    break;
                case 4:
                    deleteNumberPlateFromAuction(conn);
                    break;
                case 5:
                    System.out.println("Thank you for using the number plate auction database.");
                    done = true;
                    break;
            }

            // Print blank line.
            System.out.println();
        } while (!done);
        conn.close();
    }

    /**
     * Method to display a list of all the registrations in the database, along with their date/time of auction.
     * @param conn the connection to the PostgreSQL database.
     * @throws SQLException if connection to PostgreSQL database unobtainable.
     */
    private void showAllPlatesWithPrice(Connection conn) throws SQLException {

        Statement sqlStmnt = conn.createStatement();
        String sqlText = "SELECT " +
                "registration AS \"Reg Number\", guide_price AS \"Guide Price\", date_time AS \"Time\" " +
                "FROM " +
                "number_plates " +
                "LEFT JOIN " +
                "categories ON number_plates.category_id = categories.category_id " +
                "LEFT JOIN " +
                "date_time ON categories.date_time_id = date_time.date_time_id" +
                " ORDER BY" +
                " number_plates.category_id ASC;";

        ResultSet sqlResult = sqlStmnt.executeQuery(sqlText);
        ResultSetMetaData resultMtData = sqlResult.getMetaData();

        // To print the column headers in an easy to read display.
        System.out.printf("%s\t\t\t %s\t\t\t %s\n",resultMtData.getColumnName(1), resultMtData.getColumnName(2),
                resultMtData.getColumnName(3));

        while (sqlResult.next()) {
            for (int i = 1; i <= 3; i++) {
                String columnValue = sqlResult.getString(i);
                System.out.printf("%s\t\t\t ",columnValue);
            }
            System.out.println("");
        }
        sqlStmnt.close();
    }

    /**
     * Method to display a list of registrations based on the user's category selection.
     * @param conn the connection to the PostgreSQL database.
     * @throws SQLException if connection to PostgreSQL database unobtainable.
     */
    private void showFilteredPlatesWithPrice(Connection conn) throws SQLException {

        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter the category identifier:");
        String category = scan.nextLine();

        PreparedStatement sqlText = conn.prepareCall("SELECT " +
                "registration AS \"Reg Number\", guide_price AS \"Guide Price\", date_time AS \"Time\" " +
                "FROM " +
                "number_plates " +
                "LEFT JOIN " +
                "categories ON number_plates.category_id = categories.category_id " +
                "LEFT JOIN " +
                "date_time ON categories.date_time_id = date_time.date_time_id " +
                "WHERE number_plates.category = ?" +
                " ORDER BY" +
                " number_plates.category_id ASC;");

        sqlText.setString(1,category);

        ResultSet sqlResult = sqlText.executeQuery();
        ResultSetMetaData resultMtData = sqlResult.getMetaData();

        System.out.printf("%s\t\t\t %s\t\t\t %s\n",resultMtData.getColumnName(1), resultMtData.getColumnName(2),
                resultMtData.getColumnName(3));

        while (sqlResult.next()) {
            for (int i = 1; i <= 3; i++) {
                String columnValue = sqlResult.getString(i);
                System.out.printf("%s\t\t\t ",columnValue);
            }
            System.out.println("");
        }
    }

    /**
     * Method to add a number plate and guide price to the PostgreSQL database.
     * @param conn the connection to the PostgreSQL database.
     * @throws SQLException if connection to PostgreSQL database unobtainable.
     */
    private void addNumberPlateToAuction(Connection conn) throws SQLException {

        Scanner scan = new Scanner(System.in);

        System.out.println("Please enter the number plate category e.g. A for A12 XYZ.");
        String category = scan.nextLine();
        System.out.println("Please enter the number plate.");
        String registration = scan.nextLine();
        System.out.println("Please enter the guide price.");
        int price = scan.nextInt();

        PreparedStatement sqlText = conn.prepareCall("INSERT INTO number_plates " +
                "(category, registration, guide_price) " +
                "VALUES " +
                "(?,?,?);" +
                "UPDATE number_plates " +
                "SET category_id = categories.category_id " +
                "FROM categories " +
                "WHERE " +
                "number_plates.category = categories.category;");

        sqlText.setString(1,category);
        sqlText.setString(2,registration);
        sqlText.setInt(3,price);

        try {
            sqlText.execute();
            System.out.println("Registration successfully added to the auction.");
        } catch (SQLException ex) {
            System.out.println("Number plate already in database.");
        }
    }

    /**
     * Method to delete a number plate specified by the user from the PostgreSQL database.
     * @param conn the connection to the PostgreSQL database.
     * @throws SQLException if connection to PostgreSQL database unobtainable, or if number plate is not found in database.
     */
    private void deleteNumberPlateFromAuction(Connection conn) throws SQLException {

        Scanner scan = new Scanner(System.in);

        String selectText = "SELECT registration FROM number_plates WHERE registration = ?;";
        String deleteText = "DELETE FROM number_plates WHERE registration = ?;";

        System.out.println("Please enter the number plate to be deleted");
        String plateToBeDeleted = scan.nextLine();

        try {
            conn.setAutoCommit(false);

            PreparedStatement sqlSelectText = conn.prepareStatement(selectText);
            sqlSelectText.setString(1,plateToBeDeleted);

            ResultSet sqlResult = sqlSelectText.executeQuery();

            if(sqlResult.isBeforeFirst()){
                PreparedStatement sqlDeleteText = conn.prepareStatement(deleteText);
                sqlDeleteText.setString(1,plateToBeDeleted);
                sqlDeleteText.executeUpdate();
                conn.commit();
                System.out.println("Number plate deleted.");
            } else {
                throw new SQLException("Number plate doesn't exist");
            }

        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
            conn.rollback();
        }
    }

    /**
     * Main method to test the Main_Class constructor.
     * @param args to capture terminal arguments.
     */
    public static void main(String[] args){
        try {
            new Main_Class();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}