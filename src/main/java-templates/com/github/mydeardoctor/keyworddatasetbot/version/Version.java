package com.github.mydeardoctor.keyworddatasetbot.version;

public abstract class Version
{
    public static final String GIT_COMMIT_HASH = "${git.commit.id.abbrev}";
    public static final String GIT_TAG = "${git.closest.tag.name}";

    private Version()
    {
        super();
    }
}