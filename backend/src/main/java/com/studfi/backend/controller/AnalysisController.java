package com.studfi.backend.controller;

import com.studfi.backend.dto.Transaction;
import com.studfi.backend.service.AnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AnalysisController {

    private final AnalysisService analysisService;

    @Autowired
    public AnalysisController(AnalysisService analysisService) {
        this.analysisService = analysisService;
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactionsForAnalysis() {
        return analysisService.getAllTransactionsFromAllBanks();
    }
}