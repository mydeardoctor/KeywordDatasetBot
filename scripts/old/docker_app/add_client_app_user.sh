#!/bin/bash

pwd

# Check if client app user already exists.
# stdout and stderr are suppressed.
if ! id ${CLIENT_APP_USER} 1>/dev/null 2>&1; then
    echo "Creating ${CLIENT_APP_USER} user."

    addgroup \
    --system \
    --gid "${CLIENT_APP_USER_GID}" \
    "${CLIENT_APP_USER}"

    adduser \
    --system \
    --uid "${CLIENT_APP_USER_UID}" \
    --gid "${CLIENT_APP_USER_GID}" \
    --comment "\"Keyword dataset bot\" client application" \
    --home "${CLIENT_APP_USER_HOME}" \
    --shell /bin/bash \
    "${CLIENT_APP_USER}"
else
    echo "${CLIENT_APP_USER} user already exists, skipping."
fi

#TODO fail if create user fails
#TODO CHECK UID AND GID of user! if they are not what i want EXIT WITH ERROR

# Change client app's HOME directory permission.
echo "Changing mode of ${CLIENT_APP_USER_HOME} to 700."
chown ${CLIENT_APP_USER}:${CLIENT_APP_USER} ${CLIENT_APP_USER_HOME}
chmod 700 "${CLIENT_APP_USER_HOME}"

chown -R ${CLIENT_APP_USER}:${CLIENT_APP_USER} "${CLIENT_APP_USER_HOME}/KeywordDatasetBot"
chmod 700 "${CLIENT_APP_USER_HOME}/KeywordDatasetBot"

# Show user info.
getent passwd "${CLIENT_APP_USER}"
# Show user group.
id "${CLIENT_APP_USER}"
# Show user password status.
passwd -S "${CLIENT_APP_USER}"
# Show client app's HOME directory permissions.
ls -ld "${CLIENT_APP_USER_HOME}"

pwd
ls -al .

cd KeywordDatasetBot
ls -al
cd ..
#TODO chmod of jar and folders

exec gosu ${CLIENT_APP_USER} java -jar ./KeywordDatasetBot/KeywordDatasetBot_v0.0.1_afd30f7e.jar