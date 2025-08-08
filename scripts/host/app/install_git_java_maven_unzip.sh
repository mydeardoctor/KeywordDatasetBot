#!/bin/bash

sudo \
bash << "EOF"

get_is_installed()
{
    local TARGET_PACKAGE="$1"
    dpkg-query -W -f '${db:Status-Abbrev}\n' "${TARGET_PACKAGE}" 2>/dev/null \
    | grep -q "ii"
}

get_does_command_exist()
{
    local TARGET_COMMAND="$1"
    command -v "${TARGET_COMMAND}" >/dev/null 2>&1
}

install()
{
    local TARGET_PACKAGE="$1"
    local TARGET_COMMAND="$2"

    if { ! get_is_installed "${TARGET_PACKAGE}"; } || \
       { ! get_does_command_exist "${TARGET_COMMAND}"; }; then
        echo "${TARGET_PACKAGE} is not installed, installing."
        apt-get update
        apt-get -y install "${TARGET_PACKAGE}"
    else
        echo "${TARGET_PACKAGE} is already installed, skipping."
    fi
}

echo "Running as $(whoami)."

install "git" "git"
install "openjdk-21-jdk" "java"
install "maven" "mvn"
install "unzip" "unzip"

echo "Git:"
git --version
echo "Java:"
java --version
echo "Maven:"
mvn --version

echo "Finished running as $(whoami)."

EOF

echo