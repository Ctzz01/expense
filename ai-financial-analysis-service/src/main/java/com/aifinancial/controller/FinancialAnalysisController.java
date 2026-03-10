package com.aifinancial.controller;

import com.aifinancial.dto.FinancialAnalysisResponse;
import com.aifinancial.service.FinancialAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
@Slf4j
public class FinancialAnalysisController {

    private final FinancialAnalysisService financialAnalysisService;

    @GetMapping("/financial")
    public ResponseEntity<FinancialAnalysisResponse> getFinancialAnalysis(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        
        log.info("Requesting financial analysis for user: {} from {} to {}", userId, fromDate, toDate);
        
        try {
            FinancialAnalysisResponse response = financialAnalysisService.analyzeFinancials(userId, fromDate, toDate);
            
            if (response.getMessage() != null) {
                // Insufficient data case
                return ResponseEntity.ok(response);
            } else {
                // Successful analysis
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("Error during financial analysis for user: {}", userId, e);
            FinancialAnalysisResponse errorResponse = new FinancialAnalysisResponse();
            errorResponse.setMessage("Analysis service temporarily unavailable. Please try again later.");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
