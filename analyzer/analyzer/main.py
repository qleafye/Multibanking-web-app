from flask import Flask, jsonify
from analyzer.data_client import fetch_transactions_from_java_api
from analyzer.analysis import find_subscriptions

# Создаем экземпляр веб-приложения Flask
app = Flask(__name__)


# Определяем единственный маршрут (endpoint) нашего API
@app.route('/analyze', methods=['GET'])
def analyze_endpoint():
    """
    Главная точка входа в API.
    Забирает данные из Java-сервиса, анализирует их и возвращает результат.
    """
    # 1. Получаем данные
    transactions, error = fetch_transactions_from_java_api()

    if error:
        # Если произошла ошибка при получении данных, возвращаем ошибку 502
        return jsonify({"error": error}), 502  # 502 Bad Gateway - ошибка на стороне другого сервера

    # 2. Анализируем данные
    analysis_result = find_subscriptions(transactions)

    # 3. Возвращаем успешный результат в формате JSON
    return jsonify(analysis_result)