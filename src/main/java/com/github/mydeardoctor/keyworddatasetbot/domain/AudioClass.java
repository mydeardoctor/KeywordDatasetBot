package com.github.mydeardoctor.keyworddatasetbot.domain;

public enum AudioClass
{
    DOCTOR("doctor / доктор"),
    SAMEHADA("samehada / самехада");

    private final String humanReadableName;

    private AudioClass(final String humanReadableName)
    {
        this.humanReadableName = humanReadableName;
    }

    @Override
    public String toString()
    {
        return humanReadableName;
    }
}