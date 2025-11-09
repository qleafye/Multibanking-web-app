// Ждем, пока вся HTML-страница полностью загрузится
document.addEventListener('DOMContentLoaded', () => {

    // --- Шаг 1: Находим нужные нам элементы на странице ---
    const loginForm = document.getElementById('loginForm');
    const usernameInput = document.getElementById('username'); // Убедитесь, что id в HTML - "username"
    const passwordInput = document.getElementById('password');

    // (Пока не будем добавлять обработку ошибок, чтобы не усложнять)

    // --- Шаг 2: Вешаем "прослушку" на саму форму ---
    // Это сработает и при клике на кнопку, и при нажатии Enter в поле пароля
    loginForm.addEventListener('submit', async (event) => {

        // Предотвращаем стандартное поведение формы (перезагрузку страницы)
        event.preventDefault();

        // --- Шаг 3: Собираем данные из полей ---
        const username = usernameInput.value;
        const password = passwordInput.value;

        console.log(`Попытка входа с логином: ${username}`); // Отладочное сообщение в консоль

        // --- Шаг 4: Отправляем данные на ваш Java-бэкенд ---
        try {
            const response = await fetch('http://localhost:8080/api/v1/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                // Превращаем наши данные в JSON-строку
                body: JSON.stringify({
                    username: username,
                    password: password,
                }),
            });

            // --- Шаг 5: Обрабатываем ответ от сервера ---
            if (response.ok) {
                // Если сервер ответил "200 OK"
                const data = await response.json();

                // Сохраняем токен авторизации в специальное хранилище браузера
                localStorage.setItem('authToken', data.token); // Убедитесь, что поле в ответе называется 'token'

                alert('Успешный вход! Сейчас вы будете перенаправлены.');

                // Перенаправляем пользователя на главный экран
                // (когда мы его создадим, он будет здесь)
                window.location.href = 'main-lite.html';

            } else {
                // Если сервер ответил ошибкой (401, 404, 500...)
                const errorText = await response.text();
                alert(`Ошибка входа: ${errorText}`);
            }

        } catch (error) {
            // Если сам запрос не удалось отправить (например, Java-сервер выключен)
            alert('Не удалось подключиться к серверу. Убедитесь, что бэкенд запущен.');
            console.error('Ошибка сети:', error);
        }
    });
});