class DataHelper:
    @staticmethod
    def safe_get_nested(data, path, default=None):
        try:
            for key in path:
                if isinstance(data, list) and isinstance(key, int):
                    data = data[key]
                elif isinstance(data, dict) and key in data:
                    data = data[key]
                else:
                    return default
            return data
        except (IndexError, KeyError, TypeError):
            return default
