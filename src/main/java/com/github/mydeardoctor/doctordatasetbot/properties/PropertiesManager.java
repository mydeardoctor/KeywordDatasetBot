package com.github.mydeardoctor.doctordatasetbot.properties;


import com.sun.jdi.InternalException;
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

        //TODO new pattern. train regex
        //TODO сделать regex чтобы можно было делат ${asdsad} или $asdasd. Пишут ли так енв вар в реальности?
        //TODO backslashes in regex?
        //TODO train regex
        pattern = Pattern.compile("^\\$\\{(\\w+)\\}$");

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
            final int groupCount = matcher.groupCount();
            if(groupCount >= 1)
            {
                final String environmentVariableName = matcher.group(1);
                if(environmentVariableName != null)
                {
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
        }

        //If a property is not an environment variable.
        return property;
    }
}