// API Configuration
const API_BASE_URL = 'http://localhost:8084/loop-service';

// Global Variables
let currentUser = null;
let currentTab = 'expense';

// DOM Elements
const authSection = document.getElementById('authSection');
const dashboardSection = document.getElementById('dashboardSection');
const bottomNav = document.getElementById('bottomNav');
const loadingSpinner = document.getElementById('loadingSpinner');
const toast = document.getElementById('toast');
const toastMessage = document.getElementById('toastMessage');

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    initializeApp();
    setupEventListeners();
    updateTime();
    setInterval(updateTime, 60000); // Update time every minute
});

function initializeApp() {
    // Check if user is already logged in
    const savedUser = localStorage.getItem('currentUser');
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        showDashboard();
    }
}

function setupEventListeners() {
    // Auth forms
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // Expense form
    document.getElementById('submitExpense').addEventListener('click', handleAddExpense);
    
    // Income form
    document.getElementById('submitIncome').addEventListener('click', handleAddIncome);
    
    // Bottom navigation
    document.querySelectorAll('.nav-item').forEach(item => {
        item.addEventListener('click', handleTabChange);
    });
    
    // Menu button
    document.getElementById('menuBtn').addEventListener('click', handleMenuClick);
}

// Time update function
function updateTime() {
    const now = new Date();
    const timeString = now.toLocaleTimeString('en-US', { 
        hour: '2-digit', 
        minute: '2-digit',
        hour12: false 
    });
    document.querySelector('.time').textContent = timeString;
}

