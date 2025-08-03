#!/bin/bash

# Check if ca_admin user already exists.
# stdout and stderr are suppressed.
if ! id "${CA_ADMIN}" 1>/dev/null 2>&1; then
    echo "Creating ${CA_ADMIN} user."

    sudo adduser \
    --system \
    --comment "Local root CA admin" \
    --group \
    --home "${CA_ADMIN_HOME}" \
    --shell /bin/bash \
    "${CA_ADMIN}"
else
    echo "${CA_ADMIN} user already exists, skipping."
fi

# Change ca_admin's HOME directory permission.
echo "Changing mode of ${CA_ADMIN_HOME} to 700."
sudo chmod 700 "${CA_ADMIN_HOME}"

# Show user info.
getent passwd "${CA_ADMIN}"
# Show user group.
id "${CA_ADMIN}"
# Show user password status.
sudo passwd -S "${CA_ADMIN}"
# Show ca_admin's HOME directory permissions.
ls -ld "${CA_ADMIN_HOME}"