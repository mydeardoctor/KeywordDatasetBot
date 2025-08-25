package com.github.mydeardoctor.keyworddatasetbot.resources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class SqlLoader extends ResourceLoader
{
    private static final String SQL_EXTENSION = ".sql";

    private SqlLoader()
    {
        super();
    }

    public static Map<String, String> loadSqls(
        final String sqlDirectoryPath,
        final Set<String> sqlFileNames)
        throws IOException, IllegalArgumentException
    {
        final Map<String, String> sqls = new HashMap<>();

        for(final String sqlFileName : sqlFileNames)
        {
            final String sqlFileNameWithExtension = sqlFileName + SQL_EXTENSION;
            final String sqlFilePath = Path
                .of(sqlDirectoryPath)
                .resolve(sqlFileNameWithExtension)
                .toString();

            String sql = null;

            try
            {
                sql = loadString(sqlFilePath);
            }
            catch(final IOException | IllegalArgumentException e)
            {
                throw e;
            }

            sqls.put(sqlFileName, sql);
        }

        return sqls;
    }
}
