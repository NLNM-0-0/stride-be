#!bash

source "D:\stride-be\z-etc\bash\config-paths.env"

declare -A services=(
    ["gateway/application.yml"]="api-gateway/src/main/resources/application.yml"
    ["gateway/application-dev.yml"]="api-gateway/src/main/resources/application-dev.yml"
    ["gateway/application-prod.yml"]="api-gateway/src/main/resources/application-prod.yml"

    ["bridge/application.yml"]="bridge-service/src/main/resources/application.yml"
    ["bridge/application-dev.yml"]="bridge-service/src/main/resources/application-dev.yml"
    ["bridge/application-prod.yml"]="bridge-service/src/main/resources/application-prod.yml"
    ["bridge/firebase-secret.json"]="bridge-service/src/main/resources/firebase-secret.json"
    ["bridge/logback-spring.xml"]="bridge-service/src/main/resources/logback-spring.xml"

    ["core/application.yml"]="core-service/src/main/resources/application.yml"
    ["core/application-dev.yml"]="core-service/src/main/resources/application-dev.yml"
    ["core/application-prod.yml"]="core-service/src/main/resources/application-prod.yml"
    ["core/logback-spring.xml"]="core-service/src/main/resources/logback-spring.xml"

    ["identity/application.yml"]="identity-service/src/main/resources/application.yml"
    ["identity/application-dev.yml"]="identity-service/src/main/resources/application-dev.yml"
    ["identity/application-prod.yml"]="identity-service/src/main/resources/application-prod.yml"
    ["identity/logback-spring.xml"]="identity-service/src/main/resources/logback-spring.xml"

    ["profile/application.yml"]="profile-service/src/main/resources/application.yml"
    ["profile/application-dev.yml"]="profile-service/src/main/resources/application-dev.yml"
    ["profile/application-prod.yml"]="profile-service/src/main/resources/application-prod.yml"
    ["profile/logback-spring.xml"]="profile-service/src/main/resources/logback-spring.xml"

    ["route/.env.default"]="route-service/deploy/.env.default"
    ["route/.env.dev"]="route-service/deploy/.env.dev"
    ["route/.env.prod"]="route-service/deploy/.env.prod"

    ["grafana-agent/grafana-agent.yml"]="z-etc/log/grafana-agent.yml"

    [".env"]=".env"
)

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
