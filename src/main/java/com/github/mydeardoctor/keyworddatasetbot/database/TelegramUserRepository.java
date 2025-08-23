package com.github.mydeardoctor.keyworddatasetbot.database;

import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class TelegramUserRepository
{
    private final TelegramUserDAO telegramUserDAO;
    private Logger logger;

    public TelegramUserRepository(final TelegramUserDAO telegramUserDAO)
    {
        super();

        this.telegramUserDAO = telegramUserDAO;
        logger = LoggerFactory.getLogger(TelegramUserRepository.class);
    }

    //TODO methods
    public DialogueState getDialogueState(final Long userId) throws SQLException
    {
        try
        {
            final DialogueState dialogueState =
                telegramUserDAO.getDialogueState(userId);
            return dialogueState;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public void saveUser(
        final Long userId,
        final String username,
        final String firstName,
        final String lastName,
        final Long chatId) throws SQLException
    {
        try
        {
            telegramUserDAO.saveUser(
                userId,
                username,
                firstName,
                lastName,
                chatId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    //TODO return object
    public List<List<Long>> getUserAndChatIds(final Long lastUserId)
        throws SQLException
    {
        try
        {
            final List<List<Long>> userAndChatIds =
                telegramUserDAO.getUserAndChatIds(lastUserId);
            return userAndChatIds;
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    public AudioClass getAudioClass(final Long userId) throws SQLException
    {
        try
        {
            final AudioClass audioClass = telegramUserDAO.getAudioClass(userId);
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
        try
        {
            telegramUserDAO.updateDialogueStateAndAudioClass(
                userId,
                dialogueState,
                audioClass);
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
        try
        {
            telegramUserDAO.updateDialogueState(
                userId,
                dialogueState);
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
        try
        {
            telegramUserDAO.updateMostRecentVoice(
                userId,
                fileUniqueId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
