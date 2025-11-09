# logic.py (ИСПРАВЛЕННАЯ ВЕРСИЯ)

import json
from typing import List
from models import TransactionInput, Subscription, PotentialSaving, ProSuggestion, AnalysisMode
import os

_DIR = os.path.dirname(os.path.abspath(__file__))
KNOWLEDGE_BASE_PATH = os.path.join(_DIR, 'knowledge_base.json')

with open(KNOWLEDGE_BASE_PATH, 'r', encoding='utf-8') as f:
    KNOWLEDGE_BASE = json.load(f)


def find_subscriptions(transactions: List[TransactionInput]) -> List[Subscription]:
    """
    Находит ВСЕ транзакции, похожие на подписки, без запоминания.
    """
    found_subscriptions = []

    # Проходим по каждой транзакции из 200
    for t in transactions:
        # И ищем для нее совпадение в нашей базе знаний
        for keyword, sub_data in KNOWLEDGE_BASE.items():
            if keyword.lower() in t.description.lower():
                # Если совпадение найдено, СРАЗУ создаем объект подписки
                found_subscriptions.append(
                    Subscription(
                        name=sub_data.get('name', 'Unknown'),
                        logo_url=sub_data.get('logo_url', ''),
                        monthly_cost=abs(t.amount),  # Используем реальную сумму и берем ее по модулю
                        date=t.date,
                        potential_savings=None
                    )
                )
                # И переходим к следующей транзакции, чтобы не добавить одну и ту же дважды
                break

    return found_subscriptions



def enrich_with_pro_data(subscriptions: List[Subscription], mode: AnalysisMode) -> tuple[
    List[Subscription], List[ProSuggestion]]:
    if mode == AnalysisMode.FREE:
        return subscriptions, []

    pro_suggestions = []

  
    # Создаем множество для отслеживания уже добавленных советов по названию подписки
    added_suggestions_for = set()

    for sub in subscriptions:
        # Проверяем, не давали ли мы уже совет для этой подписки
        if sub.name in added_suggestions_for:
            continue  # Если да, переходим к следующей подписке

        keyword = next((k for k, v in KNOWLEDGE_BASE.items() if v.get("name") == sub.name), None)
        if keyword:
            service_info = KNOWLEDGE_BASE[keyword]

            # Логика для potential_savings (остается для всех экземпляров подписки)
            if "alternatives" in service_info and service_info["alternatives"]:
                alternative = service_info["alternatives"][0]
                saving = sub.monthly_cost - alternative.get("cost", 0)
                if saving > 0:
                    sub.potential_savings = PotentialSaving(
                        saving_amount=round(saving, 2)
                    )

            # Логика для советов (теперь с проверкой на дубликаты)
            if "hacks" in service_info and service_info["hacks"]:
                hack = service_info["hacks"][0]
                pro_suggestions.append(ProSuggestion(
                    name=sub.name,
                    cost=sub.monthly_cost,
                    suggestion=hack.get("pitch", "")
                ))
                # Запоминаем, что для этой подписки мы уже добавили совет
                added_suggestions_for.add(sub.name)

    return subscriptions, pro_suggestions