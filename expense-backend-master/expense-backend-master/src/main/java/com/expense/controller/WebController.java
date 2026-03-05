package com.expense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    // Serve our custom SPA from the expense-tracker folder
    @GetMapping({"/", "/app", "/dashboard"})
    public String index() {
        return "forward:/index.html"; // resolved by WebConfig static handler to expense-tracker/index.html
    }

    // Route for income page
    @GetMapping("/income")
    public String income() {
    	
        return "forward:/income.html";
    }

    @GetMapping("/categories")
    public String categories() {
        return "forward:/categories.html";
    }
}
