package com.github.mydeardoctor.keyworddatasetbot.domain;

public class CommandParser
{
    private CommandParser()
    {
        super();
    }

    public static Command parse(final String commandAsString)
        throws IllegalArgumentException
    {
        if((commandAsString != null) &&
           (commandAsString.length() > 1) &&
           (commandAsString.charAt(0) == '/'))
        {
            try
            {
                final Command command = Command.valueOf(
                    commandAsString.substring(1).toUpperCase());
                return command;
            }
            catch(final IllegalArgumentException e)
            {
                throw e;
            }
        }
        else
        {
            final String errorMessage = "Invalid command!";
            throw new IllegalArgumentException(errorMessage);
        }
    }
}
