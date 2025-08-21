package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class DialogueStateMapper extends EnumMapper
{
    private DialogueStateMapper()
    {
        super();
    }

    public static DialogueState fromString(final String dialogueStateAsString)
        throws NullPointerException
    {
        try
        {
            final DialogueState dialogueState =
                mapFromString(dialogueStateAsString, DialogueState.class);
            return dialogueState;
        }
        catch(final NullPointerException e)
        {
            throw e;
        }
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