package com.github.mydeardoctor.keyworddatasetbot.database;

import javax.sql.DataSource;
import java.nio.file.Path;

public abstract class DAO
{
    protected final DataSource dataSource;

    protected static final String SQL_DIRECTORY = "sql";
    protected static final String SQL_EXTENSION = ".sql";

    protected DAO(final DataSource dataSource)
    {
        super();

        this.dataSource = dataSource;
    }

    protected static String getSqlSubdirectoryPath(final String sqlSubdirectory)
    {
        final String sqlSubdirectoryPath = Path
            .of(SQL_DIRECTORY)
            .resolve(sqlSubdirectory)
            .toString();
        return sqlSubdirectoryPath;
    }
}
