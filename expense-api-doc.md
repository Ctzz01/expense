# Expense Backend API Documentation

## Overview

This document lists all REST APIs exposed by the Expense Backend Service. All APIs are grouped by functional domain (Expense, Income, Category, Income Category).  
Base URL: `http://localhost:8080`

---

## Expense APIs

### 1. Add Expense

- **API Name**: Add Expense
- **HTTP Method**: POST
- **Endpoint URL**: `/expense/addExpense/{userId}`
- **Description / Usage**: Adds a new expense for a user. If the category does not exist, it creates a new user-defined category.
- **Request Body JSON**:
```json
{
  "amount": 150.75,
  "description": "Lunch at restaurant",
  "timestamp": "2026-03-05T14:30:00",
  "category": {
    "name": "Food"
  }
}
```
- **Response JSON example**:
```json
"Expense added successfully!"
```
- **Required Headers**: None
- **Query Parameters**: None

---

### 2. Update Expense

- **API Name**: Update Expense
- **HTTP Method**: PUT
- **Endpoint URL**: `/expense/updateExpense/{expenseId}`
- **Description / Usage**: Updates an existing expense by ID.
- **Request Body JSON**:
```json
{
  "amount": 200.0,
  "description": "Updated dinner",
  "timestamp": "2026-03-05T20:00:00",
  "category": {
    "name": "Food"
  }
}
```
- **Response JSON example**:
```json
{
  "id": 1,
  "amount": 200.0,
  "description": "Updated dinner",
  "timestamp": "2026-03-05T20:00:00",
  "userId": 1,
  "category": {
    "id": 1,
    "name": "Food",
    "userId": null
  }
}
```
- **Required Headers**: None
- **Query Parameters**: None

---

### 3. Delete Expense

- **API Name**: Delete Expense
- **HTTP Method**: DELETE
- **Endpoint URL**: `/expense/deleteExpense/{expenseId}`
- **Description / Usage**: Deletes an expense by ID.
- **Request Body JSON**: N/A
- **Response JSON example**: 204 No Content
- **Required Headers**: None
- **Query Parameters**: None

---

### 4. Get Balance

- **API Name**: Get Account Balance
- **HTTP Method**: GET
- **Endpoint URL**: `/expense/getBalance/{userId}`
- **Description / Usage**: Retrieves the account balance for a user.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
1250.50
```
- **Required Headers**: None
- **Query Parameters**: None

---

### 5. Get Expense History

- **API Name**: Get Expense History
- **HTTP Method**: GET
- **Endpoint URL**: `/expense/getExpenseHistory/{userId}`
- **Description / Usage**: Retrieves the full expense history for a user.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
[
  {
    "id": 1,
    "amount": 150.75,
    "description": "Lunch at restaurant",
    "timestamp": "2026-03-05T14:30:00",
    "userId": 1,
    "category": {
      "id": 1,
      "name": "Food",
      "userId": null
    }
  }
]
```
- **Required Headers**: None
- **Query Parameters**: None

---

### 6. Get Category-wise Summary

- **API Name**: Get Category-wise Expense Summary
- **HTTP Method**: GET
- **Endpoint URL**: `/expense/getCategoryWiseSummary/{userId}`
- **Description / Usage**: Retrieves expense summary grouped by categories. Supports filters: week, month, year, or custom date range.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
[
  {
    "categoryName": "Food",
    "totalAmount": 850.50,
    "percentage": 45.2
  },
  {
    "categoryName": "Transport",
    "totalAmount": 320.0,
    "percentage": 17.0
  }
]
```
- **Required Headers**: None
- **Query Parameters**:
  - `filter` (optional): `week`, `month`, `year`
  - `startDate` (optional, ISO date): e.g., `2026-03-01`
  - `endDate` (optional, ISO date): e.g., `2026-03-31`

---

## Income APIs

### 1. Add Income

- **API Name**: Add Income
- **HTTP Method**: POST
- **Endpoint URL**: `/income/addIncome/{userId}`
- **Description / Usage**: Adds a new income entry for a user.
- **Request Body JSON**:
```json
{
  "amount": 3000.0,
  "description": "Monthly salary",
  "date": "2026-03-01T09:00:00",
  "category": {
    "name": "Salary"
  }
}
```
- **Response JSON example**:
```json
{
  "id": 1,
  "amount": 3000.0,
  "description": "Monthly salary",
  "date": "2026-03-01T09:00:00",
  "userId": 1,
  "category": {
    "id": 1,
    "name": "Salary",
    "userId": 1
  }
}
```
- **Required Headers**: None
- **Query Parameters**: None

---

### 2. Get Incomes

- **API Name**: Get Incomes
- **HTTP Method**: GET
- **Endpoint URL**: `/income/getIncomes/{userId}`
- **Description / Usage**: Retrieves all incomes for a user. Supports date filters.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
[
  {
    "id": 1,
    "amount": 3000.0,
    "description": "Monthly salary",
    "date": "2026-03-01T09:00:00",
    "userId": 1,
    "category": {
      "id": 1,
      "name": "Salary",
      "userId": 1
    }
  }
]
```
- **Required Headers**: None
- **Query Parameters**:
  - `filter` (optional): e.g., `week`, `month`, `year`
  - `startDate` (optional, ISO date): e.g., `2026-03-01`
  - `endDate` (optional, ISO date): e.g., `2026-03-31`

