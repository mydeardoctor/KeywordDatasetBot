package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class DialogueStateMapper extends EnumMapper
{
    private DialogueStateMapper()
    {
        super();
    }

    public static DialogueState fromString(final String dialogueStateAsString)
    {
        return mapFromString(dialogueStateAsString, DialogueState.class);
    }

    public static String toString(final DialogueState dialogueState)
    {
        final String dialogueStateAsString = mapToString(dialogueState);
        if(dialogueStateAsString != null)
        {
            return dialogueStateAsString;
        }
        else
        {
            throw new NullPointerException("dialogue state can not be null!");
        }
    }
}