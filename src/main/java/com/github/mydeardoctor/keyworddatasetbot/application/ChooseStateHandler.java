package com.github.mydeardoctor.keyworddatasetbot.application;

import com.github.mydeardoctor.keyworddatasetbot.database.DatabaseManager;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClass;
import com.github.mydeardoctor.keyworddatasetbot.domain.AudioClassMapper;
import com.github.mydeardoctor.keyworddatasetbot.domain.DialogueState;
import com.github.mydeardoctor.keyworddatasetbot.telegramuser.TelegramUserCommunicationManager;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.sql.SQLException;

public class ChooseStateHandler extends StateHandler
{
    public ChooseStateHandler(
        final DatabaseManager databaseManager,
        final TelegramUserCommunicationManager telegramUserCommunicationManager)
    {
        super(
            databaseManager,
            telegramUserCommunicationManager,
            LoggerFactory.getLogger(ChooseStateHandler.class));
    }

    @Override
    protected boolean getIsExpectedCallbackQuery(
        final CallbackQuery callbackQuery)
    {
        final boolean isExpectedCallbackQuery =
            super.getIsExpectedCallbackQuery(callbackQuery);
        if(!isExpectedCallbackQuery)
        {
            return false;
        }

        final MaybeInaccessibleMessage maybeInaccessibleMessage =
            callbackQuery.getMessage();
        final Message message = (Message)maybeInaccessibleMessage;
        final String text = message.getText();
        if(!text.equals(TelegramUserCommunicationManager.MESSAGE_CHOOSE))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    protected void handleCallbackQuery(
        final CallbackQuery callbackQuery,
        final Long chatId,
        final Long userId)
        throws SQLException
    {
        try
        {
            super.handleCallbackQuery(
                callbackQuery,
                chatId,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        final boolean isExpectedCallbackQuery =
            getIsExpectedCallbackQuery(callbackQuery);
        if(!isExpectedCallbackQuery)
        {
            return;
        }

        try
        {
            super.handleCallbackQueryWithChosenAudioClass(
                callbackQuery,
                chatId,
                userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }

    @Override
    protected void handleGarbage(final Long chatId, final Long userId)
        throws SQLException
    {
        try
        {
            super.handleGarbage(chatId, userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }

        //Enter state "choose" again.
        try
        {
            super.onStartReceive(chatId, userId);
        }
        catch(final SQLException e)
        {
            throw e;
        }
    }
}
