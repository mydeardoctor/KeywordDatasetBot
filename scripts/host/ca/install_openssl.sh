#TODO install ca-certificates for update-certificates command

#!/bin/bash

get_is_openssl_installed()
{
  dpkg-query -W -f '${db:Status-Abbrev}\n' openssl 2>/dev/null | grep -q "ii"
}

get_does_openssl_command_exist()
{
  command -v openssl >/dev/null 2>&1
}

if ! ( get_is_openssl_installed && get_does_openssl_command_exist ); then
  echo "Openssl is not installed, installing."
  sudo apt-get update
  sudo apt-get -y install openssl
else
  echo "Openssl is already installed, skipping."
fi

openssl version
echo