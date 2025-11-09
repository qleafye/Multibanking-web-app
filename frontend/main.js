// main.js

// Функция, которая будет выполняться, как только загрузится HTML-страница
document.addEventListener('DOMContentLoaded', () => {
    // Находим на странице контейнер, в который мы будем добавлять карточки
    const container = document.getElementById('subscriptions-container');

    // Если мы не на странице результатов, ничего не делаем
    if (!container) {
        return;
    }

    // --- Шаг 1: Готовим "сырые" данные для анализа ---
    // Пока у нас нет формы для ввода, мы используем тестовые данные.
    // Это имитация того, что мы получили бы от вашего Java-бэкенда.
    const transactionsForAnalysis = [
        { "date": "2025-10-10", "description": "Payment for YANDEX.PLUS", "amount": 299.0 },
        { "date": "2025-10-11", "description": "Oplata uslug MTS_PREMIUM", "amount": 249.0 },
        { "date": "2025-10-12", "description": "Payment for IVI.RU", "amount": 399.0 },
        { "date": "2025-10-15", "description": "TINKOFF PRO", "amount": 299.0 }
    ];

    // --- Шаг 2: Вызываем наш Python-аналитик ---
    // Создаем асинхронную функцию, чтобы не блокировать страницу во время запроса
    async function fetchAndDisplaySubscriptions() {
        try {
            // Отправляем POST-запрос на наш Python-сервер
            const response = await fetch('http://127.0.0.1:8000/analyze', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                // Превращаем наши данные в JSON-строку для отправки
                body: JSON.stringify({
                    transactions: transactionsForAnalysis,
                    mode: "pro" // Запрашиваем PRO-режим, чтобы видеть данные об экономии
                })
            });

            // Проверяем, что сервер ответил успешно
            if (!response.ok) {
                throw new Error(`Ошибка сервера: ${response.status}`);
            }

            // Превращаем ответ из JSON в JavaScript-объект
            const data = await response.json();

            // --- Шаг 3: Отображаем результаты на странице ---
            displaySubscriptions(data.subscriptions);

        } catch (error) {
            // Если что-то пошло не так (например, сервер выключен), выводим ошибку
            console.error("Не удалось получить данные от аналитического сервиса:", error);
            container.innerHTML = `<p style="color: red;">Ошибка: Не удалось подключиться к аналитическому сервису. Убедитесь, что он запущен.</p>`;
        }
    }

    // --- Функция для отрисовки карточек ---
    function displaySubscriptions(subscriptions) {
        // Перед отрисовкой очищаем контейнер от шаблонов
        container.innerHTML = '';

        // Проверяем, нашлись ли подписки
        if (subscriptions.length === 0) {
            container.innerHTML = `<p>Активных подписок не найдено.</p>`;
            return;
        }

        // Проходим по каждой подписке из ответа и создаем для нее HTML-карточку
        subscriptions.forEach(sub => {
            const cardHTML = createSubscriptionCardHTML(sub);
            // Добавляем готовую карточку в наш контейнер на странице
            container.insertAdjacentHTML('beforeend', cardHTML);
        });
    }

    // --- Функция-шаблон для создания HTML одной карточки ---
    function createSubscriptionCardHTML(subscription) {
        // Проверяем, есть ли информация об экономии, и создаем для нее отдельный блок
        let savingHTML = '';
        if (subscription.potential_savings) {
            savingHTML = `
                <div class="card-saving">
                    <span>Экономия до</span>
                    <strong>${Math.round(subscription.potential_savings.saving_amount)} ₽</strong>
                </div>
            `;
        }

        // Возвращаем готовую HTML-строку, подставляя данные из объекта подписки
        return `
            <div class="subscription-card">
                <img src="${subscription.logo_url}" alt="Логотип ${subscription.name}" class="card-logo">
                <div class="card-info">
                    <h3 class="card-title">${subscription.name}</h3>
                    <p class="card-cost">${subscription.monthly_cost} ₽/мес.</p>
                </div>
                ${savingHTML}
            </div>
        `;
    }

    // --- Запускаем весь процесс ---
    fetchAndDisplaySubscriptions();
});