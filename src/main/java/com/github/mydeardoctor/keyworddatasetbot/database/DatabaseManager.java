package com.github.mydeardoctor.keyworddatasetbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;


public class DatabaseManager
{
    private final String url;
    private final String user;
    private final String password;

    private final Logger logger;

    public DatabaseManager(
        final String url,
        final String user,
        final String password)
    {
        this.url = url;
        this.user = user;
        this.password = password;

        logger = LoggerFactory.getLogger(DatabaseManager.class);
    }

    public void getData()
    {
        try
        {
            final Properties properties = new Properties();
            properties.setProperty("user", user);
            properties.setProperty("password", password);
//            properties.setProperty("ssl", "true");

            final Connection connection =
                DriverManager.getConnection(url, properties);
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
//            e.printStackTrace();
        }
    }
}
