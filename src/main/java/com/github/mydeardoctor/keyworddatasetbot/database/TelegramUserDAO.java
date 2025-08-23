package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClassMapper;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueState;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueStateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TelegramUserDAO
{
    private final DataSource dataSource;

    private static final String SQL_GET_DIALOGUE_STATE =
        "SELECT dialogue_state_id FROM telegram_user WHERE user_id = ?";
    private static final String SQL_SAVE_USER =
        "INSERT INTO telegram_user (user_id, username, first_name, last_name, chat_id, dialogue_state_id, audio_class_id, most_recent_voice_id) VALUES (?, ?, ?, ?, ?, 'start', NULL, NULL)";
    private static final String SQL_GET_USER_AND_CHAT_IDS =
        "SELECT user_id, chat_id FROM telegram_user WHERE user_id > ? AND chat_id IS NOT NULL ORDER BY user_id ASC FETCH FIRST ? ROWS ONLY";
    private static final String SQL_GET_AUDIO_CLASS =
        "SELECT audio_class_id FROM telegram_user WHERE user_id = ?";

    private static final String SQL_UPDATE_DIALOGUE_STATE_AND_AUDIO_CLASS =
        "UPDATE telegram_user SET (dialogue_state_id, audio_class_id) = (?, ?) WHERE user_id = ?";
    private static final String SQL_UPDATE_DIALOGUE_STATE =
        "UPDATE telegram_user SET dialogue_state_id = ? WHERE user_id = ?";
    private static final String SQL_UPDATE_MOST_RECENT_VOICE =
        "UPDATE telegram_user SET most_recent_voice_id = ? WHERE user_id = ?";

    private static final int BATCH_SIZE = 100;
    public static int USER_ID_INDEX = 0; //TODO remove
    public static int CHAT_ID_INDEX = 1; //TODO remove

    private final Logger logger;

    public TelegramUserDAO(final DataSource dataSource)
    {
        super();

        this.dataSource = dataSource;
        logger = LoggerFactory.getLogger(TelegramUserDAO.class);
    }

    public DialogueState getDialogueState(final Long userId) throws SQLException
    {
        DialogueState dialogueState = null;

        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_DIALOGUE_STATE))
        {
            preparedStatement.setLong(1, userId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();
            //TODO is not available exception throw SQL exception
            if(isDataAvailable)
            {
                final String dialogueStateAsString =
                    resultSet.getString("dialogue_state_id");
                dialogueState = DialogueStateMapper.fromString(dialogueStateAsString);
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
        final String lastName,
        final Long chatId) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_SAVE_USER))
        {
            preparedStatement.setLong(
                1, userId);
            preparedStatement.setString(
                2, username);
            preparedStatement.setString(
                3, firstName);
            preparedStatement.setString(
                4, lastName);
            preparedStatement.setLong(
                5, chatId);

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

    /// TODO return object not list
    public List<List<Long>> getUserAndChatIds(final Long lastUserId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_USER_AND_CHAT_IDS))
        {
            preparedStatement.setLong(1, lastUserId);
            preparedStatement.setInt(2, BATCH_SIZE);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<List<Long>> userAndChatIds = new ArrayList<>();
            while(resultSet.next())
            {
                final List<Long> userAndChatId =
                    new ArrayList<>(2);
                final Long userId = resultSet.getLong("user_id");
                final Long chatId = resultSet.getLong("chat_id");
                userAndChatId.add(USER_ID_INDEX, userId);
                userAndChatId.add(CHAT_ID_INDEX, chatId);
                userAndChatIds.add(userAndChatId);
            }

            connection.commit();

            return userAndChatIds;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }


    public AudioClass getAudioClass(final Long userId) throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_GET_AUDIO_CLASS))
        {
            preparedStatement.setLong(1, userId);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final boolean isDataAvailable = resultSet.next();

            if(!isDataAvailable)
            {
                final String errorMessage =
                    "Telegram user does not contain audio class!";
                throw new SQLException(errorMessage);
            }

            final String audioClassAsString =
                resultSet.getString("audio_class_id");
            final AudioClass audioClass =
                AudioClassMapper.fromString(audioClassAsString);

            connection.commit();

            return audioClass;
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
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_UPDATE_DIALOGUE_STATE_AND_AUDIO_CLASS))
        {
            preparedStatement.setString(
                1, DialogueStateMapper.toString(dialogueState));
            preparedStatement.setString(
                2, AudioClassMapper.toString(audioClass));
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

    public void updateDialogueState(
        final Long userId,
        final DialogueState dialogueState)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_UPDATE_DIALOGUE_STATE))
        {
            preparedStatement.setString(
                1, DialogueStateMapper.toString(dialogueState));
            preparedStatement.setLong(
                2, userId);

            final int numberOfRowsAffected = preparedStatement.executeUpdate();
            if(numberOfRowsAffected != 1)
            {
                final String errorMessage =
                    new StringBuilder()
                        .append("Updating dialogue state affected ")
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

    public void updateMostRecentVoice(
        final Long userId,
        final String fileUniqueId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    SQL_UPDATE_MOST_RECENT_VOICE))
        {
            preparedStatement.setString(1, fileUniqueId);
            preparedStatement.setLong(2, userId);

            final int numberOfRowsAffected = preparedStatement.executeUpdate();
            if(numberOfRowsAffected != 1)
            {
                final String errorMessage =
                    new StringBuilder()
                        .append("Updating most recent voice affected ")
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
}
