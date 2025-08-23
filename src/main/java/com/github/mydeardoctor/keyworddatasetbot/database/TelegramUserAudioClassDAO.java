package com.github.mydeardoctor.keyworddatasetbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TelegramUserAudioClassDAO
{
    private final DataSource dataSource;

    private static final String SQL_GET_MAX_DURATION_BY_USER_ID;


    private final Logger logger;

    public TelegramUserAudioClassDAO(final DataSource dataSource)
    {
        super();

        this.dataSource = dataSource;
        logger = LoggerFactory.getLogger(TelegramUserAudioClassDAO.class);
    }


    public int getMaxDuration(final Long userId) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_MAX_DURATION_BY_USER_ID))
        {
            preparedStatement.setLong(1, userId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();

            if(!isDataAvailable)
            {
                final String errorMessage =
                    "Telegram user does not contain chosen audio class!";
                throw new SQLException(errorMessage);
            }

            final int maxDurationSeconds =
                resultSet.getInt("max_duration_seconds");

            connection.commit();

            return maxDurationSeconds;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

}
