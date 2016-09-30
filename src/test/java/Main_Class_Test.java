import org.junit.*;
import java.sql.*;

/**
 * Test class to test the methods contained within Main_Class.
 * Created by dane on 28/09/16.
 */
public class Main_Class_Test {


    private static Connection conn = null;

    @BeforeClass
    public static void setUp() throws Exception {


        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Test_Clone_Of_Number_Plates_DB", "dane", "password");
            System.out.println("Opened database successfully");

        } catch (Exception ex) {
            System.err.println(ex.getClass().getName() + ": " + ex.getMessage());
            System.exit(0);
        }

        Statement sqlStmnt = conn.createStatement();
        String sqlText = "INSERT INTO number_plates " +
                "(category, registration, guide_price) " +
                "VALUES " +
                "('A','AB12 DEF',2000),('B','BD10 EFG',500),('C','CAR 15',5000),('D','DRJ 1',3000),('E','E1 JOB',750),('E','E11 POT',500)," +
                "('F','FOG 11', 1000),('G','GR1 MY',1500),('H','H3 LLO',2000),('J','J4 MMY',1500),('K','K1 NKY',3000),('L','L1 MO',5000);" +
                "UPDATE number_plates " +
                "SET category_id = categories.category_id " +
                "FROM categories " +
                "WHERE " +
                "number_plates.category = categories.category;";

        sqlStmnt.executeUpdate(sqlText);
        System.out.println("Test data inserted into database.");

    }

    @Test
    public void showFilteredPlateWithPrice() throws Exception {

        Statement sqlStmnt = conn.createStatement();
        String sqlText = "SELECT " +
                "registration, guide_price, date_time " +
                "FROM number_plates " +
                "LEFT JOIN categories ON number_plates.category_id = categories.category_id " +
                "LEFT JOIN date_time ON categories.date_time_id = date_time.date_time_id " +
                "WHERE number_plates.category = 'A';";

        ResultSet sqlResult = sqlStmnt.executeQuery(sqlText);

        sqlResult.next();
        Assert.assertEquals("AB12 DEF",sqlResult.getString("registration"));
        Assert.assertEquals("2000",sqlResult.getString("guide_price"));
        Assert.assertEquals("2016-09-23 10:00:00",sqlResult.getString("date_time"));

    }

    @Test
    public void addNumberPlateToAuctionTest() throws SQLException {

        Statement sqlStmnt = conn.createStatement();
        String sqlText = "INSERT INTO number_plates " +
                "(category, registration, guide_price) " +
                "VALUES ('Z','ZA12 ABC',1000);" +
                "UPDATE number_plates " +
                "SET category_id = categories.category_id " +
                "FROM categories WHERE " +
                "number_plates.category = categories.category;";

        int sqlResult = sqlStmnt.executeUpdate(sqlText);
        Assert.assertEquals(1, sqlResult);

    }

    @Test
    public void addDuplicateNumberPlateToAuctionTest() throws SQLException {

        Statement sqlStmnt = conn.createStatement();
        String sqlText = "INSERT INTO number_plates " +
                "(category, registration, guide_price) " +
                "VALUES ('B','BD10 EFG',500);" +
                "UPDATE number_plates " +
                "SET category_id = categories.category_id " +
                "FROM categories WHERE " +
                "number_plates.category = categories.category;";

        try {
            sqlStmnt.executeUpdate(sqlText);

        } catch (SQLException ex) {
            Assert.assertTrue(true);
            return;
        }
        Assert.assertTrue(false);
    }

    @AfterClass
    public static void tearDown() throws Exception {

        Statement sqlStmnt = conn.createStatement();
        String sqlText = "DELETE FROM number_plates " +
                "WHERE registration IS NOT NULL;";

        sqlStmnt.executeUpdate(sqlText);
        System.out.println("Database empty!");

    }
}
