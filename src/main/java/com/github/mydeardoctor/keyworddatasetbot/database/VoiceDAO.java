package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClassMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.Map;

public class VoiceDAO
{
    private final DataSource dataSource;

    private static final String SQL_SAVE_VOICE;
    private static final String SQL_GET_VOICE_COUNT;
    private static final String SQL_GET_TOTAL_VOICE_COUNT;

    private final Logger logger;

    public VoiceDAO(final DataSource dataSource)
    {
        super();

        this.dataSource = dataSource;
        logger = LoggerFactory.getLogger(VoiceDAO.class);
    }


    public void saveVoice(
        final String fileUniqueId,
        final String fileId,
        final int durationRoundedUpSeconds,
        final AudioClass audioClass,
        final Long userId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_SAVE_VOICE))
        {
            preparedStatement.setString(1, fileUniqueId);
            preparedStatement.setString(2, fileId);
            preparedStatement.setInt(3, durationRoundedUpSeconds);
            preparedStatement.setString(4, AudioClassMapper.toString(audioClass));
            preparedStatement.setLong(5, userId);

            final int numberOfRowsAffected = preparedStatement.executeUpdate();
            if(numberOfRowsAffected != 1)
            {
                final String errorMessage =
                    new StringBuilder()
                        .append("Saving voice affected ")
                        .append(numberOfRowsAffected)
                        .append(" number of rows instead of 1!")
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


    //TODO абстрагировать connection и commit
    public Map<AudioClass, Long> getVoiceCount(final Long userId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_VOICE_COUNT))
        {
            preparedStatement.setLong(1, userId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            Map<AudioClass, Long> voiceCount = new EnumMap<>(AudioClass.class);
            while(resultSet.next())
            {
                final String audioClassAsString =
                    resultSet.getString("audio_class_id");
                final Long count =
                    resultSet.getLong("count");

                final AudioClass audioClass =
                    AudioClassMapper.fromString(audioClassAsString);

                voiceCount.put(audioClass, count);
            }

            connection.commit();

            return voiceCount;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public long getTotalVoiceCount() throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_TOTAL_VOICE_COUNT))
        {
            long totalVoiceCount = 0;

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();
            if(isDataAvailable)
            {
                totalVoiceCount = resultSet.getLong("count");
            }

            connection.commit();

            return totalVoiceCount;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
