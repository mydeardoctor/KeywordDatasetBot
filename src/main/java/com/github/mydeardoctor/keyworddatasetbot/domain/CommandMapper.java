package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class CommandMapper extends EnumMapper
{
    private CommandMapper()
    {
        super();
    }

    public static Command fromString(final String commandAsString)
        throws NullPointerException
    {
        try
        {
            final Command command =
                mapFromString(commandAsString, Command.class);
            return command;
        }
        catch(final NullPointerException e)
        {
            throw e;
        }
    }

    //TODO null pointer exception
    public static String toString(final Command command)
    {
        return mapToString(command);
    }
}
