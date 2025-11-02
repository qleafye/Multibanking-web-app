# Настройки для подключения к нашему внутреннему Java API
JAVA_API_BASE_URL = "http://localhost:8080"
TRANSACTIONS_ENDPOINT = "/api/v1/transactions"
JAVA_API_TRANSACTIONS_URL = f"{JAVA_API_BASE_URL}{TRANSACTIONS_ENDPOINT}"

# Ключевые слова, которые с высокой вероятностью указывают на подписку
SUBSCRIPTION_KEYWORDS = [
    "YANDEX.PLUS", "VK.COM", "VK MUSIC", "SPOTIFY", "NETFLIX", "AMEDIATEKA",
    "IVI", "OKKO", "KION", "ZAVR", "YOUTUBE", "APPLE.COM/BILL", "GOOGLE"
]

# --- НОВЫЙ СПИСОК: Ключевые слова для игнорирования (доходы) ---
INCOME_KEYWORDS = ["Зарплата", "Подработка", "Бонус", "Перевод"]