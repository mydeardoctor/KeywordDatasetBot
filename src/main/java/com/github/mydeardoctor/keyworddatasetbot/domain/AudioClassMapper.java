package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AudioClassMapper extends EnumMapper
{
    private AudioClassMapper()
    {
        super();
    }

    public static AudioClass fromString(final String audioClassAsString)
    {
        return mapFromString(audioClassAsString, AudioClass.class);
    }

    //TODO может быть null
    public static String toString(final AudioClass audioClass)
    {
        return mapToString(audioClass);
    }
}
