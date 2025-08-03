#!/bin/bash

CA_ADMIN_USER="root"
CA_ADMIN_GROUP="root"
CA_ADMIN_HOME="/etc/ssl/ca_admin_home"
CA_ADMIN_HOME_PERMISSIONS="700"

if [ ! -d "${CA_ADMIN_HOME}" ]; then
  echo "Creating ${CA_ADMIN_HOME}" \
       "with ${CA_ADMIN_USER}:${CA_ADMIN_GROUP} ownership" \
       "and ${CA_ADMIN_HOME_PERMISSIONS} permissions."
  sudo mkdir -p -m "${CA_ADMIN_HOME_PERMISSIONS}" "${CA_ADMIN_HOME}"
else
  echo "${CA_ADMIN_HOME} already exists, skipping."

  echo "Checking ownership of ${CA_ADMIN_HOME}"
  USER=$(stat -c "%U" "${CA_ADMIN_HOME}")
  GROUP=$(stat -c "%G" "${CA_ADMIN_HOME}")
  if [ "${USER}" != "${CA_ADMIN_USER}" ] || \
     [ "${GROUP}" != "${CA_ADMIN_GROUP}" ]; then
    echo "Changing ownership of ${CA_ADMIN_HOME}" \
         "to ${CA_ADMIN_USER}:${CA_ADMIN_GROUP}"
    sudo chown "${CA_ADMIN_USER}:${CA_ADMIN_GROUP}" "${CA_ADMIN_HOME}"
  else
    echo "Ownership of ${CA_ADMIN_HOME}" \
         "is already ${CA_ADMIN_USER}:${CA_ADMIN_GROUP}, skipping."
  fi

  echo "Checking permissions of ${CA_ADMIN_HOME}"
  PERMISSIONS=$(stat -c "%a" "${CA_ADMIN_HOME}")
  if [ "${PERMISSIONS}" != "${CA_ADMIN_HOME_PERMISSIONS}" ]; then
    echo "Changing permissions of ${CA_ADMIN_HOME}" \
         "to ${CA_ADMIN_HOME_PERMISSIONS}"
    sudo chmod "${CA_ADMIN_HOME_PERMISSIONS}" "${CA_ADMIN_HOME}"
  else
    echo "Permissions of ${CA_ADMIN_HOME}" \
         "are already ${CA_ADMIN_HOME_PERMISSIONS}, skipping."
  fi
fi

ls -d -l "${CA_ADMIN_HOME}"
echo