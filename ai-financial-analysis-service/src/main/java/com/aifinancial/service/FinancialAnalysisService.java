package com.aifinancial.service;

import com.aifinancial.config.GroqClient;
import com.aifinancial.dto.FinancialAnalysisResponse;
import com.aifinancial.dto.FinancialSummary;
import com.aifinancial.repository.ExpenseRepository;
import com.aifinancial.repository.IncomeRepository;
import com.aifinancial.repository.model.Expense;
import com.aifinancial.repository.model.Income;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class FinancialAnalysisService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final GroqClient groqClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FinancialAnalysisResponse analyzeFinancials(Long userId) {
        // Default to 6 months analysis
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return analyzeFinancials(userId, sixMonthsAgo, LocalDate.now());
    }
    
    public FinancialAnalysisResponse analyzeFinancials(Long userId, LocalDate fromDate, LocalDate toDate) {
        log.info("Starting financial analysis for user: {} from {} to {}", userId, fromDate, toDate);
        
        // Gather financial data
        FinancialSummary summary = buildFinancialSummary(userId, fromDate, toDate);
        
        // Analysis will proceed with any amount of data, but warnings will be included for insufficient data
        
        try {
            // Build AI prompt
            String prompt = buildAIPrompt(summary);
            
            // Call Groq API
            GroqClient.GroqResponse response = groqClient.chatCompletion(prompt);
            
            if (response != null && response.choices != null && !response.choices.isEmpty()) {
                String aiResponse = response.choices.get(0).message.content;
                
                // Parse AI response
                return parseAIResponse(aiResponse, summary);
            } else {
                log.error("No response from Groq API");
                return FinancialAnalysisResponse.insufficientData("Unable to generate analysis at this time.");
            }
            
        } catch (Exception e) {
            log.error("Error during financial analysis", e);
            return FinancialAnalysisResponse.insufficientData("Analysis service temporarily unavailable.");
        }
    }
    
    private FinancialSummary buildFinancialSummary(Long userId, LocalDate fromDate, LocalDate toDate) {
        FinancialSummary summary = new FinancialSummary();
        summary.setUserId(userId);
        summary.setAnalysisDate(LocalDate.now());
        
        // Convert dates to LocalDateTime
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : LocalDate.now().minusMonths(6).atStartOfDay();
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);
        
        // Fetch income data
        List<Income> incomes = incomeRepository.findByUserIdAndDateBetween(userId, fromDateTime, toDateTime);
        
        // Fetch expense data
        List<Expense> expenses = expenseRepository.findByUserIdAndTimestampBetween(userId, fromDateTime, toDateTime);
        
        summary.setTotalTransactions(incomes.size() + expenses.size());
        
        // Calculate date range
        if (!incomes.isEmpty() || !expenses.isEmpty()) {
            LocalDate earliestDate = getEarliestDate(incomes, expenses);
            LocalDate latestDate = getLatestDate(incomes, expenses);
            summary.setMonthsOfData(calculateMonthsBetween(earliestDate, latestDate));
        } else {
            summary.setMonthsOfData(0);
        }
        
        // Calculate monthly averages
        calculateIncomeMetrics(summary, incomes, userId, fromDate, toDate);
        calculateExpenseMetrics(summary, expenses, userId, fromDate, toDate);
        calculateSavingsMetrics(summary);
        calculateTrends(summary, expenses, userId);
        
        return summary;
    }
    
    private DataQuality getDataQuality(FinancialSummary summary) {
        int transactions = summary.getTotalTransactions();
        int monthsOfData = summary.getMonthsOfData();
        
        if (transactions < 10 && monthsOfData < 1) {
            return DataQuality.VERY_LOW;
        } else if (transactions < 10 || monthsOfData < 1) {
            return DataQuality.LOW;
        } else if (transactions < 20 || monthsOfData < 3) {
            return DataQuality.MEDIUM;
        } else {
            return DataQuality.HIGH;
        }
    }
    
    private enum DataQuality {
        VERY_LOW, LOW, MEDIUM, HIGH
    }
    
    private String buildAIPrompt(FinancialSummary summary) {
        try {
            // Read prompt template from file
            InputStream inputStream = getClass().getResourceAsStream("/templates/financial-analysis-prompt.txt");
            if (inputStream == null) {
                throw new RuntimeException("Prompt template file not found");
            }
            
            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            
            // Get data quality warning
            DataQuality dataQuality = getDataQuality(summary);
            String dataQualityWarning = getDataQualityWarning(dataQuality);
            
            // Format the prompt with user data
            return String.format(template,
                dataQualityWarning,
                summary.getMonthlyIncome(),
                summary.getMonthlyExpense(),
                summary.getSavingsRate() * 100,
                formatTopCategories(summary.getExpenseBreakdown()),
                summary.getExpenseTrend(),
                summary.getMonthsOfData(),
                summary.getTotalTransactions()
            );
            
        } catch (Exception e) {
            log.error("Error reading prompt template", e);
            throw new RuntimeException("Failed to load prompt template", e);
        }
    }
    
    private String getDataQualityWarning(DataQuality dataQuality) {
        switch (dataQuality) {
            case VERY_LOW:
                return "WARNING: Very limited data available (less than 10 transactions and less than 1 week of data). Analysis may not be accurate or representative of actual financial patterns.";
            case LOW:
                return "CAUTION: Limited data available (less than 10 transactions or less than 1 month of data). Analysis may not fully represent financial patterns and should be used as general guidance only.";
            case MEDIUM:
                return "NOTE: Moderate data available (less than 20 transactions or less than 3 months of data). Analysis provides reasonable insights but may not capture all financial patterns.";
            case HIGH:
                return "GOOD: Sufficient data available for reliable financial analysis and recommendations.";
            default:
                return "";
        }
    }
    
    private FinancialAnalysisResponse parseAIResponse(String aiResponse, FinancialSummary summary) {
        try {
            // Extract JSON from AI response
            String jsonContent = extractJsonFromResponse(aiResponse);
            Map<String, Object> parsed = objectMapper.readValue(jsonContent, new TypeReference<Map<String, Object>>() {});
            
            String analysis = (String) parsed.get("analysis");
            @SuppressWarnings("unchecked")
            List<String> warnings = (List<String>) parsed.getOrDefault("warnings", new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<String> insights = (List<String>) parsed.getOrDefault("insights", new ArrayList<>());
            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) parsed.getOrDefault("suggestions", new ArrayList<>());
            Integer financialHealthScore = (Integer) parsed.get("financialHealthScore");
            
            // Add automatic data quality warnings
            DataQuality dataQuality = getDataQuality(summary);
            addDataQualityWarnings(warnings, dataQuality, summary);
            
            return FinancialAnalysisResponse.success(analysis, warnings, insights, suggestions, financialHealthScore);
            
        } catch (Exception e) {
            log.error("Failed to parse AI response", e);
            return FinancialAnalysisResponse.insufficientData("Unable to process analysis results.");
        }
    }
    
    private void addDataQualityWarnings(List<String> warnings, DataQuality dataQuality, FinancialSummary summary) {
        switch (dataQuality) {
            case VERY_LOW:
                warnings.add(0, "Analysis based on very limited data (less than 10 transactions and less than 1 week). Recommendations may not be accurate.");
                warnings.add(1, String.format("Only %d transactions over %d days available - track more data for reliable insights.", 
                    summary.getTotalTransactions(), summary.getMonthsOfData() * 30));
                break;
            case LOW:
                warnings.add(0, "Analysis based on limited data (less than 10 transactions or less than 1 month). Use as general guidance only.");
                if (summary.getTotalTransactions() < 10) {
                    warnings.add(1, String.format("Only %d transactions available - more transaction data needed for accurate patterns.", 
                        summary.getTotalTransactions()));
                }
                if (summary.getMonthsOfData() < 1) {
                    warnings.add(1, String.format("Only %d days of data available - longer time period needed for trend analysis.", 
                        summary.getMonthsOfData() * 30));
                }
                break;
            case MEDIUM:
                warnings.add(0, "Analysis based on moderate data - insights are reasonable but may not capture all patterns.");
                break;
            case HIGH:
                // No additional warnings needed for high quality data
                break;
        }
    }
    
    private String extractJsonFromResponse(String response) {
        // Find JSON object in the response
        int start = response.indexOf("{");
        int end = response.lastIndexOf("}") + 1;
        if (start != -1 && end != -1) {
            return response.substring(start, end);
        }
        throw new RuntimeException("No JSON found in AI response");
    }
    
    // Helper methods for calculations
    private LocalDate getEarliestDate(List<Income> incomes, List<Expense> expenses) {
        List<LocalDate> dates = new ArrayList<>();
        incomes.stream().map(i -> i.getDate().toLocalDate()).forEach(dates::add);
        expenses.stream().map(e -> e.getTimestamp().toLocalDate()).forEach(dates::add);
        return dates.isEmpty() ? LocalDate.now() : dates.stream().min(LocalDate::compareTo).orElse(LocalDate.now());
    }
    
    private LocalDate getLatestDate(List<Income> incomes, List<Expense> expenses) {
        List<LocalDate> dates = new ArrayList<>();
        incomes.stream().map(i -> i.getDate().toLocalDate()).forEach(dates::add);
        expenses.stream().map(e -> e.getTimestamp().toLocalDate()).forEach(dates::add);
        return dates.isEmpty() ? LocalDate.now() : dates.stream().max(LocalDate::compareTo).orElse(LocalDate.now());
    }
    
    private int calculateMonthsBetween(LocalDate start, LocalDate end) {
        return (int) YearMonth.from(start).until(YearMonth.from(end), java.time.temporal.ChronoUnit.MONTHS) + 1;
    }
    
    private void calculateIncomeMetrics(FinancialSummary summary, List<Income> incomes, Long userId, LocalDate fromDate, LocalDate toDate) {
        if (incomes.isEmpty()) {
            summary.setTotalIncome(0.0);
            summary.setMonthlyIncome(0.0);
            summary.setIncomeBreakdown(new ArrayList<>());
            return;
        }
        
        double totalIncome = incomes.stream().mapToDouble(Income::getAmount).sum();
        summary.setTotalIncome(totalIncome);
        summary.setMonthlyIncome(summary.getMonthsOfData() > 0 ? totalIncome / summary.getMonthsOfData() : 0.0);
        
        // Income breakdown by category using repository query
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : LocalDate.now().minusMonths(6).atStartOfDay();
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);
        List<Object[]> categoryData = incomeRepository.getCategorySummaryByDateRange(userId, fromDateTime, toDateTime);
        
        List<FinancialSummary.IncomeCategoryBreakdown> incomeBreakdown = categoryData.stream()
            .map(row -> {
                FinancialSummary.IncomeCategoryBreakdown breakdown = new FinancialSummary.IncomeCategoryBreakdown();
                breakdown.setCategory((String) row[0]);
                breakdown.setAmount((Double) row[1]);
                breakdown.setPercentage(totalIncome > 0 ? ((Double) row[1] / totalIncome) * 100 : 0.0);
                return breakdown;
            })
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
            .collect(Collectors.toList());
        
        summary.setIncomeBreakdown(incomeBreakdown);
    }
    
    private void calculateExpenseMetrics(FinancialSummary summary, List<Expense> expenses, Long userId, LocalDate fromDate, LocalDate toDate) {
        if (expenses.isEmpty()) {
            summary.setTotalExpense(0.0);
            summary.setMonthlyExpense(0.0);
            summary.setExpenseBreakdown(new ArrayList<>());
            return;
        }
        
        double totalExpense = expenses.stream().mapToDouble(Expense::getAmount).sum();
        summary.setTotalExpense(totalExpense);
        summary.setMonthlyExpense(summary.getMonthsOfData() > 0 ? totalExpense / summary.getMonthsOfData() : 0.0);
        
        // Expense breakdown by category using repository query
        LocalDateTime fromDateTime = fromDate != null ? fromDate.atStartOfDay() : LocalDate.now().minusMonths(6).atStartOfDay();
        LocalDateTime toDateTime = toDate != null ? toDate.atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);
        List<Object[]> categoryData = expenseRepository.getCategorySummaryByDateRange(userId, fromDateTime, toDateTime);
        
        List<FinancialSummary.ExpenseCategoryBreakdown> expenseBreakdown = categoryData.stream()
            .map(row -> {
                FinancialSummary.ExpenseCategoryBreakdown breakdown = new FinancialSummary.ExpenseCategoryBreakdown();
                breakdown.setCategory((String) row[0]);
                breakdown.setAmount((Double) row[1]);
                breakdown.setPercentage(totalExpense > 0 ? ((Double) row[1] / totalExpense) * 100 : 0.0);
                return breakdown;
            })
            .sorted((a, b) -> Double.compare(b.getAmount(), a.getAmount()))
            .collect(Collectors.toList());
        
        summary.setExpenseBreakdown(expenseBreakdown);
    }
    
    private void calculateSavingsMetrics(FinancialSummary summary) {
        double netSavings = summary.getTotalIncome() - summary.getTotalExpense();
        summary.setNetSavings(netSavings);
        summary.setAverageMonthlySavings(summary.getMonthsOfData() > 0 ? netSavings / summary.getMonthsOfData() : 0.0);
        summary.setSavingsRate(summary.getTotalIncome() > 0 ? (netSavings / summary.getTotalIncome()) : 0.0);
    }
    
    private void calculateTrends(FinancialSummary summary, List<Expense> expenses, Long userId) {
        // Simple trend calculation based on recent vs older expenses
        LocalDate oneMonthAgo = LocalDate.now().minusMonths(1);
        LocalDate twoMonthsAgo = LocalDate.now().minusMonths(2);
        
        double recentExpenses = expenseRepository.getTotalExpenseByUserIdAndDateRange(
            userId, oneMonthAgo.atStartOfDay(), LocalDate.now().atTime(23, 59, 59));
            
        double olderExpenses = expenseRepository.getTotalExpenseByUserIdAndDateRange(
            userId, twoMonthsAgo.atStartOfDay(), oneMonthAgo.minusDays(1).atTime(23, 59, 59));
        
        if (olderExpenses > 0) {
            double change = ((recentExpenses - olderExpenses) / olderExpenses) * 100;
            if (change > 10) {
                summary.setExpenseTrend("Increasing");
            } else if (change < -10) {
                summary.setExpenseTrend("Decreasing");
            } else {
                summary.setExpenseTrend("Stable");
            }
        } else {
            summary.setExpenseTrend("Insufficient data");
        }
    }
    
    private String formatTopCategories(List<FinancialSummary.ExpenseCategoryBreakdown> breakdown) {
        if (breakdown == null || breakdown.isEmpty()) {
            return "No data";
        }
        return breakdown.stream()
            .limit(3)
            .map(c -> String.format("%s ($%.2f)", c.getCategory(), c.getAmount()))
            .collect(Collectors.joining(", "));
    }
}
