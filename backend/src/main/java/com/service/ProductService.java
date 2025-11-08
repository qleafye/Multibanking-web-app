package com.service;

import com.client.BankClient;
import com.dto.Product;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final List<BankClient> bankClients;

    public ProductService(List<BankClient> bankClients) {
        this.bankClients = bankClients;
    }

    // Собирает каталоги продуктов со ВСЕХ подключенных банков
    public List<Product> getAllProductsFromAllBanks() {
        return bankClients.stream()
                .flatMap(client -> client.getAvailableProducts().stream())
                .collect(Collectors.toList());
    }
}