package com.github.mydeardoctor.keyworddatasetbot.application;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.MaybeInaccessibleMessage;
import org.telegram.telegrambots.meta.api.objects.message.Message;

public abstract class UpdateUtilities
{
    private static final String VALID_CHAT_TYPE = "private";

    private UpdateUtilities()
    {
        super();
    }

    public static boolean getIsValid(final Update update)
    {
        if((update == null) ||
           ((!update.hasMessage()) && (!update.hasCallbackQuery())) ||
           ((update.hasMessage()) && (!update.getMessage().isCommand()) && (!update.getMessage().hasVoice())))
        {
            return false;
        }
        else
        {
            String chatType = "";
            if(update.hasMessage())
            {
                final Message message = update.getMessage();
                final Chat chat = message.getChat();
                chatType = chat.getType();
            }
            else if(update.hasCallbackQuery())
            {
                final CallbackQuery callbackQuery = update.getCallbackQuery();
                final MaybeInaccessibleMessage maybeInaccessibleMessage =
                    callbackQuery.getMessage();
                final Chat chat = maybeInaccessibleMessage.getChat();
                chatType = chat.getType();
            }
            if(!chatType.equals(VALID_CHAT_TYPE))
            {
                return false;
            }
            else
            {
                return true;
            }
        }
    }

    public static Long getChatId(final Update update)
    {
        final boolean isValid = getIsValid(update);
        if(!isValid)
        {
            final String errorMessage = "update is not valid!";
            throw new IllegalArgumentException(errorMessage);
        }

        if(update.hasMessage())
        {
            final Message message = update.getMessage();
            return message.getChatId();
        }
        else if(update.hasCallbackQuery())
        {
            final CallbackQuery callbackQuery = update.getCallbackQuery();
            final MaybeInaccessibleMessage maybeInaccessibleMessage =
                callbackQuery.getMessage();
            return maybeInaccessibleMessage.getChatId();
        }
        else
        {
            final String errorMessage = "update is not valid!";
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public static User getUser(final Update update)
    {
        final boolean isValid = getIsValid(update);
        if(!isValid)
        {
            final String errorMessage = "update is not valid!";
            throw new IllegalArgumentException(errorMessage);
        }

        if(update.hasMessage())
        {
            final Message message = update.getMessage();
            return message.getFrom();
        }
        else if(update.hasCallbackQuery())
        {
            final CallbackQuery callbackQuery = update.getCallbackQuery();
            return callbackQuery.getFrom();
        }
        else
        {
            final String errorMessage = "update is not valid!";
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
