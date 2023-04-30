package se2203b.assignments.ifinance;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GroupAdapter {
    private static Connection conn;
    Connection connection;
    AccountCategoryAdapter c = null;

    public GroupAdapter(Connection conn, Boolean reset) throws SQLException {
        connection = conn;
        c = new AccountCategoryAdapter(connection, false);

        Statement stmt = connection.createStatement();

        if (reset) {
            try {
                // Remove tables if database tables have been created.
                // This will throw an exception if the tables do not exist
                stmt.execute("DROP TABLE AccountGroup");
            } catch (SQLException ex) {
                // No need to report an error.
                // The table simply did not exist.
                System.out.println(ex.getMessage());
            }
        }
        try {
            // Create the table
            //         System.out.println("before table creation");
            stmt.execute("CREATE TABLE AccountGroup ("
                    + "id INT NOT NULL PRIMARY KEY,"
                    + "groupName VARCHAR(60),"
                    + "groupParent INT REFERENCES AccountGroup(id),"
                    + "groupElement VARCHAR(60) REFERENCES AccountCategory(accountName)"
                    + ")");
            addGroup();

        } catch (SQLException ex) {
            // No need to report an error.
            // The table exists and may have some data.
        }


    }

    private void addGroup() throws SQLException {
        Group group = new Group();
        AccountCategory assets = new AccountCategory("Assets", "Debit");
        AccountCategory liabilities = new AccountCategory("Liabilities", "Debit");
        AccountCategory income = new AccountCategory("Income", "Credit");
        AccountCategory expenses = new AccountCategory("Expenses", "Debit");
        Group fixedAssets = new Group(1, "Fixed assets", group, assets);
        Group investments = new Group(2, "Investments", group, assets);
        Group branch = new Group(3, "Branch/divisions", group, assets);
        Group cash = new Group(4, "Cash in hand", group, assets);
        Group bankAccounts = new Group(5, "Bank accounts", group, assets);
        Group depositA = new Group(6, "Deposits (assets)", group, assets);
        Group advanceA = new Group(7, "Advances (assets)", group, assets);
        Group capitalAccount = new Group(8, "Capital account", group, liabilities);
        Group longTermLoans = new Group(9, "Long term loans", group, liabilities);
        Group currentLiabilities = new Group(10, "Current liabilities", group, liabilities);
        Group reserve = new Group(11, "Reserves and surplus", group, liabilities);
        Group salesAccount = new Group(12, "Sales account", group, income);
        Group purchaseAccount = new Group(13, "Purchase account", group, expenses);
        Group directExp = new Group(14, "Expenses (direct)", group, expenses);
        Group inDirectExp = new Group(15, "Expenses (indirect)", group, expenses);
        Group secureLoans = new Group(16, "Secured loans", longTermLoans, liabilities);
        Group unsecureLoans = new Group(17, "Unsecured loans", longTermLoans, liabilities);
        Group duties = new Group(18, "Duties taxes payable", currentLiabilities, liabilities);
        Group provisions = new Group(19, "Provisions", currentLiabilities, liabilities);
        Group sundry = new Group(20, "Sundry creditors", currentLiabilities, liabilities);
        Group bank = new Group(21, "Bank od & limits", currentLiabilities, liabilities);

        insertRecord(group);
        insertRecord(fixedAssets);
        insertRecord(investments);
        insertRecord(branch);
        insertRecord(cash);
        insertRecord(bankAccounts);
        insertRecord(depositA);
        insertRecord(advanceA);
        insertRecord(capitalAccount);
        insertRecord(longTermLoans);
        insertRecord(currentLiabilities);
        insertRecord(reserve);
        insertRecord(salesAccount);
        insertRecord(purchaseAccount);
        insertRecord(directExp);
        insertRecord(inDirectExp);
        insertRecord(secureLoans);
        insertRecord(unsecureLoans);
        insertRecord(duties);
        insertRecord(provisions);
        insertRecord(sundry);
        insertRecord(bank);
    }


    public void insertRecord(Group data) throws SQLException {
        Statement stmt = connection.createStatement();


        if (data.getParent() != null || data.getElement() != null) {
            int groupID = getMax() + 1;
            stmt.executeUpdate("INSERT INTO AccountGroup (id,groupName,groupParent,groupElement) "
                    + "VALUES ("
                    + groupID + ", '"
                    + data.getName() + "', "
                    + data.getParent().getID() + ",'"
                    + data.getElement().getName() + "')"
            );
            data.setID(groupID);

        } else {
            stmt.executeUpdate("INSERT INTO AccountGroup (id,groupName,groupParent,groupElement) "
                    + "VALUES ("
                    + 0 + ", '"
                    + data.getName() + "', "
                    + null + ","
                    + null + ")"
            );
        }
        System.out.println("INSERTED: " + data.getName());
    }

    public void addNewGroup() throws SQLException {
        Statement stmt = connection.createStatement();
        int groupID = getMax() + 1;

    }

    public int getMax() throws SQLException {
        int groupID = 0;
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT MAX(id) FROM AccountGroup");
        if (rs.next()) groupID = rs.getInt(1);
        return groupID;
    }

    public void updateRecord(Group data) throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("UPDATE AccountGroup "
                + "SET id = " + data.getID() + ", "
                + "groupName = '" + data.getName() + "', "
                + "groupParent = " + data.getParent().getID() + ", "
                + "groupElement = '" + data.getElement().getName() + "' "
                + "WHERE id = " + data.getID());
    }

    public int getID(String name) throws SQLException {
        ResultSet rs;
        System.out.println("name sent to get ID: " + name);
        // Create a Statement object
        Statement stmt = connection.createStatement();

        // Create a string with a SELECT statement
        String sqlStatement = "SELECT id FROM AccountGroup WHERE groupName = '" + name + "'";

        // Execute the statement and get the result set
        rs = stmt.executeQuery(sqlStatement);


        int id = 0;
        if (rs.next()) {
            id = rs.getInt("id");
        }
        System.out.println("ID FOUND USING GETID: " + id);
        return id; // return the ID value
    }

    public ObservableList<Group> getGroupList() throws SQLException {
        ObservableList<Group> groupsList = FXCollections.observableArrayList();
        AccountCategory accountCategory = null;

        // Create a Statement object
        ResultSet rs;
        Statement stmt = connection.createStatement();

        // Create a string with a SELECT statement
        String sqlStatement = "SELECT * FROM AccountGroup ";


        // Execute the query by sending the SQL statement to the DBMS
        rs = stmt.executeQuery(sqlStatement);
        Group parent;
        int pID;
        int counter = 0;

        while (rs.next()) {

            Group group = new Group();
            group.setID(rs.getInt(1));
            //            System.out.println(id);
            group.setName(rs.getString(2));
            if (rs.getInt(3) != 0) {
                group.setParent(findRecord(rs.getInt(3)));
            } else {
                group.setParent(new Group());
            }
            String category = rs.getString(4);
            accountCategory = c.findRecord(category);
            group.setElement(accountCategory);
            groupsList.add(group);
        }
        return groupsList;
    }

    public ObservableList<Group> getCategorySubRoots(String name) throws SQLException {
        ObservableList<Group> categorySubRoots = FXCollections.observableArrayList();

        AccountCategory accountCategory = null;
        // Create a Statement object
        ResultSet rs;
        Statement stmt = connection.createStatement();

        // Create a string with a SELECT statement
        String sqlStatement = "SELECT * FROM AccountGroup ";

        // Execute the query by sending the SQL statement to the DBMS
        rs = stmt.executeQuery(sqlStatement);
        Group parent;
        int pID;
        int counter = 0;
        while (rs.next()) {
            System.out.println(rs.getString(4));
            String element = rs.getString(4);
            if (element.equals(name)) {
                Group group = new Group();
                group.setID(rs.getInt(1));
                group.setName(rs.getString(2));
                group.setParent(findRecord(rs.getInt(3)));
                String category = rs.getString(4);
                accountCategory = c.findRecord(category);
                group.setElement(accountCategory);
                categorySubRoots.add(group);
                counter++;
            }
        }
        return categorySubRoots;
    }

    public ObservableList<Group> getSubRoots(int id) throws SQLException {
        ObservableList<Group> subRootsList = FXCollections.observableArrayList();
        AccountCategory accountCategory = null;
        // Create a Statement object
        ResultSet rs;
        Statement stmt = connection.createStatement();

        // Create a string with a SELECT statement
        String sqlStatement = "SELECT * FROM AccountGroup ";

        // Execute the query by sending the SQL statement to the DBMS
        rs = stmt.executeQuery(sqlStatement);
        Group parent;
        int pID;
        int counter = 0;
        while (rs.next()) {
            if (rs.getInt(3) == id) {
                Group group = new Group();
                group.setID(rs.getInt(1));
                group.setName(rs.getString(2));
                group.setParent(findRecord(rs.getInt(3)));
                String category = rs.getString(4);
                accountCategory = c.findRecord(category);
                group.setElement(accountCategory);
                subRootsList.add(group);
                counter++;
            }
        }
        return subRootsList;
    }

    public Group findRecord(int id) throws SQLException {

        AccountCategory accountCategory = null;
        Group group = new Group();

        ResultSet rs;
        // Create a Statement object
        Statement stmt = connection.createStatement();

        // Create a string with a SELECT statement
        String sqlStatement = "SELECT * FROM AccountGroup WHERE id = " + id + "";
        // Execute the statement and return the result

        rs = stmt.executeQuery(sqlStatement);

        if (rs.next()) {
            Group gg = new Group();
            if (rs.getInt(3) != 0) {
                gg = findRecord(rs.getInt(3));
            } else {
                gg = new Group();
            }
            group.setID(rs.getInt(1));
            group.setName(rs.getString(2));
            group.setParent(gg);

            accountCategory = c.findRecord(rs.getString(4));
            group.setElement(accountCategory);
        }
        return group;
    }

    public void deleteRecord(Group data) throws SQLException {
        Statement stmt = connection.createStatement();
        System.out.println("DELETE FROM AccountGroup WHERE id = " + data.getID());
        stmt.executeUpdate("DELETE FROM AccountGroup WHERE id = " + data.getID());

    }

}

