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
    protected boolean getIsExpectedCallbackQuery(Update update)
    {
        final boolean isExpectedCallbackQuery =
            super.getIsExpectedCallbackQuery(update);
        if(!isExpectedCallbackQuery)
        {
            return false;
        }

        final CallbackQuery callbackQuery = update.getCallbackQuery();
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
    protected boolean getIsExpectedVoice(Update update)
    {
        return false;
    }

    @Override
    protected void handleExpectedCallbackQuery(
        final CallbackQuery callbackQuery,
        final Long chatId,
        final Long userId)
        throws SQLException
    {
        super.handleExpectedCallbackQuery(
            callbackQuery,
            chatId,
            userId);

        final String audioClassAsString = callbackQuery.getData();
        final AudioClass audioClass = AudioClassMapper.map(audioClassAsString);
        if(audioClass == null)
        {
            return;
        }

        //Send message to telegram user.
        telegramUserCommunicationManager.sendMessage(
            chatId,
            TelegramUserCommunicationManager.MESSAGE_RECORD,
            null,
            null);

        //Change state.
        try
        {
            databaseManager.updateDialogueStateAndAudioClass(
                userId,
                DialogueState.RECORD,
                audioClass);
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
        super.handleGarbage(chatId, userId);

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
