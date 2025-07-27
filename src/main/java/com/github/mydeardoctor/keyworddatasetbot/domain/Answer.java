package com.github.mydeardoctor.keyworddatasetbot.domain;

public enum Answer
{
    YES("yes / да"),
    NO("no / нет");

    private final String humanReadableName;

    private Answer(final String humanReadableName)
    {
        this.humanReadableName = humanReadableName;
    }

    @Override
    public String toString()
    {
        return humanReadableName;
    }
}