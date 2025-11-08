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
    Подписка считается найденной, если есть хотя бы один платеж,
    совпадающий с ключевым словом из нашей базы знаний.
    """
    found_subscriptions = []
    
    # Используем set для хранения уже найденных подписок, чтобы избежать дублей
    processed_keywords = set()

    for t in transactions:
        for keyword, sub_data in KNOWLEDGE_BASE.items():
            # Проверяем, что ключевое слово есть в описании и мы еще не добавляли эту подписку
            if keyword.lower() in t.description.lower() and keyword not in processed_keywords:
                
                # Создаем базовый объект подписки
                found_subscriptions.append(
                    Subscription(
                        name=sub_data['name'],
                        amount=t.amount, # Берем реальную сумму из транзакции
                        last_payment_date=t.date # И реальную дату
                    )
                )
                # Добавляем ключевое слово в обработанные, чтобы не искать его снова
                processed_keywords.add(keyword)
                # Переходим к следующей транзакции
                break
            
    return found_subscriptions

def enrich_with_pro_data(subscriptions: List[Subscription], mode: AnalysisMode) -> tuple[List[Subscription], List[ProSuggestion]]:
    """
    Обогащает найденные подписки данными из Pro-версии (экономия, советы).
    В 'free' режиме возвращает пустые списки.
    """
    # Если режим бесплатный, просто возвращаем как есть
    if mode == AnalysisMode.FREE:
        return subscriptions, []

    # Логика Pro-режима
    pro_suggestions = []
    
    for sub in subscriptions:
        # Ищем информацию о подписке в базе знаний по ее имени
        # Нам нужно найти ключ ("YANDEX.PLUS"), зная значение ("Яндекс.Плюс")
        keyword = next((k for k, v in KNOWLEDGE_BASE.items() if v["name"] == sub.name), None)
        
        if keyword:
            service_info = KNOWLEDGE_BASE[keyword]
            # Добавляем информацию о возможной экономии (берем первую альтернативу)
            if "alternatives" in service_info and service_info["alternatives"]:
                alternative = service_info["alternatives"][0]
                saving = sub.amount - alternative["cost"]
                if saving > 0:
                    sub.potential_savings = PotentialSaving(
                        service_name=alternative["name"],
                        saving_amount=round(saving, 2)
                    )
            
            # Добавляем Pro-советы (берем первый лайфхак)
            if "hacks" in service_info and service_info["hacks"]:
                hack = service_info["hacks"][0]
                pro_suggestions.append(ProSuggestion(
                    service_name=sub.name,
                    suggestion=hack["pitch"]
                ))
        
    return subscriptions, pro_suggestions

