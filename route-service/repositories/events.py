import fastapi
import loguru

from repositories.database import async_mongo

def inspect_mongo_on_connect():
    loguru.logger.info("New MongoDB connection established.")
    loguru.logger.info(f"Mongo URI --- {async_mongo.mongo_uri}")


def inspect_mongo_on_close():
    loguru.logger.info("Closing MongoDB connection.")
    async_mongo.client.close()
    loguru.logger.info("MongoDB connection closed.")


async def initialize_mongo_connection(backend_app: fastapi.FastAPI):
    loguru.logger.info("MongoDB Connection --- Establishing . . .")

    inspect_mongo_on_connect()
    backend_app.state.mongo_db = async_mongo.db

    loguru.logger.info("MongoDB Connection --- Successfully Established!")


async def dispose_mongo_connection(backend_app: fastapi.FastAPI):
    loguru.logger.info("MongoDB Connection --- Disposing . . .")

    inspect_mongo_on_close()

    loguru.logger.info("MongoDB Connection --- Successfully Disposed!")