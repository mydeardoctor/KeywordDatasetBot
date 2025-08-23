package com.github.mydeardoctor.keyworddatasetbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelegramUserVoiceDAO
{
    private final DataSource dataSource;

    //TODO return object
    private static final String SQL_GET_VOICE_FILE_IDS_AND_AUDIO_CLASS =
        "";
    private static final String SQL_DELETE_MOST_RECENT_VOICE =
        "";

    //TODO убрать
    public static int FILE_UNIQUE_ID_INDEX = 0;
    public static int FILE_ID_INDEX = 1;
    public static int AUDIO_CLASS_INDEX = 2;



    private final Logger logger;

    public TelegramUserVoiceDAO(final DataSource dataSource)
    {
        super();

        this.dataSource = dataSource;
        logger = LoggerFactory.getLogger(TelegramUserVoiceDAO.class);
    }



    public List<String> getVoiceFileIdsAndAudioClass(final Long userId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_VOICE_FILE_IDS_AND_AUDIO_CLASS))
        {
            preparedStatement.setLong(1, userId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();

            if(!isDataAvailable)
            {
                final String errorMessage =
                    "Telegram user does not contain most recent voice!";
                throw new SQLException(errorMessage);
            }

            final String fileUniqueId =
                resultSet.getString("file_unique_id");
            final String fileId =
                resultSet.getString("file_id");
            final String audioClassAsString =
                resultSet.getString("audio_class_id");
            final List<String> fileIdsAndAudioClass =
                new ArrayList<>(3);
            fileIdsAndAudioClass.add(FILE_UNIQUE_ID_INDEX, fileUniqueId);
            fileIdsAndAudioClass.add(FILE_ID_INDEX, fileId);
            fileIdsAndAudioClass.add(AUDIO_CLASS_INDEX, audioClassAsString);

            connection.commit();

            return fileIdsAndAudioClass;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public void deleteMostRecentVoice(final Long userId) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_DELETE_MOST_RECENT_VOICE))
        {
            preparedStatement.setLong(1, userId);

            final int numberOfRowsAffected = preparedStatement.executeUpdate();
            if((numberOfRowsAffected != 0) && (numberOfRowsAffected != 1))
            {
                final String errorMessage =
                    new StringBuilder()
                        .append("Deleting most recent voice affected ")
                        .append(numberOfRowsAffected)
                        .append(" number of rows instead of 0 or 1!")
                        .toString();
                throw new SQLException(errorMessage);
            }

            connection.commit();
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
