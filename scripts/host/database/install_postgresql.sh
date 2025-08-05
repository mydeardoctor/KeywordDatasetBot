#!/bin/bash

POSTGRESQL_MAJOR_VERSION="16"

get_is_postgresql_installed()
{
    dpkg-query -W -f '${db:Status-Abbrev}\n' \
    "postgresql-${POSTGRESQL_MAJOR_VERSION}" 2>/dev/null | grep -q "ii"
}

get_does_psql_command_exist()
{
    command -v psql >/dev/null 2>&1
}

if { ! get_is_postgresql_installed; } || \
   { ! get_does_psql_command_exist; }; then
    echo "Postgresql is not installed, installing."
    sudo apt-get update
    sudo apt-get -y install "postgresql-${POSTGRESQL_MAJOR_VERSION}"
else
    echo "Postgresql is already installed, skipping."
fi

psql --version
echo