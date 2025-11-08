import requests
import json

# --- КОНФИГУРАЦИЯ ---
# URL Java-сервиса, который отдает транзакции
JAVA_SERVICE_URL = "http://localhost:8080/api/v1/transactions"

# URL Python-сервиса (FastAPI) для анализа
ANALYST_SERVICE_URL = "http://127.0.0.1:8000/analyze"

# --- НОВАЯ СТРОКА: ЗДЕСЬ МЫ ВЫБИРАЕМ БАНКИ ---
# Укажите нужные банки через запятую. Например: "sbank", "abank", "vbank".
# Если оставить None, будут запрошены все банки.
SELECTED_BANKS = "sbank" 

def run_integration_test():
    """
    Выполняет полный цикл интеграционного теста:
    1. Запрашивает данные у Java-сервиса.
    2. Отправляет их на анализ в Python-сервис.
    3. Выводит результат.
    """
    print("Шаг 1: Запрос транзакций от Java-сервиса...")

    try:
        # --- ИЗМЕНЕННАЯ СТРОКА: Добавляем параметры в запрос ---
        params = {"banks": SELECTED_BANKS} if SELECTED_BANKS else None
        response_java = requests.get(JAVA_SERVICE_URL, timeout=20, params=params)
        
        # Проверяем, что Java-сервис ответил успешно
        response_java.raise_for_status()
        
        transactions = response_java.json()
        print(f"  [Успех] Получено {len(transactions)} транзакций.")

    except requests.exceptions.RequestException as e:
        print(f"  [ОШИБКА] Не удалось подключиться к Java-сервису: {e}")
        return

    print("\n--- Шаг 2: Отправка транзакций в сервис аналитики (FastAPI) ---")
    
    # Готовим данные для POST-запроса в соответствии с моделью AnalyzeRequest
    payload = {
        "transactions": transactions,
        "mode": "pro"
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
    run_integration_test()
    
    # Если хотите протестировать бесплатный режим, измените на:
    # run_full_analysis(mode="free")
