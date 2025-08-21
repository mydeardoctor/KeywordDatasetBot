package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class DialogueStateMapper extends EnumMapper
{
    private DialogueStateMapper()
    {
        super();
    }

    public static DialogueState fromString(final String dialogueStateAsString)
        throws NullPointerException, IllegalArgumentException
    {
        try
        {
            final DialogueState dialogueState =
                mapFromString(dialogueStateAsString, DialogueState.class);
            return dialogueState;
        }
        catch(final NullPointerException | IllegalArgumentException e)
        {
            throw e;
        }
    }

    public static String toString(final DialogueState dialogueState)
        throws NullPointerException
    {
        final String dialogueStateAsString = mapToString(dialogueState);
        if(dialogueStateAsString != null)
        {
            return dialogueStateAsString;
        }
        else
        {
            throw new NullPointerException("DialogueState can not be null!");
        }
    }
}