// auth.js (обновленная версия с Toastify)

document.addEventListener('DOMContentLoaded', () => {

    // --- Вспомогательная функция для показа уведомлений ---
    const showNotification = (text, type = 'success') => {
        const colors = {
            success: 'linear-gradient(to right, #00b09b, #96c93d)', // Зеленый
            error: 'linear-gradient(to right, #ff5f6d, #ffc371)'      // Красный
        };

        Toastify({
            text: text,
            duration: 3000,
            close: true,
            gravity: "top", // `top` или `bottom`
            position: "right", // `left`, `center` или `right`
            backgroundColor: type === 'success' ? colors.success : colors.error,
            stopOnFocus: true, // Останавливать таймер, если курсор над уведомлением
        }).showToast();
    };


    // --- Логика для страницы входа ---
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // Отменяем стандартную перезагрузку страницы

            const username = document.getElementById('username').value;
            const password = document.getElementById('password').value;

            // TODO: Замените URL на эндпоинт вашего Java-бэкенда
            const API_URL = 'http://localhost:8080/api/v1/auth/login';

            try {
                const response = await fetch(API_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password })
                });

                if (response.ok) {
                    const data = await response.json();
                    localStorage.setItem('authToken', data.token);
                    showNotification('Успешный вход! Перенаправляем...', 'success');
                    // Небольшая задержка перед перенаправлением, чтобы пользователь успел увидеть сообщение
                    setTimeout(() => {
                        window.location.href = 'main-lite.html';
                    }, 1500);
                } else {
                    showNotification('Ошибка входа: неверный логин или пароль.', 'error');
                }
            } catch (error) {
                showNotification('Ошибка сети: не удалось подключиться к серверу.', 'error');
                console.error(error);
            }
        });
    }

    // --- Логика для страницы регистрации (очень похожа) ---
    const regForm = document.getElementById('regForm');
    if (regForm) {
        regForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const username = document.getElementById('username').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;

            // TODO: Замените URL на эндпоинт регистрации вашего Java-бэкенда
            const API_URL = 'http://localhost:8080/api/v1/auth/register';

            try {
                const response = await fetch(API_URL, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, email, password })
                });

                if (response.ok) {
                    showNotification('Регистрация прошла успешно! Теперь вы можете войти.', 'success');
                     // Небольшая задержка перед перенаправлением
                    setTimeout(() => {
                        window.location.href = 'login.html';
                    }, 2000);
                } else {
                    // Можно будет добавить более конкретную ошибку от сервера
                    showNotification('Ошибка регистрации. Возможно, такой пользователь уже существует.', 'error');
                }
            } catch (error) {
                showNotification('Ошибка сети: не удалось подключиться к серверу.', 'error');
                console.error(error);
            }
        });
    }
});