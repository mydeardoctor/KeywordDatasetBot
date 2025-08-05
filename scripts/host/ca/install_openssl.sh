#!/bin/bash

sudo \
bash << "EOF"

get_is_openssl_installed()
{
    dpkg-query -W -f '${db:Status-Abbrev}\n' openssl 2>/dev/null | grep -q "ii"
}

get_does_openssl_command_exist()
{
    command -v openssl >/dev/null 2>&1
}

echo "Running as $(whoami)."

if { ! get_is_openssl_installed; } || \
   { ! get_does_openssl_command_exist; }; then
    echo "Openssl is not installed, installing."
    apt-get update
    apt-get -y install openssl
else
    echo "Openssl is already installed, skipping."
fi

openssl version

echo "Finished running as $(whoami)."

EOF

echo