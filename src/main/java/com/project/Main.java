package com.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws SQLException, IOException {
        String basePath = System.getProperty("user.dir") + "/data/";
        String filePath = basePath + "forhonor.db";

        // Verify if the database exists, create it if not
        File fDatabase = new File(filePath);
        if (!fDatabase.exists()) {
            initDatabase(filePath);
        }

        // Connect to the database
        Connection conn = UtilsSQLite.connect(filePath);

        // Main menu loop
        boolean exit = false;

        while (!exit) {
            Scanner scanner = new Scanner(System.in);

            // Display menu
            System.out.println("----- For Honor Database Menu -----");
            System.out.println("1. Show a table");
            System.out.println("2. Show characters per faction");
            System.out.println("3. Show the best attack character of a faction");
            System.out.println("4. Show the best defense character of a faction");
            System.out.println("5. Exit");

            // Get user choice
            try {
                int choice = scanner.nextInt();

                switch (choice) {
                    case 1:
                        showTableMenu(conn);
                        break;
                    case 2:
                        showCharactersPerFaction(conn);
                        break;
                    case 3:
                        showBestAttackCharacter(conn);
                        break;
                    case 4:
                        showBestDefenseCharacter(conn);
                        break;
                    case 5:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 5.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
            } finally {
                // Close the Scanner after each iteration
                scanner.close();
            }
        }

        // Disconnect from the database
        UtilsSQLite.disconnect(conn);
    }

    static void initDatabase(String filePath) {
        // Connect to the database
        Connection conn = UtilsSQLite.connect(filePath);
    
        // Create "faction" table
        UtilsSQLite.queryUpdate(conn, "CREATE TABLE IF NOT EXISTS faction ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name VARCHAR(15) NOT NULL,"
                + " resume VARCHAR(500) NOT NULL);");
    
        // Drop existing "character" table if it exists
        UtilsSQLite.queryUpdate(conn, "DROP TABLE IF EXISTS character;");
        System.err.println("test");
        // Create "character" table with foreign key reference to "faction" table
        UtilsSQLite.queryUpdate(conn, "CREATE TABLE IF NOT EXISTS character ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name VARCHAR(15) NOT NULL,"
                + " attack REAL NOT NULL,"
                + " defense REAL NOT NULL,"
                + " idFaction INTEGER NOT NULL,"
                + " FOREIGN KEY (idFaction) REFERENCES faction(id));");
    
        // Add information about real factions and characters from For Honor game
        populateDatabase(conn);
    
        // Disconnect from the database
        UtilsSQLite.disconnect(conn);
    }
    

    static void populateDatabase(Connection conn) {
        // Add information about real factions and characters from For Honor game
        UtilsSQLite.queryUpdate(conn, "INSERT INTO faction (name, resume) VALUES ('Knights', 'Honorable warriors with a code of chivalry.');");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO faction (name, resume) VALUES ('Vikings', 'Fierce warriors with a strong connection to nature.');");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO faction (name, resume) VALUES ('Samurais', 'Disciplined warriors with a focus on martial arts.');");
    
        // Knights
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Warden', 80.0, 60.0, 1);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Conqueror', 75.0, 70.0, 1);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Lawbringer', 85.0, 65.0, 1);");
    
        // Vikings
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Raider', 90.0, 50.0, 2);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Warlord', 70.0, 75.0, 2);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Berserker', 95.0, 45.0, 2);");
    
        // Samurais
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Kensei', 75.0, 70.0, 3);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Shugoki', 90.0, 60.0, 3);");
        UtilsSQLite.queryUpdate(conn, "INSERT INTO character (name, attack, defense, idFaction) VALUES ('Orochi', 80.0, 55.0, 3);");
    }

    // Helper method to display information about a table
    static void displayTableInfo(Connection conn, String tableName) throws SQLException {
        ResultSet rs = UtilsSQLite.querySelect(conn, "PRAGMA table_info(" + tableName + ");");
        System.out.println("Table Info for " + tableName + ":");
        while (rs.next()) {
            System.out.println("Column Name: " + rs.getString("name") +
                    ", Type: " + rs.getString("type") +
                    ", Nullable: " + rs.getString("notnull"));
        }
        System.out.println();
    }

    // Helper method to display content of a table
    static void displayTableContent(Connection conn, String tableName) throws SQLException {
        ResultSet rs = UtilsSQLite.querySelect(conn, "SELECT * FROM " + tableName + ";");
        System.out.println("Content of " + tableName + ":");

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        while (rs.next()) {
            System.out.print("    ");
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rsmd.getColumnName(i) + ": " + rs.getString(i) + ", ");
            }
            System.out.println();
        }
        System.out.println();
    }

    static void showTableMenu(Connection conn) throws SQLException {
    Scanner scanner = new Scanner(System.in);

    // Display menu for selecting a table
    System.out.println("Select a table to show:");
    System.out.println("1. Faction");
    System.out.println("2. Character");
    System.out.print("Enter your choice (1-2): ");

    try {
        int tableChoice = scanner.nextInt();

        // Consume the newline character left in the buffer
        scanner.nextLine();

        switch (tableChoice) {
            case 1:
                displayTableContent(conn, "faction");
                break;
            case 2:
                displayTableContent(conn, "character");
                break;
            default:
                System.out.println("Invalid choice. Please enter 1 or 2.");
        }
    } catch (InputMismatchException e) {
        System.out.println("Invalid input. Please enter a number.");
        scanner.nextLine(); // Consume the invalid input
    }

    scanner.close();
}

    static void showCharactersPerFaction(Connection conn) throws SQLException {
        Scanner scanner = new Scanner(System.in);

        // Display available factions
        ArrayList<String> factions = UtilsSQLite.listTableColumnValues(conn, "faction", "name");
        System.out.println("Available Factions: " + factions);

        // Get user input for faction name
        System.out.print("Enter the name of the faction: ");
        String factionName = scanner.nextLine();

        // Display characters for the selected faction
        String query = "SELECT * FROM character WHERE idFaction = (SELECT id FROM faction WHERE name = ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, factionName);
            ResultSet rs = pstmt.executeQuery();

            displayTableContent(conn, "character");
        }

        scanner.close();
    }

    static void showBestAttackCharacter(Connection conn) throws SQLException {
        Scanner scanner = new Scanner(System.in);
    
        // Display available factions
        ArrayList<String> factions = UtilsSQLite.listTableColumnValues(conn, "faction", "name");
        System.out.println("Available Factions: " + factions);
    
        // Get user input for faction name
        System.out.print("Enter the name of the faction: ");
        String factionName = scanner.nextLine();
    
        // Query to find the character with the highest attack in the specified faction
        String query = "SELECT * FROM character WHERE idFaction = (SELECT id FROM faction WHERE name = ?) ORDER BY attack DESC LIMIT 1;";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, factionName);
            ResultSet rs = pstmt.executeQuery();
    
            displayTableContent(conn, "character");
        }
    
        scanner.close();
    }

    static void showBestDefenseCharacter(Connection conn) throws SQLException {
        Scanner scanner = new Scanner(System.in);
    
        // Display available factions
        ArrayList<String> factions = UtilsSQLite.listTableColumnValues(conn, "faction", "name");
        System.out.println("Available Factions: " + factions);
    
        // Get user input for faction name
        System.out.print("Enter the name of the faction: ");
        String factionName = scanner.nextLine();
    
        // Query to find the character with the highest defense in the specified faction
        String query = "SELECT * FROM character WHERE idFaction = (SELECT id FROM faction WHERE name = ?) ORDER BY defense DESC LIMIT 1;";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, factionName);
            ResultSet rs = pstmt.executeQuery();
    
            displayTableContent(conn, "character");
        }
    
        scanner.close();
    }

}