// API Helper Functions
async function apiCall(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`;
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
        },
    };
    
    try {
        showLoading(true);
        const response = await fetch(url, { ...defaultOptions, ...options });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        return data;
    } catch (error) {
        console.error('API Error:', error);
        throw error;
    } finally {
        showLoading(false);
    }
}

// Auth Functions
async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    
    try {
        const response = await apiCall('/user/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        
        currentUser = {
            id: response.userId,
            username: response.username,
            name: response.name,
            balance: response.salary
        };
        
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        showDashboard();
        showToast('Login successful!', 'success');
        
    } catch (error) {
        showToast('Login failed. Please check your credentials.', 'error');
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const userData = {
        name: document.getElementById('registerName').value,
        username: document.getElementById('registerUsername').value,
        password: document.getElementById('registerPassword').value,
        account_balance: parseFloat(document.getElementById('registerBalance').value)
    };
    
    try {
        const response = await apiCall('/user/signUp', {
            method: 'POST',
            body: JSON.stringify(userData)
        });
        
        showToast('Registration successful! Please login.', 'success');
        document.getElementById('registerForm').reset();
        
    } catch (error) {
        showToast('Registration failed. Please try again.', 'error');
    }
}

// Dashboard Functions
function showDashboard() {
    authSection.style.display = 'none';
    dashboardSection.style.display = 'block';
    bottomNav.style.display = 'flex';
    
    updateUserInfo();
    loadCategories();
    loadExpenseHistory();
    updateBalance();
}

function updateUserInfo() {
    if (currentUser) {
        document.getElementById('userName').textContent = currentUser.name;
        document.getElementById('userBalance').textContent = currentUser.balance.toFixed(2);
    }
}

async function updateBalance() {
    if (!currentUser) return;
    
    try {
        const balance = await apiCall(`/api/balance/${currentUser.id}`);
        currentUser.balance = balance;
        document.getElementById('userBalance').textContent = balance.toFixed(2);
        
        // Update progress bar based on expenses vs income
        updateProgressBar();
        
    } catch (error) {
        console.error('Error updating balance:', error);
    }
}

function updateProgressBar() {
    // This is a simplified progress calculation
    // In a real app, you might want to calculate based on budget vs actual spending
    const progressElement = document.getElementById('expenseProgress');
    const progress = Math.min(60, 100); // Default to 60% for demo
    progressElement.style.width = `${progress}%`;
    progressElement.textContent = `${progress.toFixed(1)}%`;
}

// Category Functions
async function loadCategories() {
    if (!currentUser) return;
    
    try {
        const categories = await apiCall(`/api/categories?username=${currentUser.username}`);
        populateCategorySelects(categories);
    } catch (error) {
        console.error('Error loading categories:', error);
    }
}

function populateCategorySelects(categories) {
    const expenseSelect = document.getElementById('expenseCategory');
    const incomeSelect = document.getElementById('incomeCategory');
    
    // Clear existing options except the first one
    expenseSelect.innerHTML = '<option value="">Select Category</option>';
    incomeSelect.innerHTML = '<option value="">Select Category</option>';
    
    // Add category options
    categories.forEach(category => {
        const expenseOption = document.createElement('option');
        expenseOption.value = category.name;
        expenseOption.textContent = category.name;
        expenseSelect.appendChild(expenseOption);
        
        const incomeOption = document.createElement('option');
        incomeOption.value = category.name;
        incomeOption.textContent = category.name;
        incomeSelect.appendChild(incomeOption);
    });
}

// Expense Functions
async function handleAddExpense() {
    if (!currentUser) return;
    
    const amount = parseFloat(document.getElementById('expenseAmount').value);
    const categoryName = document.getElementById('expenseCategory').value;
    const description = document.getElementById('expenseDescription').value;
    
    if (!amount || !categoryName) {
        showToast('Please fill in amount and category.', 'error');
        return;
    }
    
    const expenseData = {
        amount: amount,
        category: { name: categoryName },
        description: description,
        timestamp: new Date().toISOString()
    };
    
    try {
        await apiCall(`/api/expense/${currentUser.username}`, {
            method: 'POST',
            body: JSON.stringify(expenseData)
        });
        
        showToast('Expense added successfully!', 'success');
        document.getElementById('expenseAmount').value = '100';
        document.getElementById('expenseCategory').value = '';
        document.getElementById('expenseDescription').value = '';
        
        updateBalance();
        loadExpenseHistory();
        
    } catch (error) {
        showToast('Failed to add expense. Please try again.', 'error');
    }
}

async function loadExpenseHistory() {
    if (!currentUser) return;
    
    try {
        const expenses = await apiCall(`/api/history/${currentUser.id}`);
        displayExpenseHistory(expenses);
    } catch (error) {
        console.error('Error loading expense history:', error);
    }
}

function displayExpenseHistory(expenses) {
    const historyContainer = document.getElementById('expenseHistory');
    historyContainer.innerHTML = '';
    
    // Group expenses by category
    const groupedExpenses = groupExpensesByCategory(expenses);
    
    Object.keys(groupedExpenses).forEach(categoryName => {
        const categoryExpenses = groupedExpenses[categoryName];
        const card = createExpenseCard(categoryName, categoryExpenses);
        historyContainer.appendChild(card);
    });
}

function groupExpensesByCategory(expenses) {
    const grouped = {};
    
    expenses.forEach(expense => {
        const categoryName = expense.category.name;
        if (!grouped[categoryName]) {
            grouped[categoryName] = [];
        }
        grouped[categoryName].push(expense);
    });
    
    return grouped;
}

function createExpenseCard(categoryName, expenses) {
    const card = document.createElement('div');
    card.className = 'expense-card fade-in';
    
    const totalAmount = expenses.reduce((sum, expense) => sum + expense.amount, 0);
    const categoryClass = getCategoryClass(categoryName);
    
    card.innerHTML = `
        <div class="expense-card-header" onclick="toggleCard(this)">
            <div class="expense-card-title">
                <div class="category-icon ${categoryClass}">
                    <i class="fas fa-arrow-right"></i>
                </div>
                <span>${categoryName}</span>
            </div>
            <div class="d-flex align-items-center">
                <span class="me-3">₹${totalAmount.toFixed(2)}</span>
                <i class="fas fa-chevron-down"></i>
            </div>
        </div>
        <div class="expense-card-content">
            ${expenses.map(expense => `
                <div class="expense-item">
                    <div class="expense-item-details">
                        <div class="expense-item-name">${expense.description || 'No description'}</div>
                        <div class="expense-item-date">${formatDate(expense.timestamp)}</div>
                    </div>
                    <div class="expense-item-amount">₹${expense.amount.toFixed(2)}</div>
                </div>
            `).join('')}
        </div>
    `;
    
    return card;
}

function toggleCard(header) {
    const content = header.nextElementSibling;
    const icon = header.querySelector('.fa-chevron-down, .fa-chevron-up');
    
    if (content.classList.contains('expanded')) {
        content.classList.remove('expanded');
        icon.classList.remove('fa-chevron-up');
        icon.classList.add('fa-chevron-down');
    } else {
        content.classList.add('expanded');
        icon.classList.remove('fa-chevron-down');
        icon.classList.add('fa-chevron-up');
    }
}

// Income Functions
async function handleAddIncome() {
    if (!currentUser) return;
    
    const amount = parseFloat(document.getElementById('incomeAmount').value);
    const categoryName = document.getElementById('incomeCategory').value;
    const description = document.getElementById('incomeDescription').value;
    
    if (!amount || !categoryName) {
        showToast('Please fill in amount and category.', 'error');
        return;
    }
    
    const incomeData = {
        amount: amount,
        category: { name: categoryName },
        description: description,
        date: new Date().toISOString()
    };
    
    try {
        await apiCall(`/income/${currentUser.username}`, {
            method: 'POST',
            body: JSON.stringify(incomeData)
        });
        
        showToast('Income added successfully!', 'success');
        document.getElementById('incomeAmount').value = '';
        document.getElementById('incomeCategory').value = '';
        document.getElementById('incomeDescription').value = '';
        
        updateBalance();
        
    } catch (error) {
        showToast('Failed to add income. Please try again.', 'error');
    }
}

// Tab Navigation
function handleTabChange(event) {
    const tab = event.currentTarget.dataset.tab;
    
    // Update active tab
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
    });
    event.currentTarget.classList.add('active');
    
    // Show/hide sections
    const incomeSection = document.getElementById('incomeSection');
    const expenseUsageSection = document.querySelector('.expense-usage-section');
    
    if (tab === 'income') {
        incomeSection.style.display = 'block';
        expenseUsageSection.style.display = 'none';
    } else {
        incomeSection.style.display = 'none';
        expenseUsageSection.style.display = 'block';
    }
    
    currentTab = tab;
}

// Utility Functions
function getCategoryClass(categoryName) {
    const categoryMap = {
        'groceries': 'groceries',
        'dining': 'dining',
        'transport': 'transport',
        'entertainment': 'entertainment',
        'utilities': 'utilities'
    };
    
    const lowerName = categoryName.toLowerCase();
    return categoryMap[lowerName] || 'other';
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
        month: 'numeric',
        day: 'numeric',
        year: 'numeric'
    });
}

function showLoading(show) {
    loadingSpinner.style.display = show ? 'flex' : 'none';
}

function showToast(message, type = 'info') {
    toastMessage.textContent = message;
    
    // Update toast styling based on type
    const toastElement = document.getElementById('toast');
    toastElement.className = `toast ${type}`;
    
    // Show toast
    const bsToast = new bootstrap.Toast(toastElement);
    bsToast.show();
}

function handleMenuClick() {
    // For now, just show a simple alert
    // In a real app, this could open a side menu or settings
    alert('Menu clicked! This could open settings or navigation.');
}

// Logout function
function logout() {
    localStorage.removeItem('currentUser');
    currentUser = null;
    
    authSection.style.display = 'block';
    dashboardSection.style.display = 'none';
    bottomNav.style.display = 'none';
    
    showToast('Logged out successfully.', 'info');
}
