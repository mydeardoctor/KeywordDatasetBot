package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClassMapper;
import com.github.mydeardoctor.keyworddatasetbot.resources.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VoiceDAO extends DAO
{
    private static final String SAVE_VOICE = "save_voice";
    private static final String GET_VOICE_COUNT = "get_voice_count";
    private static final String GET_TOTAL_VOICE_COUNT = "get_total_voice_count";

    private final Map<String, String> sqls;

    private final Logger logger;

    public VoiceDAO(final DataSource dataSource)
        throws IOException, IllegalArgumentException
    {
        super(dataSource);

        final String sqlSubdirectoryPath =
            getSqlSubdirectoryPath("voice_dao");

        final Set<String> sqlFileNames = new HashSet<>();
        sqlFileNames.add(SAVE_VOICE);
        sqlFileNames.add(GET_VOICE_COUNT);
        sqlFileNames.add(GET_TOTAL_VOICE_COUNT);

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
                    sqls.get(SAVE_VOICE)))
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
                    sqls.get(GET_VOICE_COUNT)))
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
                    sqls.get(GET_TOTAL_VOICE_COUNT)))
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