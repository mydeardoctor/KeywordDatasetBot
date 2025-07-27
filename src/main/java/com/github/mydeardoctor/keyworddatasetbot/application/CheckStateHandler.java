package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.telegramuser.TelegramUserCommunicationManager;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.SQLException;

//TODO yes leads to /start -> choose state
public class CheckStateHandler extends StateHandler
{
    public CheckStateHandler(
        final DatabaseManager databaseManager,
        final TelegramUserCommunicationManager telegramUserCommunicationManager)
    {
        super(
            databaseManager,
            telegramUserCommunicationManager,
            LoggerFactory.getLogger(CheckStateHandler.class));
    }

    @Override
    protected void onStartReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onStartReceive(chatId, userId);
    }

    @Override
    protected void onStatsReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onStatsReceive(chatId, userId);
    }

    @Override
    protected void onHelpReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onHelpReceive(chatId, userId);
    }

    @Override
    protected void onCancelReceive(final Long chatId, final Long userId)
        throws SQLException
    {
        //Send "typing..." to telegram user.
        telegramUserCommunicationManager.sendChatAction(
            chatId,
            TelegramUserCommunicationManager.CHAT_ACTION_TYPING);

        deleteMostRecentVoice(chatId, userId);

        super.onCancelReceive(chatId, userId);
    }

    private void deleteMostRecentVoice(final Long chatId, final Long userId)
        throws SQLException
    {
        //Query DB.
        try
        {
            databaseManager.deleteMostRecentVoice(userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
