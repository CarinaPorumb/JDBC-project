# JDBC Project - Countries and Continents

This project aims to create a many-to-many relationship between a country and a continent. The application is executed
from the console using a scanner. If a country or continent is repeated, the application does not duplicate the value 
but retrieves it from the database.

---

## Technologies Used

######   * Java 20

######   * JDK 20

######   * Apache Maven 3.9.1

######   * Lombok 1.18.26

######   * MySQL Connector 8.0.32

---

## Setup

Clone the project from GitHub:

`git clone https://github.com/CarinaPorumb/JDBCProject`

Before running the application, you need to create a MySQL database and tables for country, continent
and their many-to-many relationship. You can use the following SQL scripts to create the tables:

````
CREATE TABLE IF NOT EXISTS country (
    country_id int AUTO_INCREMENT,
    country_name varchar(50),
    country_capital varchar(50),
    PRIMARY KEY (country_id));

CREATE TABLE IF NOT EXISTS continent (
    continent_id int AUTO_INCREMENT,
    continent_name varchar(50),
    number_of_states INT,
    PRIMARY KEY (continent_id)); 

CREATE TABLE continent_country(
    continent_id INT,
    country_id INT,
    FOREIGN KEY(continent_id) REFERENCES continent(continent_id),
    FOREIGN KEY(country_id) REFERENCES country(country_id),
    UNIQUE(continent_id,country_id));

````

## Getting Started

Once the project is built, you can run the application by executing the `main` method in the `App` class.
The scanner will then prompt you to input the name of a country, its capital and some other
values, as well as the name of a continent, its number of states and some other values. If you enter a country or
continent that already exists in the database, the application will not create a duplicate entry but will retrieve the
existing values.
