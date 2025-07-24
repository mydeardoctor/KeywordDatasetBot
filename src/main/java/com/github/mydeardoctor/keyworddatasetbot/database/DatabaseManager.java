package com.github.mydeardoctor.keyworddatasetbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.sql.*;
import java.util.Properties;


public class DatabaseManager
{
    private final String databaseServerUrl;
    private final Properties connectionParameters;

    private static final int QUERY_TIMEOUT_S = 60;

    //TODO вынести в .sql файл
    private static final String SQL_SELECT_DIALOGUE_STATE =
        "SELECT dialogue_state_id FROM telegram_user WHERE user_id = ?";

    private final Logger logger;

    //TODO рефакторинг
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

        final Path clientAppCertsDirectoryPath =
            Path.of(clientAppCertsDirectory);
        final Path clientAppDerKeyPath =
            clientAppCertsDirectoryPath.resolve(clientAppDerKey);
        final Path clientAppCrtPath =
            clientAppCertsDirectoryPath.resolve(clientAppCrt);
        final Path caCrtPath =
            clientAppCertsDirectoryPath.resolve(caCrt);

        connectionParameters = new Properties();
        connectionParameters
            .setProperty("user", clientAppRole);
        connectionParameters
            .setProperty("password", clientAppPassword);
        connectionParameters
            .setProperty("ssl", "true");
        connectionParameters
            .setProperty("sslmode", "verify-full");
        connectionParameters
            .setProperty("sslkey", clientAppDerKeyPath.toString());
        connectionParameters
            .setProperty("sslpassword", clientAppKeyPassword);
        connectionParameters
            .setProperty("sslcert", clientAppCrtPath.toString());
        connectionParameters
            .setProperty("sslrootcert", caCrtPath.toString());

        logger = LoggerFactory.getLogger(DatabaseManager.class);
    }

    public String getDialogueState(final Long userId) throws SQLException
    {
        //TODO ENUM MAPPER
        String dialogueState = null;

        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(connection, SQL_SELECT_DIALOGUE_STATE))
        {
            preparedStatement.setLong(1, userId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();
            //TODO проверить как работает, если юзера нет
            if(isDataAvailable)
            {
                dialogueState = resultSet.getString("dialogue_state_id");
            }

            connection.commit();
        }
        catch(final SQLException e)
        {
            throw e;
        }

        return dialogueState;
    }

    private static PreparedStatement createPreparedStatement(
        final Connection connection,
        final String sql) throws SQLException
    {
        final PreparedStatement preparedStatement =
            connection.prepareStatement(sql);
        preparedStatement.setQueryTimeout(QUERY_TIMEOUT_S);
        return preparedStatement;
    }

//    private void getData()
//    {
//        try
//        {
//            Not recommended
//            COnnection pooling HIkari. OFSimpleDataSource. PGPoolingDataSource


            //TODO лучше DataSource

            //TODO try with resources
          //commit rollback

            //TODO try with resources
//            only one ResultSet can exist per Statement or PreparedStatement at a given time.
//            ResultSet resultSet = statement.executeQuery();
//            you must use a separate Statement for each thread.
            //TODO execute. executeQuery. executeUpdate
//            while(resultSet.next())
//            {
//                System.out.println(resultSet.getString(1));
//            }

            //traverse and create objects. Map?
//            String selectSql = "SELECT * FROM employees";
//            try (ResultSet resultSet = stmt.executeQuery(selectSql)) {
//                List<Employee> employees = new ArrayList<>();
//                while (resultSet.next()) {
//                    Employee emp = new Employee();
//                    emp.setId(resultSet.getInt("emp_id"));
//                    emp.setName(resultSet.getString("name"));
//                    emp.setPosition(resultSet.getString("position"));
//                    emp.setSalary(resultSet.getDouble("salary"));
//                    employees.add(emp);
//                }
//            }

//            resultSet.close();
//            statement.close();
//
//            connection.close();
//            new PGSimpleDataSource();
//            //TODO hikari
//        }
//        catch(final SQLException e)
//        {
//            //TODO
//            e.printStackTrace();
//        }
//    }

}
