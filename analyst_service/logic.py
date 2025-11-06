import json
from collections import defaultdict
from typing import List
from models import TransactionInput, Subscription, PotentialSaving, ProSuggestion, AnalysisMode
import os

# --- Загрузка базы знаний ---
# Определяем абсолютный путь к файлу, чтобы избежать ошибок FileNotFoundError
_DIR = os.path.dirname(os.path.abspath(__file__))
KNOWLEDGE_BASE_PATH = os.path.join(_DIR, 'knowledge_base.json')

# Загружаем базу знаний один раз при старте
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

def enrich_with_pro_data(subscriptions: List[Subscription], mode: AnalysisMode) -> tuple[List[Subscription], List[ProSuggestion]]:
    """
    Обогащает найденные подписки данными из Pro-версии (экономия, советы).
    В 'free' режиме возвращает пустые списки.
    """
    # Если режим бесплатный, ничего не делаем
    if mode == AnalysisMode.FREE:
        return subscriptions, []

    # Логика Pro-режима
    pro_suggestions = []
    enriched_subscriptions = []

    for sub in subscriptions:
        # Ищем информацию о подписке в базе знаний
        service_info = next((item for item in KNOWLEDGE_BASE["services"] if item["name"].lower() in sub.name.lower()), None)
        
        if service_info:
            # Добавляем информацию о возможной экономии
            if "alternative" in service_info:
                alternative = service_info["alternative"]
                saving = sub.amount - alternative["price"]
                if saving > 0:
                    sub.potential_savings = PotentialSaving(
                        service_name=alternative["name"],
                        saving_amount=round(saving, 2)
                    )
            
            # Добавляем Pro-советы
            if "pro_suggestion" in service_info:
                pro_suggestions.append(ProSuggestion(
                    service_name=sub.name,
                    suggestion=service_info["pro_suggestion"]
                ))
        
        enriched_subscriptions.append(sub)

    return enriched_subscriptions, pro_suggestions

def calculate_potential_savings(subscriptions: List[Subscription]) -> list[Subscription]:
    """
    Рассчитывает потенциальную экономию для каждой подписки.
    Эта функция больше не нужна, так как логика переехала в enrich_with_pro_data.
    Оставляем ее пустой или удаляем. Для чистоты кода лучше удалить.
    """
    # Логика перенесена
    return subscriptions
