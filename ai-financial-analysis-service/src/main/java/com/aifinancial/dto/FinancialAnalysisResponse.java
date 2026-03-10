package com.aifinancial.dto;

import lombok.Data;
import java.util.List;

@Data
public class FinancialAnalysisResponse {
    private String analysis;
    private List<String> warnings;
    private List<String> suggestions;
    private List<String> insights;
    private Integer financialHealthScore;
    private String message;
    
    public static FinancialAnalysisResponse insufficientData(String message) {
        FinancialAnalysisResponse response = new FinancialAnalysisResponse();
        response.setMessage(message);
        return response;
    }
    
    public static FinancialAnalysisResponse success(String analysis, List<String> warnings, List<String> insights, List<String> suggestions, Integer financialHealthScore) {
        FinancialAnalysisResponse response = new FinancialAnalysisResponse();
        response.setAnalysis(analysis);
        response.setWarnings(warnings);
        response.setInsights(insights);
        response.setSuggestions(suggestions);
        response.setFinancialHealthScore(financialHealthScore);
        return response;
    }
    
    // Backward compatibility method
    public static FinancialAnalysisResponse success(String analysis, List<String> warnings, List<String> suggestions) {
        return success(analysis, warnings, null, suggestions, null);
    }
}
