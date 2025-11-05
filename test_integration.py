import requests
import json

# --- Настройки ---
# URL Java-сервиса, который отдает все транзакции
JAVA_BACKEND_URL = "http://localhost:8080/api/v1/transactions"

# URL Python-сервиса, который принимает транзакции для анализа
ANALYST_SERVICE_URL = "http://127.0.0.1:8000/analyze"

def run_full_analysis(mode: str = "pro"):
    """
    Выполняет полный цикл анализа:
    1. Запрашивает транзакции у Java-бэкенда.
    2. Отправляет их в сервис аналитики (FastAPI).
    3. Печатает результат.

    :param mode: Режим анализа ("free" или "pro").
    """
    print("--- Шаг 1: Получение транзакций из Java-бэкенда ---")
    try:
        response = requests.get(JAVA_BACKEND_URL, timeout=20)
        # Проверяем, что запрос успешен (код 200)
        response.raise_for_status()
        transactions = response.json()
        print(f"Успешно получено {len(transactions)} транзакций.")
    except requests.exceptions.RequestException as e:
        print(f"\n!!! ОШИБКА: Не удалось подключиться к Java-бэкенду по адресу {JAVA_BACKEND_URL}")
        print(f"Убедитесь, что Java-сервер запущен и отвечает.")
        print(f"Детали ошибки: {e}")
        return

    print("\n--- Шаг 2: Отправка транзакций в сервис аналитики (FastAPI) ---")
    
    # Готовим данные для POST-запроса в соответствии с моделью AnalyzeRequest
    payload = {
        "transactions": transactions,
        "mode": mode
    }

    try:
        response = requests.post(ANALYST_SERVICE_URL, json=payload, timeout=10)
        response.raise_for_status()
        analysis_result = response.json()
        print("Анализ успешно завершен!")
    except requests.exceptions.RequestException as e:
        print(f"\n!!! ОШИБКА: Не удалось подключиться к сервису аналитики по адресу {ANALYST_SERVICE_URL}")
        print(f"Убедитесь, что FastAPI-сервер (uvicorn) запущен и отвечает.")
        print(f"Детали ошибки: {e}")
        return

    print("\n--- РЕЗУЛЬТАТ АНАЛИЗА ---")
    # Печатаем результат в красивом отформатированном виде
    print(json.dumps(analysis_result, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    # Запускаем анализ в "pro" режиме, чтобы увидеть все детали
    run_full_analysis(mode="pro")
    
    # Если хотите протестировать бесплатный режим, измените на:
    # run_full_analysis(mode="free")
