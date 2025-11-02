from analyzer.main import app

if __name__ == '__main__':
    # Запускаем Flask-сервер на порту 5000 в режиме отладки.
    # debug=True означает, что сервер будет автоматически перезагружаться при изменении кода.
    app.run(port=5000, debug=True)