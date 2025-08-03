#!/bin/bash

# Check if client app user already exists.
# stdout and stderr are suppressed.
if ! id ${CLIENT_APP_USER} 1>/dev/null 2>&1; then
    echo "Creating ${CLIENT_APP_USER} user."

    sudo adduser \
    --system \
    --comment "\"Keyword dataset bot\" client application" \
    --group \
    --home "${CLIENT_APP_USER_HOME}" \
    --shell /bin/bash \
    "${CLIENT_APP_USER}"
else
    echo "${CLIENT_APP_USER} user already exists, skipping."
fi

# Change client app's HOME directory permission.
echo "Changing mode of ${CLIENT_APP_USER_HOME} to 700."
sudo chmod 700 "${CLIENT_APP_USER_HOME}"

# Show user info.
getent passwd "${CLIENT_APP_USER}"
# Show user group.
id "${CLIENT_APP_USER}"
# Show user password status.
sudo passwd -S "${CLIENT_APP_USER}"
# Show client app's HOME directory permissions.
ls -ld "${CLIENT_APP_USER_HOME}"


# Run as client app user.
sudo -u "${CLIENT_APP_USER}" \
env \
CLIENT_APP_AUDIO_DIRECTORY="${CLIENT_APP_AUDIO_DIRECTORY}" \
bash << "EOF"

echo "Running as $(whoami) user."

echo "Changing directory to ${HOME}"
cd "${HOME}"

if [ ! -d "${CLIENT_APP_AUDIO_DIRECTORY}" ]; then
    # Create client app's audio directory.
    echo "Creating ${CLIENT_APP_AUDIO_DIRECTORY}"
    mkdir "${CLIENT_APP_AUDIO_DIRECTORY}"
fi

# Change client app's audio directory permission.
echo "Changing mode of ${CLIENT_APP_AUDIO_DIRECTORY} to 700."
chmod 700 "${CLIENT_APP_AUDIO_DIRECTORY}"

echo "Finished running as $(whoami) user."

EOF