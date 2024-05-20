package project;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

@Slf4j
public class App {

    public static final Logger LOGGER = LoggerFactory.getLogger(App.class);
    private static final Scanner scanner = new Scanner(System.in);
    private final static String DATABASE_URL = "jdbc:mysql://localhost/countryandcontinent";
    private final static String USER = "root";
    private final static String PASSWORD = "";

    public static void main(String[] args) throws SQLException {
        displayMenu();
        scanner.close();
    }

    private static void displayMenu() throws SQLException {
        while (true) {
            System.out.println("\nMain Menu:");
            System.out.println("1. Insert Into Country Continent");
            System.out.println("2. Insert Country");
            System.out.println("3. Insert Continent");
            System.out.println("4. View Countries");
            System.out.println("5. View Continents");
            System.out.println("6. Delete Country");
            System.out.println("7. Delete Continent");
            System.out.println("0. Exit");

            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    insertIntoCountryContinent();
                    break;
                case 2:
                    insertCountry();
                    break;
                case 3:
                    insertContinent();
                    break;
                case 4:
                    getAllCountries();
                    break;
                case 5:
                    getContinents();
                    break;
                case 6:
                    System.out.print("Enter country ID to delete: ");
                    int countryId = scanner.nextInt();
                    deleteCountryById(countryId);
                    break;
                case 7:
                    System.out.print("Enter continent ID to delete: ");
                    int continentId = scanner.nextInt();
                    deleteContinentById(continentId);
                    break;
                case 0:
                    System.out.println("Exiting...");
                    return;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    break;
            }
        }
    }

    private static long insertCountry() throws SQLException {
        System.out.println("Country name: ");
        String country = scanner.nextLine();
        System.out.println("Capital: ");
        String capital = scanner.nextLine();
        String queryCountry = "INSERT INTO country(country_name, country_capital) VALUES (?,?)";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement(queryCountry, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, country);
            ps.setString(2, capital);
            if (!checkIfCountryExist(country)) {
                ps.executeUpdate();
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating country failed, no ID obtained.");
                    }
                }
            } else {
                return getCountryByName(country);

            }
        } catch (SQLException e) {
            LOGGER.error("Error inserting country: {}", country, e);
            throw new SQLException("Error inserting country: " + country, e);

        }
    }

    private static boolean checkIfCountryExist(String country) throws SQLException {
        String query = "SELECT COUNT(*) FROM country WHERE country_name = ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, country);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking if country exists: {}", country, e);
            throw new SQLException("Error checking if country exists: " + country, e);
        }
        return false;
    }

    private static int getCountryByName(String country) throws SQLException {
        String query = "SELECT country_id FROM country WHERE country_name LIKE ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, country + "%");  // Finds all countries starting with the input
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving country by name: {}", country, e);
            throw new SQLException("SQL error while retrieving country by name: " + country, e);
        }
        return 0;
    }

    private static void getAllCountries() throws SQLException {
        Set<Country> countriesSet = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement st = connection.createStatement()) {

            ResultSet resultSet = st.executeQuery("SELECT c.country_id, c.country_name, c.country_capital, cont.continent_id, cont.continent_name, cont.number_of_states \n" +
                    "FROM country c \n" +
                    "LEFT JOIN continent_country cc ON c.country_id = cc.country_id \n" +
                    "LEFT JOIN continent cont ON cc.continent_id = cont.continent_id;");

            while (resultSet.next()) {
                int countryId = resultSet.getInt("country_id");
                String countryName = resultSet.getString("country_name");
                String countryCapital = resultSet.getString("country_capital");

                Country country = new Country(countryId, countryName, countryCapital);

                Integer continentId = resultSet.getObject("continent_id", Integer.class);
                String continentName = resultSet.getString("continent_name");
                Integer numberOfStates = resultSet.getObject("number_of_states", Integer.class); // Use getObject to safely handle null

                if (continentId != null && continentName != null && numberOfStates != null) {
                    Continent continent = new Continent(continentId, continentName, numberOfStates);
                    country.setContinent(continent);
                }
                countriesSet.add(country);
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving countries: ", e);
        }

        for (Country country : countriesSet) {
            System.out.println(country + (country.getContinent() != null ? " in " + country.getContinent().getName() : " with no associated continent"));
        }
    }

    private static void insertIntoCountryContinent() throws SQLException {
        long countryId = insertCountry();
        long continentId = insertContinent();
        String queryInsertContinent = "INSERT INTO continent_country(continent_id,country_id) VALUES (?,?);";

        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
            connection.setAutoCommit(false);
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryInsertContinent)) {
                preparedStatement.setInt(1, (int) continentId);
                preparedStatement.setInt(2, (int) countryId);
                preparedStatement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException exception) {
                    LOGGER.error("Failed to rollback transaction", exception);
                }
            }
            LOGGER.error("Error inserting country and continent association", e);
            throw new SQLException("Error inserting country and continent association", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.error("Failed to close connection", e);
                }
            }
        }
    }

    private static boolean checkIfContinentExist(String continent) throws SQLException {
        String query = "SELECT COUNT(*) FROM continent WHERE continent_name LIKE ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, continent + "%");
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) != 0;
            }
        } catch (SQLException e) {
            LOGGER.error("Error checking if continent exists: {}", continent, e);
            throw new SQLException("SQL error while checking if continent exists: " + continent, e);
        }
        return false;
    }

    private static int getContinentByName(String continent) throws SQLException {
        String query = "SELECT continent_id FROM continent WHERE continent_name LIKE ?";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, continent + "%");
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error retrieving continent by name: {}", continent, e);
            throw new SQLException("SQL error while retrieving continent by name: " + continent, e);
        }
        return 0;
    }

    private static long insertContinent() {
        System.out.println("Continent name: ");
        String continent = scanner.nextLine();
        System.out.println("Number of states: ");
        int states = 0;

        try {
            states = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid number format for states: {}", e.getMessage());
            return 0;
        }

        String queryContinent = "INSERT INTO continent(continent_name,number_of_states) VALUES (?,?)";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement(queryContinent, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, continent);
            ps.setInt(2, states);
            if (!checkIfContinentExist(continent)) {
                ps.executeUpdate();
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    } else {
                        throw new SQLException("Creating continent failed, no ID obtained.");
                    }
                }
            } else {
                LOGGER.info("Continent already exists: {}", continent);
                return getContinentByName(continent);
            }
        } catch (SQLException e) {
            LOGGER.error("Error inserting continent: {}", continent, e);
            return 0;
        }
    }

    private static void getContinents() {
        Set<Continent> continents = new HashSet<>();

        String query = "SELECT c.continent_id, c.continent_name, c.number_of_states, " +
                "s.country_id, s.country_name, s.country_capital " +
                "FROM continent c " +
                "LEFT JOIN continent_country cc ON c.continent_id = cc.continent_id " +
                "LEFT JOIN country s ON cc.country_id = s.country_id;";

        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement(query);

        ) {
            ResultSet resultSet = ps.executeQuery();
            while (resultSet.next()) {

                int continentId = resultSet.getInt("continent_id");
                String continentName = resultSet.getString("continent_name");
                int numberOfStates = resultSet.getInt("number_of_states");

                Continent continent = continents.stream()
                        .filter(c -> c.getId() == continentId)
                        .findFirst()
                        .orElseGet(() -> new Continent(continentId, continentName, numberOfStates, new HashSet<>()));


                int countryId = resultSet.getInt("country_id");
                if (!resultSet.wasNull()) {
                    String countryName = resultSet.getString("country_name");
                    String countryCapital = resultSet.getString("country_capital");
                    Country country = new Country(countryId, countryName, countryCapital);
                    continent.getCountries().add(country);
                }

                continents.add(continent);

            }
        } catch (SQLException e) {
            LOGGER.error("Error fetching continents: ", e);
        }
        for (Continent elem : continents) {
            if (elem.getCountries().isEmpty()) {
                System.out.println(elem + " with no associated countries");
            } else {
                System.out.println(elem + " ");
            }
        }
    }

    private static void deleteContinentFromCountry(int countryId) {
        String updateQuery = "UPDATE continent_country SET continent_id = null WHERE country_id = ?;";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {

            ps.setInt(1, countryId);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Country with ID: " + countryId + " has been successfully removed from its continent.");
            } else {
                System.out.println("No continent connection found for the country with ID: " + countryId + ".");
            }
        } catch (Exception e) {
            System.err.println("An error occurred while trying to remove the country with ID: " + countryId + " from its continent. Error: " + e.getMessage());
        }
    }

    private static void deleteCountryFromForeignTable(int countryId) {
        String deleteQuery = "DELETE FROM `continent_country` WHERE country_id = ?;";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(deleteQuery)) {

            ps.setInt(1, countryId);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Successfully removed all associations for country ID: " + countryId + " from continent_country table.");
            } else {
                System.out.println("No associations found to remove for country ID: " + countryId + " in continent_country table.");
            }
        } catch (Exception e) {
            System.err.println("An error occurred while attempting to delete associations for country ID: " + countryId + ". Error: " + e.getMessage());

        }
    }

    private static void deleteCountryById(int countryId) {
        if (countryId <= 0) {
            System.out.println("The provided country ID is invalid. Please enter a valid country ID.");
        }
        deleteContinentFromCountry(countryId);
        deleteCountryFromForeignTable(countryId);

        String deleteQuery = "DELETE FROM `country` WHERE country_id = ?;";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(deleteQuery)) {

            ps.setInt(1, countryId);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Country with ID: " + countryId + " was successfully deleted.");
            } else {
                System.out.println("No country found with ID: " + countryId + ", or it was previously deleted.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred while attempting to delete the country with ID: " + countryId + ". Error: " + e.getMessage());
        }
    }

    private static void updateForeignTableForContinent(int id) {
        String updateQuery = "UPDATE continent_country SET country_id = null WHERE continent_id = ?;";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(updateQuery)) {
            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Updated associations in the continent_country table for continent ID: " + id);
            } else {
                System.out.println("No rows found to update in continent_country table for continent ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("An error occurred while trying to update continent_country table for continent ID: " + id + ". Error: " + e.getMessage());
        }
    }

    private static void deleteContinentFromForeignTable(int id) {
        String deleteQuery = "DELETE FROM `continent_country` WHERE continent_id = ?;";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(deleteQuery)) {

            ps.setInt(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {
                System.out.println(affectedRows + " relationships deleted for continent ID: " + id);
            } else {
                System.out.println("No relationships found for continent ID: " + id + ", no action taken.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while attempting to delete relationships for continent ID: " + id + ": " + e.getMessage());
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    private static void deleteContinentById(int continentId) {
        if (continentId <= 0) {
            System.out.println("Invalid continent ID. Please try again with a valid ID.");
        }

        updateForeignTableForContinent(continentId);
        deleteContinentFromForeignTable(continentId);

        String deleteQuery = "DELETE FROM `continent` WHERE continent_id = ?;";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(deleteQuery)) {
            ps.setInt(1, continentId);
            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Continent successfully deleted.");
            } else {
                System.out.println("Continent deletion failed. No continent found with ID: " + continentId);
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while attempting to delete the continent. " + "Error: " + e.getMessage());
        }

    }

}