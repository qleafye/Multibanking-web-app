from fastapi import FastAPI
from models import AnalyzeRequest, AnalyzeResponse, Summary, AnalysisMode
from logic import find_subscriptions, enrich_with_pro_data, calculate_potential_savings

app = FastAPI(
    title="StudFi Analyst Service",
    description="Анализирует банковские транзакции для поиска подписок.",
    version="1.0.0"
)

@app.get("/", tags=["Health Check"])
def read_root():
    """Простой эндпоинт для проверки работоспособности сервиса."""
    return {"status": "ok"}

@app.post("/analyze", response_model=AnalyzeResponse, tags=["Analysis"])
def analyze_transactions(request: AnalyzeRequest):
    """
    Принимает список транзакций и режим (free/pro).
    Возвращает детализированный анализ найденных подписок.
    """
    # Шаг 1: Найти все подписки на основе транзакций. Это общий шаг для обоих режимов.
    found_subscriptions = find_subscriptions(request.transactions)
    
    # Шаг 2: Рассчитать всю возможную экономию. Это будет использоваться как тизер в сводке.
    potential_savings = calculate_potential_savings(found_subscriptions)
    
    # Шаг 3: Если режим "pro", обогатить данные о подписках лайфхаками и альтернативами.
    if request.mode == AnalysisMode.PRO:
        found_subscriptions = enrich_with_pro_data(found_subscriptions)
        
    # Шаг 4: Создать объект сводки. `potential_savings_pro` включается во все ответы.
    summary = Summary(
        total_subscriptions_found=len(found_subscriptions),
        total_monthly_cost=round(sum(sub.monthly_cost for sub in found_subscriptions), 2),
        potential_savings_pro=potential_savings
    )
    
    # Шаг 5: Собрать и вернуть финальный объект ответа.
    return AnalyzeResponse(
        summary=summary,
        subscriptions=found_subscriptions
    )
