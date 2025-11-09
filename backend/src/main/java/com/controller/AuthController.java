package com.controller;

import com.dto.AuthResponse;
import com.dto.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
//@CrossOrigin(origins = "*") // Разрешаем запросы с любого источника (для хакатона это идеально)
public class AuthController {

    // Эндпоинт для входа
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        // --- ЗДЕСЬ МЫ ПРОВЕРЯЕМ ДАННЫЕ ---
        String correctUsername = "team100-5";
        String correctPassword = "fiOdrM6KVRex22RQBLbNzZvE4jkPymhc";

        // Проверяем, совпадают ли логин и пароль
        if (correctUsername.equals(loginRequest.getUsername()) && correctPassword.equals(loginRequest.getPassword())) {
            // Если все верно, создаем фейковый токен и отправляем успешный ответ
            String token = "fake-jwt-token-for-team100-5";
            return ResponseEntity.ok(new AuthResponse(token));
        } else {
            // Если данные неверны, отправляем ошибку "401 Unauthorized"
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный логин или пароль");
        }
    }

    // Сюда можно будет добавить эндпоинт для регистрации /register, если понадобится
}