// dashboard.js (с улучшенным отображением процентов по депозиту)
document.addEventListener('DOMContentLoaded', () => {
    const statsContainer = document.getElementById('bank-stats-container');
    const modal = document.getElementById('transactions-modal');
    const modalTitle = document.getElementById('modal-title');
    const modalBody = document.getElementById('modal-body-content');
    const closeModalBtn = document.getElementById('modal-close-btn');
    if (!statsContainer) return;

    let transactionsByBank = {};

    async function fetchAndGroupTransactions() {
        // ... (этот код остается без изменений) ...
        statsContainer.innerHTML = '<p>Загрузка данных по банкам...</p>';
        try {
            const token = localStorage.getItem('authToken');
            if (!token) { window.location.href = 'login.html'; return; }

            const javaApiUrl = 'http://localhost:8080/api/v1/transactions?banks=xbank,abank,vbank,sbank';
            const javaResponse = await fetch(javaApiUrl, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!javaResponse.ok) throw new Error(`Ошибка Java: ${javaResponse.status}`);
            const allTransactions = await javaResponse.json();

            transactionsByBank = allTransactions.reduce((acc, tx) => {
                if (!acc[tx.bankName]) acc[tx.bankName] = [];
                acc[tx.bankName].push(tx);
                return acc;
            }, {});

            displayBankStats();
        } catch (error) {
            console.error("Ошибка при загрузке транзакций:", error);
            statsContainer.innerHTML = '<p style="color: red;">Не удалось загрузить данные.</p>';
        }
    }

    function displayBankStats() {
        // ... (этот код остается без изменений) ...
        statsContainer.innerHTML = '';
        if (Object.keys(transactionsByBank).length === 0) {
            statsContainer.innerHTML = '<p>Данные по банкам не найдены.</p>';
            return;
        }
        for (const bankName in transactionsByBank) {
            const transactions = transactionsByBank[bankName];
            const totalCount = transactions.length;
            const totalAmount = transactions.reduce((sum, tx) => sum + tx.amount, 0);

            const cardHTML = `
                <div class="bank-stats-card">
                    <h3>${bankName}</h3>
                    <p>Всего транзакций: <strong>${totalCount}</strong></p>
                    <p>Общий оборот: <strong>${totalAmount.toFixed(0)} ₽</strong></p>
                    <button class="view-trans-btn" data-bank="${bankName}">Показать транзакции</button>
                </div>`;
            statsContainer.insertAdjacentHTML('beforeend', cardHTML);
        }
    }

    function openTransactionsModal(bankName) {
        const transactions = transactionsByBank[bankName] || [];
        modalTitle.textContent = `Транзакции банка: ${bankName}`;

        if (transactions.length === 0) {
            modalBody.innerHTML = '<p>Транзакций для этого банка не найдено.</p>';
            modal.style.display = 'flex';
            return;
        }

        transactions.sort((a, b) => new Date(b.date) - new Date(a.date));

        const groupedByMonth = transactions.reduce((acc, tx) => {
            const date = new Date(tx.date);
            const monthYear = date.toLocaleString('ru-RU', { month: 'long', year: 'numeric' });
            if (!acc[monthYear]) {
                acc[monthYear] = [];
            }
            acc[monthYear].push(tx);
            return acc;
        }, {});

        let transactionsHTML = '';
        for (const month in groupedByMonth) {
            transactionsHTML += `<h4 class="month-divider">${month.charAt(0).toUpperCase() + month.slice(1)}</h4>`;
            transactionsHTML += '<ul class="transactions-list">';

            groupedByMonth[month].forEach(tx => {
                const amountClass = tx.amount > 0 ? 'positive' : 'negative';

                // --- ИЗМЕНЕНИЯ НАЧИНАЮТСЯ ЗДЕСЬ ---
                let descriptionHTML;
                const depositText = "Проценты по депозиту";

                // Проверяем, является ли это транзакцией по депозиту
                if (tx.description.startsWith(depositText)) {
                    // Разделяем строку на основной текст и номер
                    const parts = tx.description.split('№');
                    const mainText = parts[0];
                    const depositId = parts[1] ? `№${parts[1]}` : ''; // Собираем номер обратно, если он есть

                    // Генерируем специальный HTML
                    descriptionHTML = `${mainText}<span class="transaction-id">${depositId}</span>`;
                } else {
                    // Для всех остальных транзакций оставляем как было
                    descriptionHTML = tx.description;
                }
                // --- ИЗМЕНЕНИЯ ЗАКАНЧИВАЮТСЯ ЗДЕСЬ ---

                transactionsHTML += `
                    <li>
                        <span class="description">${descriptionHTML}</span>
                        <span class="amount ${amountClass}">${tx.amount.toFixed(2)} ₽</span>
                    </li>`;
            });

            transactionsHTML += '</ul>';
        }

        modalBody.innerHTML = transactionsHTML;
        modal.style.display = 'flex';
    }

    // ... (остальной код остается без изменений) ...
    statsContainer.addEventListener('click', (event) => {
        if (event.target.classList.contains('view-trans-btn')) {
            const bankName = event.target.dataset.bank;
            openTransactionsModal(bankName);
        }
    });
    closeModalBtn.addEventListener('click', () => modal.style.display = 'none');
    modal.addEventListener('click', (event) => {
        if (event.target === modal) modal.style.display = 'none';
    });

    fetchAndGroupTransactions();
});