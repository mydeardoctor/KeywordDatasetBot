package com.github.mydeardoctor.keyworddatasetbot.domain;

//TODO абстрагировать мапперы через <T>?
public class AudioClassMapper
{
    private AudioClassMapper()
    {
        super();
    }

    public static AudioClass map(final String audioClassAsString)
    {
        if(audioClassAsString != null)
        {
            return AudioClass.valueOf(audioClassAsString.toUpperCase());
        }
        else
        {
            return null;
        }
    }

    public static String map(final AudioClass audioClass)
    {
        if(audioClass != null)
        {
            return audioClass.name().toLowerCase();
        }
        else
        {
            return null;
        }
    }
}
