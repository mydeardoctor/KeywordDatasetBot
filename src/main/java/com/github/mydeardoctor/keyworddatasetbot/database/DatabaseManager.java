package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.delay.DelayManager;
import com.github.mydeardoctor.keyworddatasetbot.shutdown.ShutdownHookResourceCloser;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.*;
import java.util.*;


public class DatabaseManager
{
    private HikariDataSource dataSource;

    private static final int INITIAL_CONNECTION_TIMEOUT_MINUTES = 5;
    private static final int INITIAL_CONNECTION_TIMEOUT_S =
        INITIAL_CONNECTION_TIMEOUT_MINUTES * 60;

    private static final int INITIAL_CONNECTION_INTERVAL_S = 10;
    private static final int INITIAL_CONNECTION_INTERVAL_MS =
        INITIAL_CONNECTION_INTERVAL_S * 1000;

    private static final int INITIAL_CONNECTION_NUMBER_OF_TRIES =
        INITIAL_CONNECTION_TIMEOUT_S / INITIAL_CONNECTION_INTERVAL_S;

    private static final long CONNECTION_TIMEOUT_S = 60;
    private static final long CONNECTION_TIMEOUT_MS =
        CONNECTION_TIMEOUT_S * 1000;

    private static final int QUERY_TIMEOUT_S = 60;

    private final Logger logger;

    public DatabaseManager(
        final int poolSize,
        final String databaseServerHostname,
        final String databaseName,
        final String databaseServerPort,
        final String appRole,
        final String appPassword,
        final String appCertsDirectory,
        final String appDerKey,
        final String appKeyPassword,
        final String appCrt,
        final String caCrt)
    {
        final Properties properties = new Properties();

        //HikariCP properties.
        //https://github.com/brettwooldridge/HikariCP?tab=readme-ov-file#gear-configuration-knobs-baby
        properties.setProperty(
                "dataSourceClassName",
                "org.postgresql.ds.PGSimpleDataSource");
        properties.setProperty(
                "autoCommit",
                "false");
        properties.setProperty(
                "connectionTimeout",
                String.valueOf(CONNECTION_TIMEOUT_MS));
        properties.setProperty(
                "maximumPoolSize",
                String.valueOf(poolSize));

        //PostgreSQL Driver datasource properties.
        //https://jdbc.postgresql.org/documentation/datasource/#table113-datasource-configuration-properties
        properties.setProperty("dataSource.serverName", databaseServerHostname);
        properties.setProperty("dataSource.databaseName", databaseName);
        properties.setProperty("dataSource.portNumber", databaseServerPort);
        properties.setProperty("dataSource.user", appRole);
        properties.setProperty("dataSource.password", appPassword);
        properties.setProperty("dataSource.ssl", "true");

        //PostgreSQL Driver connection parameters.
        //https://jdbc.postgresql.org/documentation/use/#connection-parameters
        final Path appCertsDirectoryPath =
                Path.of(appCertsDirectory);
        final Path appDerKeyPath =
                appCertsDirectoryPath.resolve(appDerKey);
        final Path appCrtPath =
                appCertsDirectoryPath.resolve(appCrt);
        final Path caCrtPath =
                appCertsDirectoryPath.resolve(caCrt);
        properties.setProperty("dataSource.sslmode", "verify-full");
        properties.setProperty("dataSource.sslkey", appDerKeyPath.toString());
        properties.setProperty("dataSource.sslpassword", appKeyPassword);
        properties.setProperty("dataSource.sslcert", appCrtPath.toString());
        properties.setProperty("dataSource.sslrootcert", caCrtPath.toString());
        properties.setProperty("dataSource.tcpKeepAlive", "true");

        final HikariConfig config = new HikariConfig(properties);


        logger = LoggerFactory.getLogger(DatabaseManager.class);


        for(int i = 0; i < INITIAL_CONNECTION_NUMBER_OF_TRIES; ++i)
        {
            try
            {
                final String message = new StringBuilder()
                    .append("Trying to connect to database. Try №")
                    .append(i + 1)
                    .append(" out of ")
                    .append(INITIAL_CONNECTION_NUMBER_OF_TRIES)
                    .append(".")
                    .toString();
                logger.debug(message);
                dataSource = new HikariDataSource(config);
                break;
            }
            catch(final HikariPool.PoolInitializationException e)
            {
                if(i < (INITIAL_CONNECTION_NUMBER_OF_TRIES - 1))
                {
                    logger.debug("Could not connect to database! Waiting...");
                    DelayManager.delay(INITIAL_CONNECTION_INTERVAL_MS);
                }
                else
                {
                    logger.error("Could not connect to database!", e);
                    System.exit(1);
                }
            }
        }

        logger.debug("Successfully connected to database.");
        Runtime.getRuntime().addShutdownHook(
                new Thread(new ShutdownHookResourceCloser(dataSource)));
    }

    public DataSource getDataSource()
    {
        return dataSource;
    }

    public static PreparedStatement createPreparedStatement(
        final Connection connection,
        final String sql) throws SQLException
    {
        final PreparedStatement preparedStatement =
            connection.prepareStatement(sql);
        preparedStatement.setQueryTimeout(QUERY_TIMEOUT_S);
        return preparedStatement;
    }
}