---

### 3. Update Income

- **API Name**: Update Income
- **HTTP Method**: PUT
- **Endpoint URL**: `/income/updateIncome/{incomeId}`
- **Description / Usage**: Updates an existing income entry.
- **Request Body JSON**:
```json
{
  "amount": 3200.0,
  "description": "Updated salary",
  "date": "2026-03-01T09:00:00",
  "category": {
    "name": "Salary"
  }
}
```
- **Response JSON example**:
```json
{
  "id": 1,
  "amount": 3200.0,
  "description": "Updated salary",
  "date": "2026-03-01T09:00:00",
  "userId": 1,
  "category": {
    "id": 1,
    "name": "Salary",
    "userId": 1
  }
}
```
- **Required Headers**: None
- **Query Parameters**: None

---

### 4. Delete Income

- **API Name**: Delete Income
- **HTTP Method**: DELETE
- **Endpoint URL**: `/income/deleteIncome/{incomeId}`
- **Description / Usage**: Deletes an income entry by ID.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
"Income deleted successfully."
```
- **Required Headers**: None
- **Query Parameters**: None

---

## Category APIs (Expense Categories)

### 1. Get All Categories

- **API Name**: Get All Expense Categories
- **HTTP Method**: GET
- **Endpoint URL**: `/api/categories`
- **Description / Usage**: Retrieves all expense categories for a user.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
[
  {
    "id": 1,
    "name": "Food",
    "userId": 1
  },
  {
    "id": 2,
    "name": "Transport",
    "userId": 1
  }
]
```
- **Required Headers**: None
- **Query Parameters**:
  - `userId` (required): User ID

---

### 2. Create Category

- **API Name**: Create Expense Category
- **HTTP Method**: POST
- **Endpoint URL**: `/api/categories`
- **Description / Usage**: Creates a new expense category for a user.
- **Request Body JSON**:
```json
{
  "name": "Entertainment"
}
```
- **Response JSON example**:
```json
{
  "id": 3,
  "name": "Entertainment",
  "userId": 1
}
```
- **Required Headers**: None
- **Query Parameters**:
  - `userId` (required): User ID

---

### 3. Update Category

- **API Name**: Update Expense Category
- **HTTP Method**: PUT
- **Endpoint URL**: `/api/categories/{id}`
- **Description / Usage**: Updates an expense category by ID.
- **Request Body JSON**:
```json
{
  "name": "Fun & Games"
}
```
- **Response JSON example**:
```json
{
  "id": 3,
  "name": "Fun & Games",
  "userId": 1
}
```
- **Required Headers**: None
- **Query Parameters**:
  - `userId` (required): User ID

---

### 4. Delete Category

- **API Name**: Delete Expense Category
- **HTTP Method**: DELETE
- **Endpoint URL**: `/api/categories/{id}`
- **Description / Usage**: Deletes an expense category by ID. Returns 409 if expenses reference this category.
- **Request Body JSON**: N/A
- **Response JSON example**: 204 No Content
- **Required Headers**: None
- **Query Parameters**:
  - `userId` (required): User ID

---

## Income Category APIs

### 1. Create Income Category

- **API Name**: Create Income Category
- **HTTP Method**: POST
- **Endpoint URL**: `/api/income-category`
- **Description / Usage**: Creates a new income category for a user.
- **Request Body JSON**: N/A (uses form params)
- **Response JSON example**:
```json
{
  "id": 1,
  "name": "Salary",
  "userId": 1
}
```
- **Required Headers**: `Content-Type: application/x-www-form-urlencoded`
- **Query Parameters**:
  - `userId` (required): User ID
  - `name` (required): Category name

---

### 2. Get Income Categories

- **API Name**: Get Income Categories
- **HTTP Method**: GET
- **Endpoint URL**: `/api/income-category`
- **Description / Usage**: Retrieves all income categories for a user.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
[
  {
    "id": 1,
    "name": "Salary",
    "userId": 1
  },
  {
    "id": 2,
    "name": "Freelance",
    "userId": 1
  }
]
```
- **Required Headers**: None
- **Query Parameters**:
  - `userId` (required): User ID

---

### 3. Delete Income Category

- **API Name**: Delete Income Category
- **HTTP Method**: DELETE
- **Endpoint URL**: `/api/income-category/{id}`
- **Description / Usage**: Deletes an income category by ID. Returns 409 if incomes reference this category.
- **Request Body JSON**: N/A
- **Response JSON example**:
```json
"Category deleted"
```
- **Required Headers**: None
- **Query Parameters**:
  - `userId` (required): User ID

---

## Notes

- All `userId` parameters are required and identify the operating user.
- Date/time fields use ISO 8601 format (`YYYY-MM-DDTHH:mm:ss`).
- The service runs on port `8080` by default.
- No authentication/authorization headers are currently required.
- Deleting a category (expense or income) will return HTTP 409 Conflict if any income/expense records reference it.
