package com.github.mydeardoctor.keyworddatasetbot.domain;

public abstract class CommandParser
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
                final String commandAsStringWithoutSlash =
                    commandAsString.substring(1);
                //TODO create Command mapper
                final Command command =
                    CommandMapper.fromString(commandAsStringWithoutSlash);
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
