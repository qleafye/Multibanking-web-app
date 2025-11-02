import requests
from config import JAVA_API_TRANSACTIONS_URL


def fetch_transactions_from_java_api() -> (list, str | None):
    """
    Подключается к Java-сервису и забирает список транзакций.

    Возвращает:
        tuple: (список_транзакций, None) в случае успеха.
        tuple: (None, сообщение_об_ошибке) в случае неудачи.
    """
    print(f"Подключаемся к Java API по адресу: {JAVA_API_TRANSACTIONS_URL}")
    try:
        response = requests.get(JAVA_API_TRANSACTIONS_URL, timeout=5)  # таймаут 5 секунд
        response.raise_for_status()  # Вызовет ошибку, если статус не 2xx

        transactions = response.json()
        print(f"Успешно получено {len(transactions)} транзакций.")
        return transactions, None
    except requests.exceptions.RequestException as e:
        error_message = f"Не удалось получить данные от Java-сервиса: {e}"
        print(error_message)
        return None, error_message