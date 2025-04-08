#!bash

source ./config-paths.env

declare -A services=(
    ["gateway/application.yml"]="api-gateway/src/main/resources/application.yml"
    ["gateway/application-dev.yml"]="api-gateway/src/main/resources/application-dev.yml"

    ["file/application.yml"]="file-service/src/main/resources/application.yml"
    ["file/application-dev.yml"]="file-service/src/main/resources/application-dev.yml"

    ["identity/application.yml"]="identity-service/src/main/resources/application.yml"
    ["identity/application-dev.yml"]="identity-service/src/main/resources/application-dev.yml"

    ["notification/application.yml"]="notification-service/src/main/resources/application.yml"
    ["notification/application-dev.yml"]="notification-service/src/main/resources/application-dev.yml"
    ["notification/firebase-secret.json"]="notification-service/src/main/resources/firebase-secret.json"

    ["profile/application.yml"]="profile-service/src/main/resources/application.yml"
    ["profile/application-dev.yml"]="profile-service/src/main/resources/application-dev.yml"

    [".env"]=".env"
)

CONFIG_DIR="D:/stride-config/backend"
SOURCE_DIR="D:/stride-be"

# Copy files
for dest_file in "${!services[@]}"; do
    SOURCE_PATH="$SOURCE_DIR/${services[$dest_file]}"
    DEST_PATH="$CONFIG_DIR/$dest_file"

    if [ -f "$SOURCE_PATH" ]; then
        # Create the directory if it doesn't exist
        mkdir -p "$(dirname "$DEST_PATH")"

        # Copy the file
        cp "$SOURCE_PATH" "$DEST_PATH"
        echo "File copied from $SOURCE_PATH to $DEST_PATH"
    else
        echo "Source file $SOURCE_PATH does not exist. Skipping..."
    fi
done

echo "All selected files copied successfully!"
