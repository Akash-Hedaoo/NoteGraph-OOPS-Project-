-- Run this script in MySQL before starting the Spring Boot backend. 
-- Hibernate will automatically generate all the tables inside this database.

CREATE DATABASE IF NOT EXISTS notegraph_db;

USE notegraph_db;

-- Note: Don't forget to check backend/src/main/resources/application.properties
-- to ensure that spring.datasource.username and spring.datasource.password 
-- match your local MySQL credentials.
