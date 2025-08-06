#!/bin/bash

psql        \
-U postgres \
-d postgres \
-f /home/create_database_roles_and_schema.sql \
-v client_app_role="${CLIENT_APP_ROLE}" \
-v client_app_password="${CLIENT_APP_PASSWORD}" \
-v database_name="${DATABASE_NAME}"