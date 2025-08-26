package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClassMapper;
import com.github.mydeardoctor.keyworddatasetbot.domain.FileIdsAndAudioClass;
import com.github.mydeardoctor.keyworddatasetbot.resources.SqlLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TelegramUserVoiceDAO extends DAO
{
    private static final String GET_VOICE_FILE_IDS_AND_AUDIO_CLASS =
        "get_voice_file_ids_and_audio_class";
    private static final String DELETE_MOST_RECENT_VOICE =
        "delete_most_recent_voice";

    private final Map<String, String> sqls;

    private final Logger logger;

    public TelegramUserVoiceDAO(final DataSource dataSource)
        throws IOException, IllegalArgumentException
    {
        super(dataSource);

        final String sqlSubdirectoryPath =
            getSqlSubdirectoryPath("telegram_user_voice_dao");

        final Set<String> sqlFileNames = new HashSet<>();
        sqlFileNames.add(GET_VOICE_FILE_IDS_AND_AUDIO_CLASS);
        sqlFileNames.add(DELETE_MOST_RECENT_VOICE);

        try
        {
            sqls = SqlLoader.loadSqls(sqlSubdirectoryPath, sqlFileNames);
        }
        catch(final IOException | IllegalArgumentException e)
        {
            throw e;
        }

        logger = LoggerFactory.getLogger(TelegramUserVoiceDAO.class);
    }

    public FileIdsAndAudioClass getVoiceFileIdsAndAudioClass(
        final Long userId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    sqls.get(GET_VOICE_FILE_IDS_AND_AUDIO_CLASS)))
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
            final AudioClass audioClass =
                AudioClassMapper.fromString(audioClassAsString);
            final FileIdsAndAudioClass fileIdsAndAudioClass =
                new FileIdsAndAudioClass(
                    fileUniqueId,
                    fileId,
                    audioClass);

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
                    sqls.get(DELETE_MOST_RECENT_VOICE)))
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