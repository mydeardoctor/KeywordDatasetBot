package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClassMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AudioClassDAO
{
    private final DataSource dataSource;

    private static final String SQL_GET_AUDIO_CLASSES =
        "SELECT audio_class_id FROM audio_class WHERE audio_class_id IS NOT NULL";
    private static final String SQL_GET_MAX_DURATION_BY_AUDIO_CLASS_ID =
        "SELECT max_duration_seconds FROM audio_class WHERE audio_class_id = ? AND audio_class_id IS NOT NULL";

    private final Logger logger;

    public AudioClassDAO(final DataSource dataSource)
    {
        super();

        this.dataSource = dataSource;
        logger = LoggerFactory.getLogger(AudioClassDAO.class);
    }


    public List<AudioClass> getAudioClasses() throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_AUDIO_CLASSES))
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
                    SQL_GET_MAX_DURATION_BY_AUDIO_CLASS_ID))
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
