from clients.supabase_client import SupabaseClient
from configuration.manager import settings
from services.supabase_service import SupabaseService


def get_supabase_service() -> SupabaseService:
    return SupabaseService(
        supabase_client=SupabaseClient(
            base_url=settings.SUPABASE_URL,
            token=settings.SUPABASE_TOKEN
        )
    )