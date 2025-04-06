#!bash

declare -A services=(
    ["gateway/application.yml"]="api-gateway/src/main/resources/application.yml"
    ["file/application.yml"]="file-service/src/main/resources/application.yml"
    ["identity/application.yml"]="identity-service/src/main/resources/application.yml"
    ["notification/application.yml"]="notification-service/src/main/resources/application.yml"
    ["profile/application.yml"]="profile-service/src/main/resources/application.yml"
    ["gateway/application-dev.yml"]="api-gateway/src/main/resources/application-dev.yml"
    ["file/application-dev.yml"]="file-service/src/main/resources/application-dev.yml"
    ["identity/application-dev.yml"]="identity-service/src/main/resources/application-dev.yml"
    ["notification/application-dev.yml"]="notification-service/src/main/resources/application-dev.yml"
    ["profile/application-dev.yml"]="profile-service/src/main/resources/application-dev.yml"
    [".env"]=".env"
)

CONFIG_DIR="D:/stride-config/backend"
SOURCE_DIR="D:/stride-be"

# Copy files from SOURCE_DIR to CONFIG_DIR
for dest_file in "${!services[@]}"; do
    SOURCE_PATH="$SOURCE_DIR/${services[$dest_file]}"
    DEST_PATH="$CONFIG_DIR/$dest_file"

    if [ -f "$DEST_PATH" ]; then
        mkdir -p "$(dirname "$SOURCE_PATH")"

        cp "$DEST_PATH" "$SOURCE_PATH"
        echo "File copied from $DEST_PATH to $SOURCE_PATH"
    else
        echo "Source file $DEST_PATH does not exist. Skipping..."
    fi
done

echo "All selected files copied successfully!"
