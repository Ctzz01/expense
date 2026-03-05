(function () {
  'use strict';

  // DOM elements
  const budgetInput = document.getElementById('budgetInput');
  const saveBudgetBtn = document.getElementById('saveBudgetBtn');

  const budgetDisplay = document.getElementById('budgetDisplay');
  const spentDisplay = document.getElementById('spentDisplay');
  const remainingDisplay = document.getElementById('remainingDisplay');
  const remainingPercentLabel = document.getElementById('remainingPercentLabel');
  const progressBar = document.getElementById('progressBar');
  const overBudgetAlert = document.getElementById('overBudgetAlert');
  const overBudgetAmount = document.getElementById('overBudgetAmount');

  const currencySymbolSpan = document.getElementById('currencySymbolSpan');
  const currencySymbolSpan2 = document.getElementById('currencySymbolSpan2');

  const expenseForm = document.getElementById('expenseForm');
  const dateInput = document.getElementById('dateInput');
  const categoryInput = document.getElementById('categoryInput');
  const descriptionInput = document.getElementById('descriptionInput');
  const amountInput = document.getElementById('amountInput');

  const filterCategory = document.getElementById('filterCategory');
  const sortBy = document.getElementById('sortBy');
  const clearAllBtn = document.getElementById('clearAllBtn');
  const resetAllBtn = document.getElementById('resetAllBtn');
  const historyBody = document.getElementById('historyBody');
  const emptyState = document.getElementById('emptyState');
  const categoryBreakdown = document.getElementById('categoryBreakdown');
  const incomeCategoryBreakdown = document.getElementById('incomeCategoryBreakdown');
  // Date filter controls - expenses
  const expDateFilterBtn = document.getElementById('expDateFilterBtn');
  const expDateFilterPanel = document.getElementById('expDateFilterPanel');
  const expDateFilterSelect = document.getElementById('expDateFilterSelect');
  const expDateFrom = document.getElementById('expDateFrom');
  const expDateTo = document.getElementById('expDateTo');
  // Category date filter controls - expenses
  const expCatDateFilterBtn = document.getElementById('expCatDateFilterBtn');
  const expCatDateFilterPanel = document.getElementById('expCatDateFilterPanel');
  const expCatDateFilterSelect = document.getElementById('expCatDateFilterSelect');
  const expCatDateFrom = document.getElementById('expCatDateFrom');
  const expCatDateTo = document.getElementById('expCatDateTo');
  // Date filter controls - income
  const incDateFilterBtn = document.getElementById('incDateFilterBtn');
  const incDateFilterPanel = document.getElementById('incDateFilterPanel');
  const incDateFilterSelect = document.getElementById('incDateFilterSelect');
  const incDateFrom = document.getElementById('incDateFrom');
  const incDateTo = document.getElementById('incDateTo');

  // Backend config
  const CONTEXT_PATH = (function() {
    const path = window.location.pathname;
    const m = path.match(/^\/(?:([^\/]+))(?:\/|$)/);
    return m ? `/${m[1]}` : '';
  })();
  const API_BASE = (window.ENV_API_BASE || `${window.location.origin}${CONTEXT_PATH}`).replace(/\/$/, '');
  const DEFAULT_USERNAME = 'guest';

  // Income page elements (may be null on expenses page)
  const incomeForm = document.getElementById('incomeForm');
  const incomeDateInput = document.getElementById('incomeDateInput');
  const incomeCategoryInput = document.getElementById('incomeCategoryInput');
  const incomeDescriptionInput = document.getElementById('incomeDescriptionInput');
  const incomeAmountInput = document.getElementById('incomeAmountInput');
  const incomeFilterCategory = document.getElementById('incomeFilterCategory');
  const incomeSortBy = document.getElementById('incomeSortBy');
  const incomeHistoryBody = document.getElementById('incomeHistoryBody');
  const resetAllIncomeBtn = document.getElementById('resetAllIncomeBtn');
  // Categories page elements
  const expCatForm = document.getElementById('expCatForm');
  const expCatName = document.getElementById('expCatName');
  const expCatChips = document.getElementById('expCatChips');
  const incCatForm = document.getElementById('incCatForm');
  const incCatName = document.getElementById('incCatName');
  const incCatChips = document.getElementById('incCatChips');

  // Local storage keys (only for remembering chosen username)
  const STORAGE_KEYS = {
    username: 'expense_tracker_username',
  };

  // Utils
  const CURRENCY_CODE = 'INR';
  function formatMoney(amount) {
    try {
      return new Intl.NumberFormat(undefined, {
        style: 'currency',
        currency: CURRENCY_CODE,
        maximumFractionDigits: 2,
      }).format(Number(amount) || 0);
    } catch (_) {
      return `₹${(Number(amount) || 0).toFixed(2)}`;
    }
  }

  function renderCategoryBreakdown(expenses) {
    const totalBudget = getTotalIncome();
    const totalsByCategory = {};
    for (const e of expenses) {
      const amt = Number(e.amount || 0);
      if (amt <= 0) continue;
      totalsByCategory[e.category] = (totalsByCategory[e.category] || 0) + amt;
    }

    if (!categoryBreakdown) return;
    categoryBreakdown.innerHTML = '';

    const categoriesUsed = Object.keys(totalsByCategory);
    if (categoriesUsed.length === 0) {
      // nothing to show
      return;
    }

    // Stable order by name
    categoriesUsed.sort((a,b) => a.localeCompare(b));
    categoriesUsed.forEach((cat) => {
      const spent = totalsByCategory[cat] || 0;
      const percentOfBudget = totalBudget > 0 ? Math.min(100, Math.round((spent / totalBudget) * 100)) : 0;
      const icon = categoryIcon(cat);
      const color = percentShareColor(percentOfBudget);

      const tile = document.createElement('div');
      tile.className = 'category-tile';
      tile.innerHTML = `
        <div class="category-avatar" style="border-color:${color.border}; background:${color.bg}; color:${color.text}">
          <i class="bi ${icon}"></i>
        </div>
        <div class="category-info">
          <div class="name">${cat}</div>
          <div class="amount">${formatMoney(spent)}</div>
          <div class="percent">${percentOfBudget}% of budget</div>
        </div>
      `;
      categoryBreakdown.appendChild(tile);
    });
  }

  function loadState() {
    return { user: null, incomes: [], expenses: [] };
  }

  function saveState(_) {}

  // ---------- API helpers ----------
  async function apiGet(path) {
    const res = await fetch(`${API_BASE}${path}`);
    if (!res.ok) throw new Error(`GET ${path} failed`);
    return res.json();
  }
  async function apiPost(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`POST ${path} failed`);
    const ct = res.headers.get('content-type') || '';
    if (ct.includes('application/json')) {
      return res.json();
    }
    // Backend may return plain text (e.g., "Expense added successfully!")
    return res.text();
  }
  async function apiDelete(path) {
    const res = await fetch(`${API_BASE}${path}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(`DELETE ${path} failed`);
    try { return await res.json(); } catch { return null; }
  }
  async function apiPut(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error(`PUT ${path} failed`);
    return res.json();
  }

  async function ensureUser(username) {
    const res = await fetch(`${API_BASE}/user/ensure?username=${encodeURIComponent(username)}`, { method: 'POST' });
    if (!res.ok) throw new Error('Unable to ensure user');
    return res.json();
  }

  async function fetchExpenses(userId) {
    // Optional query params for date filtering
    const params = buildDateParams(expDateFilterSelect, expDateFrom, expDateTo);
    const list = await apiGet(`/api/history/${userId}${params}`);
    // Map to frontend model
    return list.map((e) => ({
      id: e.id,
      date: (e.timestamp || '').slice(0, 10),
      category: (e.category && e.category.name) ? capitalize(e.category.name) : 'Other',
      description: e.description || '',
      amount: Number(e.amount || 0),
    }));
  }
  async function addExpenseRemote(username, expense) {
    const payload = {
      amount: expense.amount,
      description: expense.description,
      timestamp: `${expense.date}T12:00:00`,
      category: { name: expense.category },
    };
    await apiPost(`/api/expense/${encodeURIComponent(username)}`, payload);
  }
  async function deleteExpenseRemote(expenseId) {
    await apiDelete(`/api/expense/${expenseId}`);
  }

  async function fetchIncomes(username) {
    // Income controller currently has no date filters; if needed, backend can be extended.
    // For now, client-side filter by date if custom is chosen.
    const list = await apiGet(`/income/${encodeURIComponent(username)}`);
    return list.map((i) => ({
      id: i.id,
      date: (i.date || '').slice(0, 10),
      category: (i.category && i.category.name) ? capitalize(i.category.name) : 'Other',
      description: i.description || '',
      amount: Number(i.amount || 0),
    }));
  }
  async function addIncomeRemote(username, income) {
    const payload = {
      amount: income.amount,
      description: income.description,
      date: `${income.date}T12:00:00`,
      category: { name: income.category },
    };
    await apiPost(`/income/${encodeURIComponent(username)}`, payload);
  }
  async function deleteIncomeRemote(incomeId) {
    await apiDelete(`/income/${incomeId}`);
  }

  // Category APIs
  async function fetchExpenseCategories(username) {
    return apiGet(`/api/categories?username=${encodeURIComponent(username)}`);
  }
  async function addExpenseCategory(username, name) {
    // CategoryController expects a Category object {name}
    try {
      return await apiPost(`/api/categories?username=${encodeURIComponent(username)}`, { name });
    } catch (e) {
      alert('Category already exists or invalid.');
      throw e;
    }
  }
  async function deleteExpenseCategory(username, id) {
    try {
      return await apiDelete(`/api/categories/${id}?username=${encodeURIComponent(username)}`);
    } catch (e) {
      alert('Unable to delete this category. It may be system-defined or referenced by expenses.');
      throw e;
    }
  }
  async function fetchIncomeCategories(username) {
    return apiGet(`/api/income-category?username=${encodeURIComponent(username)}`);
  }
  async function addIncomeCategory(username, name) {
    const url = `/api/income-category?username=${encodeURIComponent(username)}&name=${encodeURIComponent(name)}`;
    // this endpoint expects params; using POST without body
    const res = await fetch(`${API_BASE}${url}`, { method: 'POST' });
    if (!res.ok) throw new Error('Add income category failed');
    return res.json();
  }
  async function deleteIncomeCategory(username, id) {
    return apiDelete(`/api/income-category/${id}?username=${encodeURIComponent(username)}`);
  }

  function capitalize(s) { return (s || '').charAt(0).toUpperCase() + (s || '').slice(1); }

  function computeTotals(expenses) {
    const spent = expenses.reduce((sum, e) => sum + Number(e.amount || 0), 0);
    return { spent };
  }

  function setProgress(spent, budget) {
    const remaining = Math.max(budget - spent, 0);
    const ratioRemaining = budget > 0 ? (remaining / budget) : 0;
    const percent = Math.min(Math.max(Math.round(ratioRemaining * 100), 0), 100);

    // When remaining is 0 (and there was a budget), make the whole loader red for strong emphasis
    const zeroRemaining = budget > 0 && remaining === 0;
    const widthToShow = zeroRemaining ? 100 : percent;
    progressBar.style.width = `${widthToShow}%`;
    progressBar.setAttribute('aria-valuenow', String(percent));
    remainingPercentLabel.textContent = `${percent}%`;

    // Color thresholds: >=80% green, 20-79% orange, <20% red
    if (zeroRemaining) {
      progressBar.style.background = 'linear-gradient(90deg,#ef4444,#f87171)';
    } else if (percent >= 80) {
      progressBar.style.background = 'linear-gradient(90deg,#22c55e,#34d399)';
    } else if (percent >= 20) {
      progressBar.style.background = 'linear-gradient(90deg,#f59e0b,#f97316)';
    } else {
      progressBar.style.background = 'linear-gradient(90deg,#ef4444,#f87171)';
    }

    if (spent > budget) {
      overBudgetAlert.classList.remove('d-none');
      overBudgetAmount.textContent = formatMoney(spent - budget);
    } else {
      overBudgetAlert.classList.add('d-none');
    }

    budgetDisplay.textContent = formatMoney(budget);
    spentDisplay.textContent = formatMoney(spent);
    remainingDisplay.textContent = formatMoney(remaining);
  }

  function renderHistory(expenses, filters) {
    const { category, sort } = filters;
    let list = [...expenses];

    // Filter
    if (category && category !== 'ALL') {
      list = list.filter((e) => e.category === category);
    }

    // Sort
    const sorters = {
      date_desc: (a, b) => new Date(b.date) - new Date(a.date),
      date_asc: (a, b) => new Date(a.date) - new Date(b.date),
      amount_desc: (a, b) => b.amount - a.amount,
      amount_asc: (a, b) => a.amount - b.amount,
      category_asc: (a, b) => a.category.localeCompare(b.category),
      category_desc: (a, b) => b.category.localeCompare(a.category),
    };
    list.sort(sorters[sort] || sorters.date_desc);

    historyBody.innerHTML = '';
    if (list.length === 0) {
      emptyState.classList.remove('d-none');
      return;
    }
    emptyState.classList.add('d-none');

    list.forEach((e) => {
      const tr = document.createElement('tr');
      const icon = categoryIcon(e.category);
      tr.innerHTML = `
        <td>${e.date}</td>
        <td><span class="category-badge"><i class="bi ${icon}"></i>${e.category}</span></td>
        <td>${escapeHtml(e.description)}</td>
        <td class="text-end fw-semibold">${formatMoney(e.amount)}</td>
        <td class="text-end">
          <button class="btn btn-sm btn-outline-light me-1" data-action="edit" data-id="${e.id}"><i class="bi bi-pencil"></i></button>
          <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${e.id}"><i class="bi bi-trash"></i></button>
        </td>
      `;
      historyBody.appendChild(tr);
    });
  }

  function escapeHtml(str) {
    return String(str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;');
  }

  function setAmountError(message) {
    const inputGroup = amountInput.closest('.input-group') || amountInput.parentElement;
    let errorEl = document.getElementById('amountError');
    if (message) {
      amountInput.classList.add('is-invalid');
      if (!errorEl) {
        errorEl = document.createElement('div');
        errorEl.id = 'amountError';
        errorEl.className = 'invalid-feedback d-block mt-1';
        inputGroup.parentElement.appendChild(errorEl);
      }
      errorEl.textContent = message;
    } else {
      amountInput.classList.remove('is-invalid');
      if (errorEl && errorEl.parentElement) errorEl.parentElement.removeChild(errorEl);
    }
  }

  function upsertExpenseLocal(expense) {
    const idx = state.expenses.findIndex((x) => x.id === expense.id);
    if (idx >= 0) state.expenses[idx] = expense; else state.expenses.push(expense);
  }
  function deleteExpenseLocal(id) {
    state.expenses = state.expenses.filter((x) => x.id !== id);
  }

  // Income helpers
  function upsertIncome(income) {
    const idx = state.incomes.findIndex((x) => x.id === income.id);
    if (idx >= 0) {
      state.incomes[idx] = income;
    } else {
      state.incomes.push(income);
    }
    saveState({ incomes: state.incomes });
  }

  function deleteIncome(id) {
    state.incomes = state.incomes.filter((x) => x.id !== id);
    saveState({ incomes: state.incomes });
  }

  function getTotalIncome() {
    return state.incomes.reduce((sum, i) => sum + Number(i.amount || 0), 0);
  }

  async function refreshExpensesUI() {
    // Re-fetch remote data (both expenses and incomes) so totals are accurate
    if (state.user) {
      state.expenses = await fetchExpenses(state.user.id);
      state.incomes = await fetchIncomes(state.user.username);
    }
    const { spent } = computeTotals(state.expenses);
    const totalIncome = getTotalIncome();
    setProgress(spent, totalIncome);
    updateFilterOptions(state.expenses);
    renderHistory(state.expenses, { category: filterCategory ? filterCategory.value : 'ALL', sort: sortBy ? sortBy.value : 'date_desc' });
    // Re-render categories breakdown using the category date filter selection
    const catParams = buildDateParams(expCatDateFilterSelect, expCatDateFrom, expCatDateTo);
    if (catParams) {
      // If a filter is selected for categories view, refetch expenses just for that view
      const list = await apiGet(`/api/history/${state.user.id}${catParams}`);
      const mapped = list.map((e) => ({
        id: e.id,
        date: (e.timestamp || '').slice(0, 10),
        category: (e.category && e.category.name) ? capitalize(e.category.name) : 'Other',
        description: e.description || '',
        amount: Number(e.amount || 0),
      }));
      renderCategoryBreakdown(mapped);
    } else {
      renderCategoryBreakdown(state.expenses);
    }
  }
  // Category date filters (expenses)
  if (expCatDateFilterBtn) expCatDateFilterBtn.addEventListener('click', () => {
    expCatDateFilterPanel.classList.toggle('d-none');
  });
  if (expCatDateFilterSelect) expCatDateFilterSelect.addEventListener('change', () => {
    toggleCustomRange(expCatDateFilterSelect, '.exp-cat-custom');
    refreshExpensesUI();
  });
  if (expCatDateFrom) expCatDateFrom.addEventListener('change', () => refreshExpensesUI());
  if (expCatDateTo) expCatDateTo.addEventListener('change', () => refreshExpensesUI());

  async function refreshIncomeUI() {
    // Re-fetch incomes and keep expenses for progress
    if (state.user) {
      state.incomes = await fetchIncomes(state.user.username);
      // Also refresh expenses to compute remaining correctly
      state.expenses = await fetchExpenses(state.user.id);
    }
    const { spent } = computeTotals(state.expenses);
    const totalIncome = getTotalIncome();
    setProgress(spent, totalIncome);
    updateIncomeFilterOptions(state.incomes);
    const incomeList = applyClientDateFilter(state.incomes, incDateFilterSelect, incDateFrom, incDateTo, 'date');
    renderIncomeHistory(incomeList, { category: incomeFilterCategory ? incomeFilterCategory.value : 'ALL', sort: incomeSortBy ? incomeSortBy.value : 'date_desc' });
    renderIncomeCategoryBreakdown(state.incomes);
  }

  function updateFilterOptions(expenses) {
    if (!filterCategory) return;
    const current = filterCategory.value;
    const set = new Set();
    for (const e of expenses) {
      if (e && e.category) set.add(e.category);
    }
    const list = Array.from(set).sort((a,b) => a.localeCompare(b));
    const options = ['<option value="ALL">All Categories</option>']
      .concat(list.map((c) => `<option value="${c}">${c}</option>`))
      .join('');
    filterCategory.innerHTML = options;
    // restore selection if still valid
    if (current && (current === 'ALL' || set.has(current))) {
      filterCategory.value = current;
    } else {
      filterCategory.value = 'ALL';
    }
  }
  
  function categoryIcon(cat) {
    const map = {
      Food: 'bi-egg-fried',
      Transport: 'bi-bus-front',
      Entertainment: 'bi-controller',
      Bills: 'bi-receipt',
      Shopping: 'bi-bag',
      Health: 'bi-heart-pulse',
      Education: 'bi-mortarboard',
      Other: 'bi-three-dots',
    };
    return map[cat] || 'bi-tag';
  }

  function percentShareColor(percentOfBudget) {
    if (percentOfBudget >= 60) {
      return { border: '#ef4444', bg: 'rgba(239,68,68,0.15)', text: '#7f1d1d' }; // red for major chunk
    }
    if (percentOfBudget >= 30) {
      return { border: '#f59e0b', bg: 'rgba(245,158,11,0.15)', text: '#7c2d12' }; // orange for notable chunk
    }
    return { border: '#34d399', bg: 'rgba(34,197,94,0.15)', text: '#065f46' }; // green for small chunk
  }

  // State
  let state = loadState();

  // Initialize defaults
  (async function init() {
    const today = new Date().toISOString().slice(0, 10);
    if (dateInput) dateInput.value = today;
    if (incomeDateInput) incomeDateInput.value = today;

    if (currencySymbolSpan) currencySymbolSpan.textContent = '₹';
    if (currencySymbolSpan2) currencySymbolSpan2.textContent = '₹';

    // Initial render depending on page
    // Resolve username
    const u = new URLSearchParams(location.search).get('u') || localStorage.getItem(STORAGE_KEYS.username) || DEFAULT_USERNAME;
    localStorage.setItem(STORAGE_KEYS.username, u);
    state.user = await ensureUser(u);
    if (expenseForm) {
      await populateExpenseCategorySelect();
      await refreshExpensesUI();
    }
    if (incomeForm) {
      await populateIncomeCategorySelect();
      await refreshIncomeUI();
    }
    if (expCatForm || incCatForm) await refreshCategoriesUI();
  })();
  async function populateExpenseCategorySelect() {
    if (!categoryInput || !state.user) return;
    const cats = await fetchExpenseCategories(state.user.username);
    renderCategoryOptions(categoryInput, cats);
  }

  async function populateIncomeCategorySelect() {
    if (!incomeCategoryInput || !state.user) return;
    const cats = await fetchIncomeCategories(state.user.username);
    renderCategoryOptions(incomeCategoryInput, cats);
  }

  function renderCategoryOptions(selectEl, cats) {
    const options = [
      '<option value="" disabled selected>Choose...</option>',
      ...cats.map((c) => `<option value="${escapeHtml(c.name)}">${capitalize(c.name)}</option>`),
    ].join('');
    selectEl.innerHTML = options;
  }

  // Handlers
  if (saveBudgetBtn && budgetInput) {
    saveBudgetBtn.addEventListener('click', () => {
      alert('Income is now managed on the Income page. Add income there.');
    });
  }

  // currency select removed (locked to INR)

  if (expenseForm) expenseForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const expense = {
      id: cryptoRandomId(),
      date: dateInput.value,
      category: categoryInput.value,
      description: descriptionInput.value.trim(),
      amount: Number(amountInput.value || '0'),
    };

    if (!expense.date || !expense.category || !expense.description || !(expense.amount > 0)) {
      return; // basic validation
    }

    // remaining = budget - total spent so far
    const { spent } = computeTotals(state.expenses);
    const remaining = Math.max(getTotalIncome() - spent, 0);
    if (expense.amount > remaining) {
      setAmountError(`Amount exceeds remaining balance of ${formatMoney(remaining)}`);
      amountInput.focus();
      return;
    }
    setAmountError('');

    try {
      await addExpenseRemote(state.user.username, expense);
    } finally {
      // Ensure the latest data (including server-side calculations) is reflected
      // by performing a full page reload after successful add (even if backend returned text).
      window.location.reload();
    }
  });

  if (clearAllBtn) clearAllBtn.addEventListener('click', async () => {
    if (!confirm('Clear all expenses?')) return;
    // delete all remotely
    const ids = (state.expenses || []).map((e) => e.id);
    await Promise.all(ids.map((id) => deleteExpenseRemote(id)));
    await refreshExpensesUI();
  });

  if (resetAllBtn) resetAllBtn.addEventListener('click', async () => {
    if (!confirm('Reset all data (budget and expenses)?')) return;
    // delete all expenses and incomes for this user
    const expenseIds = (state.expenses || []).map((e) => e.id);
    await Promise.all(expenseIds.map((id) => deleteExpenseRemote(id)));
    const incomeIds = (state.incomes || []).map((i) => i.id);
    await Promise.all(incomeIds.map((id) => deleteIncomeRemote(id)));
    state.incomes = []; state.expenses = [];
    if (budgetInput) budgetInput.value = '';
    if (expenseForm) expenseForm.reset();
    if (dateInput) dateInput.value = new Date().toISOString().slice(0, 10);
    if (expenseForm) await refreshExpensesUI();
    if (incomeForm) await refreshIncomeUI();
  });

  if (filterCategory) filterCategory.addEventListener('change', () => {
    renderHistory(state.expenses, { category: filterCategory.value, sort: sortBy.value });
  });

  if (sortBy) sortBy.addEventListener('change', () => {
    renderHistory(state.expenses, { category: filterCategory.value, sort: sortBy.value });
  });

  // Delegate edit/delete
  if (historyBody) historyBody.addEventListener('click', async (e) => {
    const btn = e.target.closest('button[data-action]');
    if (!btn) return;
    const id = btn.getAttribute('data-id');
    const action = btn.getAttribute('data-action');
    const item = state.expenses.find((x) => x.id === id);
    if (!item) return;

    if (action === 'delete') {
      await deleteExpenseRemote(id);
      await refreshExpensesUI();
      return;
    }

    if (action === 'edit') {
      // Pre-fill form for editing
      dateInput.value = item.date;
      categoryInput.value = item.category;
      descriptionInput.value = item.description;
      amountInput.value = String(item.amount);

      // Remove old one; adding will upsert new edited entry
      await deleteExpenseRemote(id);
      await refreshExpensesUI();
    }
  });

  // Income page events
  if (incomeForm) incomeForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const income = {
      id: cryptoRandomId(),
      date: incomeDateInput.value,
      category: incomeCategoryInput.value,
      description: incomeDescriptionInput.value.trim(),
      amount: Number(incomeAmountInput.value || '0'),
    };
    if (!income.date || !income.category || !income.description || !(income.amount > 0)) return;
    await addIncomeRemote(state.user.username, income);
    incomeForm.reset();
    if (incomeDateInput) incomeDateInput.value = new Date().toISOString().slice(0, 10);
    await refreshIncomeUI();
  });

  if (resetAllIncomeBtn) resetAllIncomeBtn.addEventListener('click', async () => {
    if (!confirm('Clear all income records?')) return;
    const ids = (state.incomes || []).map((i) => i.id);
    await Promise.all(ids.map((id) => deleteIncomeRemote(id)));
    await refreshIncomeUI();
  });

  // Categories page handlers
  if (expCatForm) expCatForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = (expCatName.value || '').trim();
    if (!name) return;
    await addExpenseCategory(state.user.username, name);
    expCatName.value = '';
    await refreshCategoriesUI();
  });
  if (incCatForm) incCatForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const name = (incCatName.value || '').trim();
    if (!name) return;
    await addIncomeCategory(state.user.username, name);
    incCatName.value = '';
    await refreshCategoriesUI();
  });

  async function refreshCategoriesUI() {
    if (!state.user) return;
    const [expCats, incCats] = await Promise.all([
      fetchExpenseCategories(state.user.username),
      fetchIncomeCategories(state.user.username),
    ]);
    if (expCatChips) renderChips(expCatChips, expCats, {
      onDelete: (id) => deleteExpenseCategory(state.user.username, id),
      onEdit: (id, newName) => updateExpenseCategory(state.user.username, id, newName),
    });
    if (incCatChips) renderChips(incCatChips, incCats, {
      onDelete: (id) => deleteIncomeCategory(state.user.username, id),
      onEdit: (id, newName) => updateIncomeCategory(state.user.username, id, newName),
    });
  }

  async function updateExpenseCategory(username, id, name) {
    const res = await fetch(`${API_BASE}/api/categories/${id}?username=${encodeURIComponent(username)}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name }),
    });
    if (!res.ok) { alert('Rename failed (duplicate or error)'); return; }
  }
  async function updateIncomeCategory(username, id, name) {
    // Not present in backend; we’ll skip edit for income categories for now
    alert('Edit for income categories not available yet.');
  }

  function renderChips(container, cats, actions) {
    container.innerHTML = '';
    cats.forEach((c) => {
      const chip = document.createElement('div');
      chip.className = 'chip';
      chip.innerHTML = `<span class="label">${escapeHtml(c.name)}</span>`;
      // Edit inline on click label
      const label = chip.querySelector('.label');
      label.addEventListener('click', async () => {
        const newName = prompt('Rename category', c.name);
        if (newName && newName.trim() && actions.onEdit) {
          await actions.onEdit(c.id, newName.trim());
          await refreshCategoriesUI();
        }
      });
      const x = document.createElement('button');
      x.className = 'remove';
      x.innerHTML = '<i class="bi bi-x"></i>';
      x.addEventListener('click', async () => { if (actions.onDelete) { await actions.onDelete(c.id); await refreshCategoriesUI(); } });
      chip.appendChild(x);
      container.appendChild(chip);
    });
  }

  if (incomeFilterCategory) incomeFilterCategory.addEventListener('change', () => {
    renderIncomeHistory(state.incomes, { category: incomeFilterCategory.value, sort: incomeSortBy.value });
  });
  if (incomeSortBy) incomeSortBy.addEventListener('change', () => {
    renderIncomeHistory(state.incomes, { category: incomeFilterCategory.value, sort: incomeSortBy.value });
  });

  // Date filter interactions — expenses
  if (expDateFilterBtn) expDateFilterBtn.addEventListener('click', () => {
    expDateFilterPanel.classList.toggle('d-none');
  });
  if (expDateFilterSelect) expDateFilterSelect.addEventListener('change', () => {
    toggleCustomRange(expDateFilterSelect, '.exp-custom');
    refreshExpensesUI();
  });
  if (expDateFrom) expDateFrom.addEventListener('change', () => refreshExpensesUI());
  if (expDateTo) expDateTo.addEventListener('change', () => refreshExpensesUI());

  // Date filter interactions — income
  if (incDateFilterBtn) incDateFilterBtn.addEventListener('click', () => {
    incDateFilterPanel.classList.toggle('d-none');
  });
  if (incDateFilterSelect) incDateFilterSelect.addEventListener('change', () => {
    toggleCustomRange(incDateFilterSelect, '.inc-custom');
    refreshIncomeUI();
  });
  if (incDateFrom) incDateFrom.addEventListener('change', () => refreshIncomeUI());
  if (incDateTo) incDateTo.addEventListener('change', () => refreshIncomeUI());

  function toggleCustomRange(selectEl, selector) {
    const show = selectEl && selectEl.value === 'custom';
    document.querySelectorAll(selector).forEach((el) => el.classList.toggle('d-none', !show));
  }

  function buildDateParams(selectEl, fromEl, toEl) {
    if (!selectEl) return '';
    const val = selectEl.value;
    if (val === 'custom' && fromEl && toEl && fromEl.value && toEl.value) {
      return `?startDate=${encodeURIComponent(fromEl.value)}&endDate=${encodeURIComponent(toEl.value)}`;
    }
    if (['today','week','month','year'].includes(val)) {
      return `?filter=${encodeURIComponent(val)}`;
    }
    return '';
  }

  function applyClientDateFilter(items, selectEl, fromEl, toEl, field) {
    if (!selectEl || !selectEl.value) return items;
    const val = selectEl.value;
    if (val !== 'custom' || !(fromEl && toEl && fromEl.value && toEl.value)) return items;
    const from = new Date(fromEl.value);
    const to = new Date(toEl.value);
    return items.filter((it) => {
      const d = new Date(it[field]);
      return d >= from && d <= to;
    });
  }

  function incomeIcon(cat) {
    const map = {
      Salary: 'bi-cash-stack',
      Freelance: 'bi-laptop',
      Business: 'bi-building',
      Investments: 'bi-graph-up-arrow',
      Gift: 'bi-gift',
      Other: 'bi-three-dots',
    };
    return map[cat] || 'bi-coin';
  }

  function renderIncomeHistory(incomes, filters) {
    if (!incomeHistoryBody) return;
    const { category, sort } = filters;
    let list = [...incomes];
    if (category && category !== 'ALL') list = list.filter((i) => i.category === category);
    const sorters = {
      date_desc: (a, b) => new Date(b.date) - new Date(a.date),
      date_asc: (a, b) => new Date(a.date) - new Date(b.date),
      amount_desc: (a, b) => b.amount - a.amount,
      amount_asc: (a, b) => a.amount - b.amount,
      category_asc: (a, b) => a.category.localeCompare(b.category),
      category_desc: (a, b) => b.category.localeCompare(a.category),
    };
    list.sort(sorters[sort] || sorters.date_desc);

    incomeHistoryBody.innerHTML = '';
    list.forEach((i) => {
      const tr = document.createElement('tr');
      const icon = incomeIcon(i.category);
      tr.innerHTML = `
        <td>${i.date}</td>
        <td><span class="category-badge"><i class="bi ${icon}"></i>${i.category}</span></td>
        <td>${escapeHtml(i.description)}</td>
        <td class="text-end fw-semibold">${formatMoney(i.amount)}</td>
      `;
      incomeHistoryBody.appendChild(tr);
    });
  }

  function updateIncomeFilterOptions(incomes) {
    if (!incomeFilterCategory) return;
    const current = incomeFilterCategory.value;
    const set = new Set();
    for (const i of incomes) { if (i && i.category) set.add(i.category); }
    const list = Array.from(set).sort((a,b) => a.localeCompare(b));
    const options = ['<option value="ALL">All Sources</option>']
      .concat(list.map((c) => `<option value="${c}">${c}</option>`))
      .join('');
    incomeFilterCategory.innerHTML = options;
    if (current && (current === 'ALL' || set.has(current))) {
      incomeFilterCategory.value = current;
    } else {
      incomeFilterCategory.value = 'ALL';
    }
  }

  function renderIncomeCategoryBreakdown(incomes) {
    if (!incomeCategoryBreakdown) return;
    const totalsByCategory = {};
    for (const i of incomes) {
      const amt = Number(i.amount || 0);
      if (amt <= 0) continue;
      totalsByCategory[i.category] = (totalsByCategory[i.category] || 0) + amt;
    }
    incomeCategoryBreakdown.innerHTML = '';
    const cats = Object.keys(totalsByCategory).sort((a,b) => a.localeCompare(b));
    cats.forEach((cat) => {
      const amount = totalsByCategory[cat];
      const icon = incomeIcon(cat);
      const tile = document.createElement('div');
      tile.className = 'category-tile';
      tile.innerHTML = `
        <div class="category-avatar" style="border-color:#34d399; background:rgba(34,197,94,0.15); color:#065f46">
          <i class="bi ${icon}"></i>
        </div>
        <div class="category-info">
          <div class="name">${cat}</div>
          <div class="amount">${formatMoney(amount)}</div>
        </div>
      `;
      incomeCategoryBreakdown.appendChild(tile);
    });
  }

  function cryptoRandomId() {
    if (window.crypto && crypto.randomUUID) return crypto.randomUUID();
    return 'id-' + Math.random().toString(16).slice(2) + Date.now().toString(16);
  }
})();


