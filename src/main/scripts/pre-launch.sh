#!/bin/bash

# Define Variables
appName="LockBox"
user="${USER}"
username="${appName,,}_${user}"
temp_password=$(openssl rand -base64 12)
schemaName="lockbox_account_data"


h2_user="sa"
h2_password=""

H2_JAR_PATH="$(find ~/.m2/repository -name "h2*.jar" | head -n 1)"

java -cp "${H2_JAR_PATH}" org.h2.tools.Shell \
  -user "${h2_user}" \
  -password "${h2_password}" \
  -sql "CREATE SCHEMA IF NOT EXISTS ${schemaName};" \
  -sql "CREATE USER ${username} PASSWORD '${appName,,}_${user}_password';" \
  -sql "GRANT ALL PRIVILEGES ON SCHEMA ${schemaName} TO ${username};"

# Check if the schema and user were created successfully
if [ $? -eq 0 ]; then
    echo "Schema '${schemaName}' and user '${username}' created successfully."
else
    echo "Error creating schema and user."
fi
