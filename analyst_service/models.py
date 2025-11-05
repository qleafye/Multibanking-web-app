from pydantic import BaseModel
from typing import List, Optional
from enum import Enum

# --- Входные модели (данные, которые мы ожидаем от клиента) ---

class TransactionInput(BaseModel):
    """Модель для одной банковской транзакции, поступающей на вход."""
    date: str  # Дата транзакции
    description: str  # Описание (например, "Payment for YANDEX.PLUS")
    amount: float  # Сумма транзакции

class AnalysisMode(str, Enum):
    """Перечисление для выбора режима анализа: бесплатный или Pro."""
    FREE = "free"
    PRO = "pro"

class AnalyzeRequest(BaseModel):
    """Модель для основного запроса на анализ."""
    transactions: List[TransactionInput]  # Список транзакций для анализа
    mode: AnalysisMode = AnalysisMode.FREE  # Режим по умолчанию - "free"

# --- Выходные модели (данные, которые мы возвращаем клиенту) ---

class Hack(BaseModel):
    """Модель для "лайфхака" по экономии на подписке (Pro-версия)."""
    name: str  # Название лайфхака (например, "Годовой тариф")
    saving_per_month: float  # Сколько можно сэкономить в месяц
    pitch: str  # Короткое продающее описание

class Alternative(BaseModel):
    """Модель для альтернативной (более выгодной) подписки (Pro-версия)."""
    name: str  # Название альтернативы (например, "VK Комбо")
    cost: float  # Стоимость альтернативы
    pitch: str  # Короткое продающее описание

class Subscription(BaseModel):
    """Модель для одной найденной подписки."""
    name: str  # Человекочитаемое название (например, "Яндекс.Плюс")
    monthly_cost: float  # Стоимость подписки в месяц
    logo_url: str  # URL логотипа сервиса

    # Поля для Pro-версии. `Optional` означает, что они могут отсутствовать.
    alternatives: Optional[List[Alternative]] = None
    hacks: Optional[List[Hack]] = None

class Summary(BaseModel):
    """Сводная информация по результатам анализа."""
    total_subscriptions_found: int  # Сколько всего подписок найдено
    total_monthly_cost: float  # Общая стоимость всех подписок в месяц
    potential_savings_pro: float  # Потенциальная экономия (показывается даже в free-режиме как тизер)

class AnalyzeResponse(BaseModel):
    """Основная модель ответа, которая объединяет сводку и список подписок."""
    summary: Summary
    subscriptions: List[Subscription]
