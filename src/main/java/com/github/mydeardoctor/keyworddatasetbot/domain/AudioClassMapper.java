package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class AudioClassMapper
{
    private AudioClassMapper()
    {
        super();
    }

    public static AudioClass map(final String audioClassAsString)
    {
        return EnumMapper.map(audioClassAsString, AudioClass.class);
    }

    public static String map(final AudioClass audioClass)
    {
        return EnumMapper.map(audioClass);
    }
}
