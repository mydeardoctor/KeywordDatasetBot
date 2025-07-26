package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClassMapper;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueState;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueStateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Audio;

import java.nio.file.Path;
import java.sql.*;
import java.util.*;


public class DatabaseManager
{
    private final String databaseServerUrl;
    private final Properties connectionParameters;

    private static final int QUERY_TIMEOUT_S = 60;

    //TODO вынести в .sql файл
    private static final String SQL_GET_DIALOGUE_STATE =
        "SELECT dialogue_state_id FROM telegram_user WHERE user_id = ?";
    private static final String SQL_SAVE_USER =
        "INSERT INTO telegram_user (user_id, username, first_name, last_name, dialogue_state_id, audio_class_id, most_recent_voice_id) VALUES (?, ?, ?, ?, 'start', NULL, NULL)";
    private static final String SQL_GET_AUDIO_CLASSES =
        "SELECT audio_class_id FROM audio_class WHERE audio_class_id IS NOT NULL";
    private static final String SQL_GET_VOICE_COUNT =
        "SELECT audio_class_id, COUNT(audio_class_id) AS count FROM voice WHERE user_id = ? GROUP BY audio_class_id";
    private static final String SQL_GET_TOTAL_VOICE_COUNT =
        "SELECT COUNT(audio_class_id) AS count FROM voice";
    private static final String SQL_DELETE_MOST_RECENT_VOICE =
        "DELETE FROM voice WHERE file_unique_id = (SELECT most_recent_voice_id FROM telegram_user WHERE user_id = ? AND most_recent_voice_id IS NOT NULL)";
    private static final String SQL_UPDATE_DIALOGUE_STATE_AND_AUDIO_CLASS =
        "UPDATE telegram_user SET (dialogue_state_id, audio_class_id) = (?, ?) WHERE user_id = ?";

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

    public DialogueState getDialogueState(final Long userId) throws SQLException
    {
        DialogueState dialogueState = null;

        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(connection, SQL_GET_DIALOGUE_STATE))
        {
            preparedStatement.setLong(1, userId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();
            if(isDataAvailable)
            {
                final String dialogueStateAsString =
                    resultSet.getString("dialogue_state_id");
                dialogueState = DialogueStateMapper.map(dialogueStateAsString);
            }

            connection.commit();
        }
        catch(final SQLException e)
        {
            throw e;
        }

        return dialogueState;
    }

    public void saveUser(
        final Long userId,
        final String username,
        final String firstName,
        final String lastName) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(connection, SQL_SAVE_USER))
        {
            preparedStatement.setLong(
                1, userId);
            preparedStatement.setString(
                2, username);
            preparedStatement.setString(
                3, firstName);
            preparedStatement.setString(
                4, lastName);

            final int numberOfRowsAffected = preparedStatement.executeUpdate();
            if(numberOfRowsAffected != 1)
            {
                final String errorMessage =
                    new StringBuilder()
                        .append("Saving user affected ")
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

    public List<AudioClass> getAudioClasses() throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(connection, SQL_GET_AUDIO_CLASSES))
        {
            final ResultSet resultSet = preparedStatement.executeQuery();
            List<AudioClass> audioClasses = new ArrayList<>();
            while(resultSet.next())
            {
                final String audioClassAsString =
                    resultSet.getString("audio_class_id");
                final AudioClass audioClass =
                    AudioClassMapper.map(audioClassAsString);
                if(audioClass != null)
                {
                    audioClasses.add(audioClass);
                }
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

    //TODO абстрагировать connection и commit
    public Map<AudioClass, Long> getVoiceCount(final Long userId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(connection, SQL_GET_VOICE_COUNT))
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
                    AudioClassMapper.map(audioClassAsString);

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
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(connection, SQL_GET_TOTAL_VOICE_COUNT))
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

    public void deleteMostRecentVoice(final Long userId) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(
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

    public void updateDialogueStateAndAudioClass(
        final Long userId,
        final DialogueState dialogueState,
        final AudioClass audioClass) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(
                    databaseServerUrl, connectionParameters);
            final PreparedStatement preparedStatement =
                createPreparedStatement(
                    connection,
                    SQL_UPDATE_DIALOGUE_STATE_AND_AUDIO_CLASS))
        {
            preparedStatement.setString(
                1, DialogueStateMapper.map(dialogueState));
            preparedStatement.setString(
                2, AudioClassMapper.map(audioClass));
            preparedStatement.setLong(
                3, userId);

            final int numberOfRowsAffected = preparedStatement.executeUpdate();
            if(numberOfRowsAffected != 1)
            {
                final String errorMessage =
                    new StringBuilder()
                        .append("Updating dialogue state and audio class affected ")
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

    private static PreparedStatement createPreparedStatement(
        final Connection connection,
        final String sql) throws SQLException
    {
        final PreparedStatement preparedStatement =
            connection.prepareStatement(sql);
        preparedStatement.setQueryTimeout(QUERY_TIMEOUT_S);
        return preparedStatement;
    }
}
