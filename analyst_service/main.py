from fastapi import FastAPI
from models import (
    AnalyzeRequest, 
    AnalyzeResponse, 
    Subscription, 
    ProSuggestion,
    AnalysisMode
)
from logic import find_subscriptions, enrich_with_pro_data

app = FastAPI(
    title="StudFi Analyst Service",
    description="Анализирует банковские транзакции для поиска подписок.",
    version="1.0.0"
)

@app.get("/", tags=["Health Check"])
def read_root():
    """Простой эндпоинт для проверки работоспособности сервиса."""
    return {"status": "ok"}

@app.post("/analyze", response_model=AnalyzeResponse)
def analyze_transactions(request: AnalyzeRequest):
    """
    Анализирует транзакции, находит подписки и обогащает их данными
    в зависимости от выбранного режима (free/pro).
    """
    # Шаг 1: Найти все возможные подписки
    found_subscriptions = find_subscriptions(request.transactions)

    # Шаг 2: Обогатить данные в зависимости от режима
    enriched_subscriptions, pro_suggestions = enrich_with_pro_data(
        found_subscriptions, request.mode
    )

    # Шаг 3: Сформировать и вернуть ответ
    return AnalyzeResponse(
        subscriptions=enriched_subscriptions,
        pro_version_suggestions=pro_suggestions if request.mode == AnalysisMode.PRO else []
    )
