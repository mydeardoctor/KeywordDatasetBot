if ! { apt list --installed 2>/dev/null | grep -q "^postgresql-16"; }; then
    # Install Postgresql-16
    echo "Installing postgresql-16"
    sudo apt update
    sudo apt install postgresql-16
else
    echo "postgresql-16 is already installed, skipping."
fi