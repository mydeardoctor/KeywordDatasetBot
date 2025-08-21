package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class DialogueStateMapper
{
    private DialogueStateMapper()
    {
        super();
    }

    public static DialogueState map(final String dialogueStateAsString)
    {
        return EnumMapper.map(dialogueStateAsString, DialogueState.class);
    }

    public static String map(final DialogueState dialogueState)
    {
        final String dialogueStateAsString = EnumMapper.map(dialogueState);
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