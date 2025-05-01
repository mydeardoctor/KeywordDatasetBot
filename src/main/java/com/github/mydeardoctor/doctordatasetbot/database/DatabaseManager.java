package com.github.mydeardoctor.doctordatasetbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DatabaseManager
{
    //TODO изменить базу данных
    //TODO мб сделать отдельного юзера, чтобы не заходить как админ
    private static final String url =
        "jdbc:postgresql://localhost:5432/text_database"; //TODO percent encoded?
    //https://jdbc.postgresql.org/documentation/use/ TODO
    private static final String user =
        "postgres";
    private static final String password =
        System.getenv("POSTGRES_PASSWORD");

    private final Logger logger =
        LoggerFactory.getLogger(DatabaseManager.class);

    public DatabaseManager()
    {

    }

    //TODO synchronized? different connections? connection pool?
    public void getData()
    {
        try
        {
//            Properties properties = new Properties();
//            properties.setProperty()
            final Connection connection =
                DriverManager.getConnection(url, user, password);
            //TODO лучше DataSource

            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM text_file_metadata");
            while(resultSet.next())
            {
                System.out.println(resultSet.getString(1) + resultSet.getString(2));
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
