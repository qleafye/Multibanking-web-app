// subs.js (с фильтром "только именованные")

document.addEventListener('DOMContentLoaded', () => {
    // Получаем ссылки на все нужные элементы
    const container = document.getElementById('subscriptions-container');
    const bankCheckboxes = document.querySelectorAll('.bank-checkbox');
    const namedOnlyCheckbox = document.getElementById('named-only-checkbox');
    if (!container) return;

    // Переменная для хранения последних загруженных данных, чтобы не делать лишних запросов
    let lastFetchedSubscriptions = [];

    // --- Функции-шаблоны для HTML (без изменений) ---
    function createSubscriptionCardHTML(subscription) { /* ... код без изменений ... */ }
    function createAggregatedCardHTML(generalSubscriptions) { /* ... код без изменений ... */ }
    // (Код этих функций скрыт для краткости, он остается прежним)
    function createSubscriptionCardHTML(subscription) {
        let savingHTML = '';
        if (subscription.potential_savings) {
            savingHTML = `<div class="card-saving"><span>Экономия до</span><strong>${Math.round(subscription.potential_savings.saving_amount)} ₽</strong></div>`;
        }
        return `
            <div class="subscription-card">
                <img src="${subscription.logo_url || 'images/placeholder-logo.png'}" alt="Логотип ${subscription.name}" class="card-logo">
                <div class="card-info">
                    <h3 class="card-title">${subscription.name}</h3>
                    <p class="card-cost">${subscription.monthly_cost.toFixed(2)} ₽/мес.</p>
                </div>
                ${savingHTML}
            </div>`;
    }
    function createAggregatedCardHTML(generalSubscriptions) {
        const totalAmount = generalSubscriptions.reduce((sum, sub) => sum + sub.monthly_cost, 0);
        const count = generalSubscriptions.length;
        const sub = generalSubscriptions[0];
        return `
            <div class="subscription-card aggregated-card">
                <img src="${sub.logo_url || 'images/placeholder-logo.png'}" alt="Логотип ${sub.name}" class="card-logo">
                <div class="card-info">
                    <h3 class="card-title">${sub.name}</h3>
                    <p class="card-cost">Общая сумма: ${totalAmount.toFixed(2)} ₽</p>
                </div>
                <div class="card-count-badge">${count} платежей</div>
            </div>`;
    }


    // --- НОВАЯ ФУНКЦИЯ ДЛЯ ОТРИСОВКИ ---
    // Эта функция не загружает данные, а только отображает то, что уже загружено.
    function renderSubscriptions() {
        container.innerHTML = ''; // Очищаем контейнер

        if (!lastFetchedSubscriptions || lastFetchedSubscriptions.length === 0) {
            container.innerHTML = '<p>Активных подписок не найдено.</p>';
            return;
        }

        // Группируем подписки по ключу "Год-Месяц" для надежной сортировки
        const groupedByMonth = lastFetchedSubscriptions.reduce((acc, sub) => {
            const date = new Date(sub.date);
            const key = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
            if (!acc[key]) {
                acc[key] = {
                    monthName: date.toLocaleString('ru-RU', { month: 'long', year: 'numeric' }),
                    subscriptions: []
                };
            }
            acc[key].subscriptions.push(sub);
            return acc;
        }, {});

        const sortedKeys = Object.keys(groupedByMonth).sort().reverse();

        container.insertAdjacentHTML('beforeend', `<h2 class="subscriptions-period-header">История подписок</h2>`);

        sortedKeys.forEach(key => {
            const group = groupedByMonth[key];
            const namedSubscriptions = group.subscriptions.filter(s => s.name !== "Общие подписки");
            const generalSubscriptions = group.subscriptions.filter(s => s.name === "Общие подписки");

            // Если нет ни именованных, ни общих (или общие скрыты), то не рисуем заголовок месяца
            if (namedSubscriptions.length === 0 && (generalSubscriptions.length === 0 || namedOnlyCheckbox.checked)) {
                return;
            }

            const monthHTML = `<h3 class="month-header">${group.monthName.charAt(0).toUpperCase() + group.monthName.slice(1)}</h3>`;
            container.insertAdjacentHTML('beforeend', monthHTML);

            // --- КЛЮЧЕВОЕ ИЗМЕНЕНИЕ ---
            // Показываем "Общие подписки" только если галочка НЕ стоит
            if (generalSubscriptions.length > 0 && !namedOnlyCheckbox.checked) {
                container.insertAdjacentHTML('beforeend', createAggregatedCardHTML(generalSubscriptions));
            }
            namedSubscriptions.forEach(sub => {
                container.insertAdjacentHTML('beforeend', createSubscriptionCardHTML(sub));
            });
        });
    }


    // --- Функция для ЗАГРУЗКИ данных (остается почти без изменений) ---
    async function updateSubscriptions() {
        const selectedBanks = Array.from(bankCheckboxes).filter(cb => cb.checked).map(cb => cb.value);
        container.innerHTML = '<p>Загрузка подписок...</p>';

        if (selectedBanks.length === 0) {
            container.innerHTML = '<p>Выберите хотя бы один банк для анализа.</p>';
            return;
        }

        const javaApiUrl = `http://localhost:8080/api/v1/transactions?banks=${selectedBanks.join(',')}`;

        try {
            const token = localStorage.getItem('authToken');
            if (!token) { window.location.href = 'login.html'; return; }

            const javaResponse = await fetch(javaApiUrl, { headers: { 'Authorization': `Bearer ${token}` } });
            if (!javaResponse.ok) throw new Error(`Ошибка Java: ${javaResponse.status}`);
            const realTransactions = await javaResponse.json();

            const pythonResponse = await fetch('http://127.0.0.1:8000/analyze', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ transactions: realTransactions, mode: "pro" })
            });
            if (!pythonResponse.ok) throw new Error(`Ошибка Python: ${pythonResponse.status}`);
            const data = await pythonResponse.json();

            // Сохраняем результат в переменную
            lastFetchedSubscriptions = data.subscriptions;
            // Вызываем функцию отрисовки
            renderSubscriptions();

        } catch (error) {
            console.error("Ошибка при обновлении подписок:", error);
            container.innerHTML = `<p style="color: red;">Произошла ошибка. Убедитесь, что все серверы запущены.</p>`;
        }
    }

    // --- НАЗНАЧАЕМ ОБРАБОТЧИКИ СОБЫТИЙ ---

    // При изменении банков - загружаем данные заново
    bankCheckboxes.forEach(checkbox => checkbox.addEventListener('change', updateSubscriptions));

    // При изменении галочки "только именованные" - просто перерисовываем то, что уже есть
    namedOnlyCheckbox.addEventListener('change', renderSubscriptions);

    // Первоначальная загрузка данных при открытии страницы
    updateSubscriptions();
});