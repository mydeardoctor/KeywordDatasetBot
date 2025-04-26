package com.github.mydeardoctor.doctordatasetbot.properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PropertiesManager
{
    private final Properties properties;
    private final Pattern pattern;
    private final Logger logger;

    public PropertiesManager(final String pathToPropertiesFile)
        throws IOException
    {
        properties = new Properties();
        /* If the path begins with '/',
        then it is the absolute path relative to the root of the classpath. */
        try(final InputStream inputStream =
                getClass().getResourceAsStream(pathToPropertiesFile))
        {
            properties.load(inputStream);
        }

        pattern = Pattern.compile("^\\$(?:([A-Z_][A-Z_\\d]*)|\\{([A-Z_][A-Z_\\d]*)\\})$");

        logger = LoggerFactory.getLogger(PropertiesManager.class);
    }

    public String getProperty(final String key)
    {
        //Try to get a property.
        final String property = properties.getProperty(key);
        if(property == null)
        {
            final String errorMessage =
                new StringBuilder()
                    .append("Property \"")
                    .append(key)
                    .append("\" does not exist!")
                    .toString();
            throw new NoSuchElementException(errorMessage);
        }

        //Check if a property is an environment variable.
        final Matcher matcher = pattern.matcher(property);
        final boolean result = matcher.matches();
        if(result)
        {
            final String environmentVariableName1 = matcher.group(1);
            final String environmentVariableName2 = matcher.group(2);
            if((environmentVariableName1 != null) ||
               (environmentVariableName2 != null))
            {
                final String environmentVariableName;
                if(environmentVariableName1 != null)
                {
                    environmentVariableName = environmentVariableName1;
                }
                else
                {
                    environmentVariableName = environmentVariableName2;
                }
                final String environmentVariable =
                    System.getenv(environmentVariableName);
                if(environmentVariable != null)
                {
                    return environmentVariable;
                }
                else
                {
                    final String errorMessage =
                        new StringBuilder()
                            .append("Environment variable \"")
                            .append(environmentVariableName)
                            .append("\" does not exist!")
                            .toString();
                    throw new NoSuchElementException(errorMessage);
                }
            }
        }

        //If a property is not an environment variable.
        return property;
    }
}