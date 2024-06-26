# JDBC Project - Countries and Continents

This project establishes a many-to-many relationship between countries and continents. The application is executed from
the console using a scanner. If a country or continent already exists in the database, the application retrieves it
without creating a duplicate entry.

---

## Technologies Used

* Java 21
* Apache Maven 3.9.5
* Lombok 1.18.30
* MySQL Connector 8.3.0

---

## Setup

Clone the project from GitHub:

`git clone https://github.com/CarinaPorumb/JDBCProject`

Before running the application, create a MySQL database and tables for countries, continents, and their many-to-many
relationship. Use the following SQL scripts to set up the tables:

```sql
CREATE TABLE IF NOT EXISTS country
(
    country_id      int AUTO_INCREMENT,
    country_name    varchar(50),
    country_capital varchar(50),
    PRIMARY KEY (country_id)
);

CREATE TABLE IF NOT EXISTS continent
(
    continent_id     int AUTO_INCREMENT,
    continent_name   varchar(50),
    number_of_states INT,
    PRIMARY KEY (continent_id)
);

CREATE TABLE continent_country
(
    continent_id INT,
    country_id   INT,
    FOREIGN KEY (continent_id) REFERENCES continent (continent_id),
    FOREIGN KEY (country_id) REFERENCES country (country_id),
    UNIQUE (continent_id, country_id)
);

```

---

## Getting Started

Once the project is built, run the application by executing the `main` method in the `App` class. The scanner will
prompt you to input a country name, its capital, and related details, as well as a continent name and its number of
states. The application avoids creating duplicate entries by retrieving existing values from the database if a country
or continent already exists.

---