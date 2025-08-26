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
import java.util.*;

public class AudioClassDAO extends DAO
{
    private static final String GET_AUDIO_CLASSES
        = "get_audio_classes";
    private static final String GET_MAX_DURATION_BY_AUDIO_CLASS_ID
        = "get_max_duration_by_audio_class_id";

    private final Map<String, String> sqls;

    private final Logger logger;

    public AudioClassDAO(final DataSource dataSource)
        throws IOException, IllegalArgumentException
    {
        super(dataSource);

        final String sqlSubdirectoryPath =
            getSqlSubdirectoryPath("audio_class_dao");

        final Set<String> sqlFileNames = new HashSet<>();
        sqlFileNames.add(GET_AUDIO_CLASSES);
        sqlFileNames.add(GET_MAX_DURATION_BY_AUDIO_CLASS_ID);

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

        logger = LoggerFactory.getLogger(AudioClassDAO.class);
    }

    public List<AudioClass> getAudioClasses() throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    sqls.get(GET_AUDIO_CLASSES)))
        {
            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<AudioClass> audioClasses = new ArrayList<>();
            while(resultSet.next())
            {
                final String audioClassAsString =
                    resultSet.getString("audio_class_id");
                final AudioClass audioClass =
                    AudioClassMapper.fromString(audioClassAsString);
                audioClasses.add(audioClass);
            }

            if(audioClasses.isEmpty())
            {
                final String errorMessage =
                    "There are no audio classes in the database!";
                throw new SQLException(errorMessage);
            }

            connection.commit();

            return audioClasses;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public int getMaxDuration(final AudioClass audioClass) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    sqls.get(GET_MAX_DURATION_BY_AUDIO_CLASS_ID)))
        {
            preparedStatement.setString(
                1, AudioClassMapper.toString(audioClass));

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();

            if(!isDataAvailable)
            {
                final String errorMessage =
                    "There is no audio class in the database!";
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