// advices.js

document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('advices-container');
    if (!container) return;

    // --- Функция-шаблон для создания HTML одной карточки совета ---
    function createAdviceCardHTML(suggestion) {
        return `
            <div class="advice-card">
                <div class="advice-icon">💡</div>
                <div class="advice-content">
                    <h3 class="advice-title">${suggestion.name}</h3>
                    <p class="advice-subtitle">Текущая стоимость: ${suggestion.cost.toFixed(2)} ₽/мес.</p>
                    <p class="advice-text">${suggestion.suggestion}</p>
                </div>
            </div>
        `;
    }

    // --- Функция для отображения советов на странице ---
    function displayAdvices(suggestions) {
        container.innerHTML = ''; // Очищаем контейнер

        if (!suggestions || suggestions.length === 0) {
            container.innerHTML = '<p>Персональных советов пока нет. Возможно, вы уже пользуетесь самыми выгодными тарифами!</p>';
            return;
        }

        suggestions.forEach(suggestion => {
            const cardHTML = createAdviceCardHTML(suggestion);
            container.insertAdjacentHTML('beforeend', cardHTML);
        });
    }

    // --- Основная функция для загрузки и отображения данных ---
    async function fetchAndDisplayAdvices() {
        container.innerHTML = '<p>Анализируем ваши подписки и ищем способы сэкономить...</p>';

        // Мы запрашиваем транзакции по всем банкам сразу, чтобы дать полный совет
        const javaApiUrl = 'http://localhost:8080/api/v1/transactions?banks=xbank,abank,vbank,sbank';

        try {
            const token = localStorage.getItem('authToken');
            if (!token) {
                window.location.href = 'login.html';
                return;
            }

            // 1. Получаем все транзакции с Java-сервера
            const javaResponse = await fetch(javaApiUrl, { headers: { 'Authorization': `Bearer ${token}` } });
            if (!javaResponse.ok) throw new Error(`Ошибка Java: ${javaResponse.status}`);
            const realTransactions = await javaResponse.json();

            // 2. Отправляем транзакции на PRO-анализ в Python
            const pythonResponse = await fetch('http://127.0.0.1:8000/analyze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ transactions: realTransactions, mode: "pro" }) // Запрашиваем PRO-режим
            });
            if (!pythonResponse.ok) throw new Error(`Ошибка Python: ${pythonResponse.status}`);
            const data = await pythonResponse.json();

            // 3. Отображаем полученные советы
            displayAdvices(data.pro_version_suggestions);

        } catch (error)
        {
            console.error("Ошибка при загрузке советов:", error);
            container.innerHTML = `<p style="color: red;">Не удалось загрузить советы. Убедитесь, что все серверы запущены.</p>`;
        }
    }

    // Запускаем процесс при загрузке страницы
    fetchAndDisplayAdvices();
});