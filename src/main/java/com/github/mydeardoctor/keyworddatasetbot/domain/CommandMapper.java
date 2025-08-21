package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class CommandMapper extends EnumMapper
{
    private CommandMapper()
    {
        super();
    }

    public static Command fromString(final String commandAsString)
    {
        return mapFromString(commandAsString, Command.class);
    }

    //TODO null pointer exception
    public static String toString(final Command command)
    {
        return mapToString(command);
    }
}
