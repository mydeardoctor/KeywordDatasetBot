package com.github.mydeardoctor.keyworddatasetbot.domain;

public class DialogueStateMapper
{
    private DialogueStateMapper()
    {
        super();
    }

    public static DialogueState map(final String dialogueStateAsString)
    {
        if(dialogueStateAsString != null)
        {
            return DialogueState.valueOf(dialogueStateAsString.toUpperCase());
        }
        else
        {
            return null;
        }
    }

    public static String map(final DialogueState dialogueState)
    {
        if(dialogueState != null)
        {
            return dialogueState.name().toLowerCase();
        }
        else
        {
            throw new NullPointerException("dialogue state can not be null!");
        }
    }
}