import enum


class Environment(str, enum.Enum):
    DEVELOPMENT = "DEV"
    STAGING = "STG"
    PRODUCTION = "PROD"