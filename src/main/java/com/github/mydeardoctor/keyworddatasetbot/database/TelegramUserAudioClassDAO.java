package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.resources.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TelegramUserAudioClassDAO extends DAO
{
    private static final String GET_MAX_DURATION_BY_USER_ID
        = "get_max_duration_by_user_id";

    private final Map<String, String> sqls;

    private final Logger logger;

    public TelegramUserAudioClassDAO(final DataSource dataSource)
        throws IOException, IllegalArgumentException
    {
        super(dataSource);

        final String sqlSubdirectoryPath =
            getSqlSubdirectoryPath("telegram_user_audio_class_dao");

        final Set<String> sqlFileNames = new HashSet<>();
        sqlFileNames.add(GET_MAX_DURATION_BY_USER_ID);

        try
        {
            sqls = ResourceLoader.loadStrings(
                sqlSubdirectoryPath,
                sqlFileNames,
                SQL_EXTENSION);
        }
        catch(final IOException | IllegalArgumentException e)
        {
            throw e;
        }

        logger = LoggerFactory.getLogger(TelegramUserAudioClassDAO.class);
    }

    public int getMaxDuration(final Long userId) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    sqls.get(GET_MAX_DURATION_BY_USER_ID)))
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