from fastapi import FastAPI
from models import AnalyzeRequest, AnalyzeResponse, AnalysisMode
from logic import find_subscriptions, enrich_with_pro_data
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="StudFi Analyst Service",
    description="Анализирует банковские транзакции для поиска подписок.",
    version="1.0.0"
)


# Мы говорим серверу принимать запросы с ЛЮБОГО адреса.
# Это абсолютно безопасно для локальной разработки и идеально подходит для хакатона.
origins = ["*"]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"], # Разрешаем все методы (GET, POST, OPTIONS и т.д.)
    allow_headers=["*"], # Разрешаем все заголовки
)

@app.get("/", tags=["Health Check"])
def read_root():
    return {"status": "ok"}

@app.post("/analyze", response_model=AnalyzeResponse)
def analyze_transactions(request: AnalyzeRequest):
    found_subscriptions = find_subscriptions(request.transactions)
    enriched_subscriptions, pro_suggestions = enrich_with_pro_data(
        found_subscriptions, request.mode
    )
    return AnalyzeResponse(
        subscriptions=enriched_subscriptions,
        pro_version_suggestions=pro_suggestions if request.mode == AnalysisMode.PRO else []
    )