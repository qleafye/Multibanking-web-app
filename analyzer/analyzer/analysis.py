from config import SUBSCRIPTION_KEYWORDS, INCOME_KEYWORDS


def find_subscriptions(transactions: list[dict]) -> dict:
    """
    Анализирует список транзакций и находит подписки.
    Сначала отфильтровывает явные доходы, а затем ищет повторяющиеся траты.
    """
    payment_counts = {}

    for tx in transactions:
        desc = tx.get("description")
        amount = abs(tx.get("amount", 0))  # Сумму всегда берем по модулю

        if not desc:
            continue

        # --- НОВАЯ ЛОГИКА: Проверяем, не доход ли это ---
        is_income = any(keyword.lower() in desc.lower() for keyword in INCOME_KEYWORDS)
        if is_income:
            continue  # Если это доход, пропускаем и идем к следующей транзакции

        # --- Старая логика для подсчета трат ---
        if desc in payment_counts:
            payment_counts[desc]["count"] += 1
            payment_counts[desc]["total_amount"] += amount
        else:
            payment_counts[desc] = {
                "count": 1,
                "total_amount": amount,
                "monthly_amount": amount
            }

    # ... остальная часть функции остается без изменений ...
    found_subscriptions_data = {}
    for name, data in payment_counts.items():
        is_recurring = data["count"] > 1
        is_keyword_match = any(keyword.lower() in name.lower() for keyword in SUBSCRIPTION_KEYWORDS)
        if is_recurring or is_keyword_match:
            found_subscriptions_data[name] = data

    result_list = [
        {
            "name": name,
            "monthly_amount": round(data["monthly_amount"], 2),
            "total_spent": round(data["total_amount"], 2),
            "transactions_count": data["count"]
        }
        for name, data in found_subscriptions_data.items()
    ]

    total_monthly_cost = sum(sub["monthly_amount"] for sub in result_list)

    return {
        "subscriptions": result_list,
        "total_monthly_cost": round(total_monthly_cost, 2)
    }