package ro.ItSchool;

import java.sql.*;
import java.util.*;

public class App {

    private static Scanner scanner;
    private final static String DATABASE_URL = "jdbc:mysql://localhost/countryandcontinent";
    private final static String USER = "root";
    private final static String PASSWORD = "";

    public static void main(String[] args) throws SQLException {

        scanner = new Scanner(System.in);

        insertIntoCountryContinent();
        getAllCountries();
     //   getContinents();
        scanner.close();
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
                    if (generatedKeys.next())
                        return generatedKeys.getLong(1);
                    else
                        throw new SQLException("Creating country failed, no ID obtained.");
                }
            } else
                return getCountryByName(country);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static long insertContinent() throws SQLException {
        System.out.println("Continent name: ");
        String continent = scanner.nextLine();
        System.out.println("Number of states: ");
        int states = Integer.parseInt(scanner.nextLine());
        String queryContinent = "INSERT INTO continent(continent_name,number_of_states) VALUES (?,?)";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement ps = connection.prepareStatement(queryContinent, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, continent);
            ps.setInt(2, states);
            if (!checkIfContinentExist(continent)) {
                ps.executeUpdate();
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next())
                        return generatedKeys.getLong(1);
                    else
                        throw new SQLException("Creating continent failed, no ID obtained.");
                }
            } else
                return getContinentByName(continent);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static boolean checkIfCountryExist(String country) throws SQLException {
        String query = "SELECT COUNT(*) FROM country WHERE country_name LIKE '%" + country + "%'";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                if (resultSet.getInt(1) == 0)
                    return false;
                else
                    return true;
            }
        } catch (Exception e) {
            throw new SQLException("SQL error");
        }
        return false;
    }

    private static boolean checkIfContinentExist(String continent) throws SQLException {
        String query = "SELECT COUNT(*) FROM continent WHERE continent_name LIKE '%" + continent + "%'";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                if (resultSet.getInt(1) == 0)
                    return false;
                else
                    return true;
            }
        } catch (SQLException e) {
            throw new SQLException("SQL error");
        }
        return false;
    }

    private static int getContinentByName(String continent) throws SQLException {
        String query = "SELECT continent_id FROM continent WHERE continent_name LIKE '%" + continent + "%'";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new SQLException("SQL error");
        }
        return 0;
    }

    private static int getCountryByName(String country) throws SQLException {
        String query = "SELECT country_id FROM country WHERE country_name LIKE '%" + country + "%'";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            throw new SQLException("SQL error");
        }
        return 0;
    }

    private static void insertIntoCountryContinent() throws SQLException {
        long countryId = insertCountry();
        long continentId = insertContinent();
        String queryInsertContinent = "INSERT INTO continent_country(continent_id,country_id) VALUES (?,?);";
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(queryInsertContinent)) {
            preparedStatement.setInt(1, (int) continentId);
            preparedStatement.setInt(2, (int) countryId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void getAllCountries() {
        Set<Country> countriesSet = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement st = connection.createStatement()) {
            ResultSet resultSet = st.executeQuery("SELECT * FROM continent_country cc\n" +
                    "INNER join country s on \n" +
                    "cc.country_id = s.country_id\n" +
                    "INNER join continent c ON\n" +
                    "cc.continent_id = c.continent_id;");
            while (resultSet.next()) {
                Country countries1 = new Country(resultSet.getInt("country_id"),
                        resultSet.getString("country_name"),
                        resultSet.getString("country_capital"),
                        new Continent(resultSet.getInt("continent_id"),
                                resultSet.getString("continent_name"),
                                resultSet.getInt("number_of_states")));
                countriesSet.add(countries1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Country elem : countriesSet) {
            System.out.println(elem + " ");
        }
    }

    private static void getContinents() {
        Set<Continent> continentsSet = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(DATABASE_URL, USER, PASSWORD);
             Statement st = connection.createStatement()) {
            ResultSet resultSet = st.executeQuery("SELECT * FROM continent_country cc\n" +
                    "INNER join continent c on\n" +
                    "cc.continent_id = c.continent_id\n" +
                    "INNER join country s ON\n" +
                    "cc.country_id = s.country_id;");
            while (resultSet.next()) {
                Continent continent = new Continent(resultSet.getInt("continent_id"),
                        resultSet.getString("continent_name"),
                        resultSet.getInt("number_of_states"),
                        new Country(resultSet.getInt("country_id"),
                                resultSet.getString("country_name"),
                                resultSet.getString("country_capital")));

                continentsSet.add(continent);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (Continent elem : continentsSet) {
            System.out.println(elem + " ");
        }
    }
}