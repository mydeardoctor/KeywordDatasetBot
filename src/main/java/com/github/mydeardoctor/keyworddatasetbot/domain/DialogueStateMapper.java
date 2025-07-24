package com.github.mydeardoctor.keyworddatasetbot.domain;

public class DialogueStateMapper
{
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
}