from pydantic import BaseModel, Field
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
    """Модель запроса на анализ транзакций."""
    transactions: list[TransactionInput]
    # Добавляем поле для режима с дефолтным значением "free"
    mode: AnalysisMode = AnalysisMode.FREE

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

class PotentialSaving(BaseModel):
    """Модель для потенциальной экономии."""
    saving_amount: float  # Сколько можно сэкономить

class Subscription(BaseModel):
    """Модель найденной подписки."""
    name: str  # Человекочитаемое название (например, "Яндекс.Плюс")
    monthly_cost: float  # Стоимость подписки в месяц
    logo_url: str  # URL логотипа сервиса
    # Делаем это поле опциональным, чтобы его не было в free-режиме
    potential_savings: PotentialSaving | None = None

class ProSuggestion(BaseModel):
    """Модель для предложения Pro-версии."""
    name: str  # Название сервиса
    cost: float  # Стоимость
    suggestion: str  # Короткое продающее описание

class Summary(BaseModel):
    """Сводная информация по результатам анализа."""
    total_subscriptions_found: int  # Сколько всего подписок найдено
    total_monthly_cost: float  # Общая стоимость всех подписок в месяц
    potential_savings_pro: float  # Потенциальная экономия (показывается даже в free-режиме как тизер)

class AnalyzeResponse(BaseModel):
    """Модель ответа с результатами анализа."""
    subscriptions: list[Subscription]
    # Делаем это поле опциональным, чтобы его не было в free-режиме
    pro_version_suggestions: list[ProSuggestion] | None = None
