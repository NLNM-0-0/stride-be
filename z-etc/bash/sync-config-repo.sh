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
    ["core/ehcache.xml"]="core-service/src/main/resources/ehcache.xml"

    ["identity/application.yml"]="identity-service/src/main/resources/application.yml"
    ["identity/application-dev.yml"]="identity-service/src/main/resources/application-dev.yml"
    ["identity/application-prod.yml"]="identity-service/src/main/resources/application-prod.yml"
    ["identity/logback-spring.xml"]="identity-service/src/main/resources/logback-spring.xml"

    ["profile/application.yml"]="profile-service/src/main/resources/application.yml"
    ["profile/application-dev.yml"]="profile-service/src/main/resources/application-dev.yml"
    ["profile/application-prod.yml"]="profile-service/src/main/resources/application-prod.yml"
    ["profile/logback-spring.xml"]="profile-service/src/main/resources/logback-spring.xml"

    ["metric/application.yml"]="metric-service/src/main/resources/application.yml"
    ["metric/application-dev.yml"]="metric-service/src/main/resources/application-dev.yml"
    ["metric/application-prod.yml"]="metric-service/src/main/resources/application-prod.yml"
    ["metric/logback-spring.xml"]="metric-service/src/main/resources/logback-spring.xml"

    ["grafana-agent/grafana-agent.yml"]="/z-etc/log/grafana-agent.yml"

    [".env"]=".env"
)

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
