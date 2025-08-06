#!/bin/bash

bash ./generate_ca_crt.sh

bash ./generate_database_server_csr.sh
bash ./generate_database_admin_csr.sh
bash ./generate_app_csr.sh

bash ./sign_database_servers_csr.sh
bash ./sign_database_admins_csr.sh
#bash ./sign_app_csr.sh