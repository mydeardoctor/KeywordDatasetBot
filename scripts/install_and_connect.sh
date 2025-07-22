bash ./installation/install.sh

source .env

cd ./database_roles_and_schema
bash ./create_database_roles_and_schema.sh
cd ..

bash ./ssl/ca/add_ca_admin_user.sh
bash ./ssl/ca/generate_ca.sh

bash ./ssl/database_server/generate_database_server_csr.sh
bash ./ssl/ca/sign_database_servers_csr.sh

bash ./ssl/client_app/add_client_app_user.sh
bash ./ssl/client_app/generate_client_app_csr.sh
bash ./ssl/ca/sign_client_apps_csr.sh

bash ./ssl/database_server/edit_postgresql_configuration.sh

bash ./connection/connect.sh