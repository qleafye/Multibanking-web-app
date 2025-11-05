import json
from collections import defaultdict
from typing import List
from models import TransactionInput, Subscription
import os

# --- Загрузка базы знаний ---
# Определяем абсолютный путь к файлу, чтобы избежать ошибок FileNotFoundError
_DIR = os.path.dirname(os.path.abspath(__file__))
KNOWLEDGE_BASE_PATH = os.path.join(_DIR, 'knowledge_base.json')

# Загружаем нашу "базу знаний" о подписках один раз при старте сервиса.
with open(KNOWLEDGE_BASE_PATH, 'r', encoding='utf-8') as f:
    KNOWLEDGE_BASE = json.load(f)

def find_subscriptions(transactions: List[TransactionInput]) -> List[Subscription]:
    """
    Анализирует транзакции для поиска подписок.
    Подписка считается найденной, если найдено 2 или более платежа,
    совпадающих с ключевым словом из нашей базы знаний.
    """
    potential_subscriptions = defaultdict(list)
    
    # Группируем транзакции по ключевому слову подписки
    for t in transactions:
        for keyword in KNOWLEDGE_BASE.keys():
            if keyword.lower() in t.description.lower():
                potential_subscriptions[keyword].append(t)
                break  # Переходим к следующей транзакции, как только нашли совпадение

    found_subscriptions = []
    for keyword, trans_list in potential_subscriptions.items():
        # Эвристика: подписка подтверждена, если было 2 или более платежей
        if len(trans_list) >= 2:
            sub_data = KNOWLEDGE_BASE[keyword]
            # Создаем базовый объект подписки (без Pro-данных)
            found_subscriptions.append(
                Subscription(
                    name=sub_data['name'],
                    monthly_cost=sub_data['monthly_cost'],
                    logo_url=sub_data['logo_url']
                )
            )
            
    return found_subscriptions

def enrich_with_pro_data(subscriptions: List[Subscription]) -> List[Subscription]:
    """
    Обогащает список найденных подписок Pro-данными (альтернативы и лайфхаки)
    из базы знаний. Это ядро Pro-аналитики.
    """
    for sub in subscriptions:
        # Находим соответствующее ключевое слово в базе знаний по имени подписки
        keyword = next((kw for kw, data in KNOWLEDGE_BASE.items() if data['name'] == sub.name), None)
        if keyword:
            pro_data = KNOWLEDGE_BASE[keyword]
            # Прикрепляем альтернативы и лайфхаки, если они существуют
            if pro_data.get('alternatives'):
                sub.alternatives = pro_data['alternatives']
            if pro_data.get('hacks'):
                sub.hacks = pro_data['hacks']
    
    return subscriptions

def calculate_potential_savings(subscriptions: List[Subscription]) -> float:
    """
    Рассчитывает общую потенциальную экономию от всех лайфхаков для найденных подписок.
    Это значение используется как тизер в сводке для всех режимов.
    """
    total_savings = 0.0
    for sub in subscriptions:
        keyword = next((kw for kw, data in KNOWLEDGE_BASE.items() if data['name'] == sub.name), None)
        if keyword:
            hacks = KNOWLEDGE_BASE[keyword].get('hacks', [])
            for hack in hacks:
                total_savings += hack.get('saving_per_month', 0.0)
    return round(total_savings, 2)
