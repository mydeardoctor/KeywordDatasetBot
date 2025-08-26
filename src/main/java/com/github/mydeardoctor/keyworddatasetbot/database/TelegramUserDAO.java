package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.*;
import com.github.mydeardoctor.keyworddatasetbot.resources.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class TelegramUserDAO extends DAO
{
    private static final String GET_DIALOGUE_STATE
        = "get_dialogue_state";
    private static final String GET_AUDIO_CLASS
        = "get_audio_class";
    private static final String SAVE_USER
        = "save_user";
    private static final String GET_USER_AND_CHAT_IDS
        = "get_user_and_chat_ids";
    private static final String UPDATE_DIALOGUE_STATE_AND_AUDIO_CLASS
        = "update_dialogue_state_and_audio_class";
    private static final String UPDATE_DIALOGUE_STATE
        = "update_dialogue_state";
    private static final String UPDATE_MOST_RECENT_VOICE
        = "update_most_recent_voice";

    private final Map<String, String> sqls;

    private static final int BATCH_SIZE = 100;

    private final Logger logger;

    public TelegramUserDAO(final DataSource dataSource)
        throws IOException, IllegalArgumentException
    {
        super(dataSource);

        final String sqlSubdirectoryPath =
            getSqlSubdirectoryPath("telegram_user_dao");

        final Set<String> sqlFileNames = new HashSet<>();
        sqlFileNames.add(GET_DIALOGUE_STATE);
        sqlFileNames.add(SAVE_USER);
        sqlFileNames.add(GET_USER_AND_CHAT_IDS);
        sqlFileNames.add(GET_AUDIO_CLASS);
        sqlFileNames.add(UPDATE_DIALOGUE_STATE_AND_AUDIO_CLASS);
        sqlFileNames.add(UPDATE_DIALOGUE_STATE);
        sqlFileNames.add(UPDATE_MOST_RECENT_VOICE);

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
                    sqls.get(GET_DIALOGUE_STATE)))
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
                    sqls.get(SAVE_USER)))
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

    public List<UserIdAndChatId> getUserAndChatIds(final Long lastUserId)
        throws SQLException
    {
        try(final ConnectionWithRollback connection =
                new ConnectionWithRollback(dataSource);
            final PreparedStatement preparedStatement =
                DatabaseManager.createPreparedStatement(
                    connection,
                    sqls.get(GET_USER_AND_CHAT_IDS)))
        {
            preparedStatement.setLong(1, lastUserId);
            preparedStatement.setInt(2, BATCH_SIZE);

            final ResultSet resultSet = preparedStatement.executeQuery();
            final List<UserIdAndChatId> userAndChatIds = new ArrayList<>();
            while(resultSet.next())
            {
                final Long userId = resultSet.getLong("user_id");
                final Long chatId = resultSet.getLong("chat_id");
                final UserIdAndChatId userIdAndChatId =
                    new UserIdAndChatId(userId, chatId);
                userAndChatIds.add(userIdAndChatId);
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
                    sqls.get(GET_AUDIO_CLASS)))
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
                    sqls.get(UPDATE_DIALOGUE_STATE_AND_AUDIO_CLASS)))
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
                    sqls.get(UPDATE_DIALOGUE_STATE)))
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
                    sqls.get(UPDATE_MOST_RECENT_VOICE)))
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