package com.github.mydeardoctor.keyworddatasetbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;


public class DatabaseManager
{
    private final String databaseServerUrl;
    private final String clientAppRole;
    private final String clientAppPassword;
    private final String clientAppKeyPassword;

    private final Path clientAppDerKeyPath;
    private final Path clientAppCrtPath;
    private final Path caCrtPath;

    private final Logger logger;

    public DatabaseManager(
        final String databaseServerUrl,
        final String clientAppRole,
        final String clientAppPassword,
        final String clientAppCertsDirectory,
        final String clientAppDerKey,
        final String clientAppKeyPassword,
        final String clientAppCrt,
        final String caCrt)
    {
        this.databaseServerUrl = databaseServerUrl;
        this.clientAppRole = clientAppRole;
        this.clientAppPassword = clientAppPassword;
        this.clientAppKeyPassword = clientAppKeyPassword;

        final Path clientAppCertsDirectoryPath = Path.of(clientAppCertsDirectory);
        this.clientAppDerKeyPath = clientAppCertsDirectoryPath.resolve(clientAppDerKey);
        this.clientAppCrtPath = clientAppCertsDirectoryPath.resolve(clientAppCrt);
        this.caCrtPath = clientAppCertsDirectoryPath.resolve(caCrt);

        logger = LoggerFactory.getLogger(DatabaseManager.class);
    }

    public void getData()
    {
        try
        {
            //Connection parameters
            final Properties properties = new Properties();
            properties.setProperty("user", clientAppRole);
            properties.setProperty("password", clientAppPassword);
            properties.setProperty("ssl", "true");
            properties.setProperty("sslmode", "verify-full");
            properties.setProperty("sslkey", clientAppDerKeyPath.toString());
            properties.setProperty("sslpassword", clientAppKeyPassword);
            properties.setProperty("sslcert", clientAppCrtPath.toString());
            properties.setProperty("sslrootcert", caCrtPath.toString());

            final Connection connection =
                DriverManager.getConnection(databaseServerUrl, properties);
            //TODO лучше DataSource

            Statement statement = connection.createStatement();
            //TODO prepared statement ?
            ResultSet resultSet = statement.executeQuery("SELECT dialogue_state_id FROM dialogue_state");
            while(resultSet.next())
            {
                System.out.println(resultSet.getString(1));
            }
            resultSet.close();
            statement.close();

            connection.close();
        }
        catch(final SQLException e)
        {
            //TODO
            e.printStackTrace();
        }
    }
}
