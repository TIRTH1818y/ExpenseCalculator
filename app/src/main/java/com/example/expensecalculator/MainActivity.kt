package com.example.expensecalculator

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton

/**
 * Main dashboard screen of the Expense Calculator application.
 *
 * Displays financial summary cards, category-wise spending breakdown,
 * interactive search and filtering, and the chronological list of expenses.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var repository: ExpenseRepository
    private lateinit var adapter: ExpenseAdapter

    // Summary Card Views
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvHighestExpenseAmount: TextView
    private lateinit var tvHighestExpenseTitle: TextView
    private lateinit var tvTransactionCount: TextView

    // Category Breakdown Views
    private lateinit var tvBreakdownFood: TextView
    private lateinit var tvBreakdownTravel: TextView
    private lateinit var tvBreakdownShopping: TextView
    private lateinit var tvBreakdownBills: TextView
    private lateinit var tvBreakdownOther: TextView

    // Search and Filter Views
    private lateinit var etSearch: EditText
    private lateinit var btnClearSearch: ImageButton
    private lateinit var chipGroupCategory: ChipGroup
    private lateinit var chipAll: Chip

    // List and Empty State Views
    private lateinit var rvExpenses: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyTitle: TextView
    private lateinit var tvEmptySubtitle: TextView
    private lateinit var btnEmptyAction: Button
    private lateinit var tvListHeaderCount: TextView
    private lateinit var fabAddExpense: ExtendedFloatingActionButton

    // Filter state
    private var currentCategoryFilter: String = "All"
    private var currentSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        repository = ExpenseRepository(this)

        initViews()
        setupRecyclerView()
        setupSearchAndFilters()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        // Refresh dashboard calculations and list whenever returning to MainActivity
        refreshDashboard()
    }

    private fun initViews() {
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvHighestExpenseAmount = findViewById(R.id.tvHighestExpenseAmount)
        tvHighestExpenseTitle = findViewById(R.id.tvHighestExpenseTitle)
        tvTransactionCount = findViewById(R.id.tvTransactionCount)

        tvBreakdownFood = findViewById(R.id.tvBreakdownFood)
        tvBreakdownTravel = findViewById(R.id.tvBreakdownTravel)
        tvBreakdownShopping = findViewById(R.id.tvBreakdownShopping)
        tvBreakdownBills = findViewById(R.id.tvBreakdownBills)
        tvBreakdownOther = findViewById(R.id.tvBreakdownOther)

        etSearch = findViewById(R.id.etSearch)
        btnClearSearch = findViewById(R.id.btnClearSearch)
        chipGroupCategory = findViewById(R.id.chipGroupCategory)
        chipAll = findViewById(R.id.chipAll)

        rvExpenses = findViewById(R.id.rvExpenses)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        tvEmptyTitle = findViewById(R.id.tvEmptyTitle)
        tvEmptySubtitle = findViewById(R.id.tvEmptySubtitle)
        btnEmptyAction = findViewById(R.id.btnEmptyAction)
        tvListHeaderCount = findViewById(R.id.tvListHeaderCount)
        fabAddExpense = findViewById(R.id.fabAddExpense)
    }

    private fun setupRecyclerView() {
        adapter = ExpenseAdapter(
            onDeleteClicked = { expense ->
                showDeleteConfirmationDialog(expense)
            }
        )
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter
    }

    private fun setupSearchAndFilters() {
        // Search text change listener
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchQuery = s?.toString()?.trim() ?: ""
                btnClearSearch.visibility = if (currentSearchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                loadFilteredExpenses()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            etSearch.text?.clear()
        }

        // Category filter chips listener
        chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) {
                chipAll.isChecked = true
                currentCategoryFilter = "All"
            } else {
                val selectedChip = findViewById<Chip>(checkedIds.first())
                val categoryName = selectedChip.text.toString()
                currentCategoryFilter = if (categoryName.equals("All", ignoreCase = true)) "All" else categoryName
            }
            loadFilteredExpenses()
        }

        btnEmptyAction.setOnClickListener {
            resetFilters()
        }
    }

    private fun setupListeners() {
        fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Refreshes summary cards, category breakdown metrics, and the expense list.
     */
    private fun refreshDashboard() {
        updateSummaryCards()
        updateCategoryBreakdown()
        loadFilteredExpenses()
    }

    /**
     * Fetches real-time totals and highest expense from SQLite.
     */
    private fun updateSummaryCards() {
        val total = repository.getTotalExpenses()
        tvTotalExpenses.text = ExpenseUtils.formatCurrency(total)

        val highest = repository.getHighestExpense()
        if (highest != null) {
            tvHighestExpenseAmount.text = ExpenseUtils.formatCurrency(highest.amount)
            tvHighestExpenseTitle.text = "${highest.title} (${highest.category})"
        } else {
            tvHighestExpenseAmount.text = ExpenseUtils.formatCurrency(0.0)
            tvHighestExpenseTitle.text = getString(R.string.no_expenses_recorded)
        }

        val count = repository.getExpenseCount()
        tvTransactionCount.text = resources.getQuantityString(
            R.plurals.transaction_count_plural,
            count,
            count
        )
    }

    /**
     * Updates category breakdown numbers from SQLite GROUP BY query.
     */
    private fun updateCategoryBreakdown() {
        val categoryTotals = repository.getCategoryTotals()

        tvBreakdownFood.text = ExpenseUtils.formatCurrencyCompact(categoryTotals[Expense.CATEGORY_FOOD] ?: 0.0)
        tvBreakdownTravel.text = ExpenseUtils.formatCurrencyCompact(categoryTotals[Expense.CATEGORY_TRAVEL] ?: 0.0)
        tvBreakdownShopping.text = ExpenseUtils.formatCurrencyCompact(categoryTotals[Expense.CATEGORY_SHOPPING] ?: 0.0)
        tvBreakdownBills.text = ExpenseUtils.formatCurrencyCompact(categoryTotals[Expense.CATEGORY_BILLS] ?: 0.0)
        tvBreakdownOther.text = ExpenseUtils.formatCurrencyCompact(categoryTotals[Expense.CATEGORY_OTHER] ?: 0.0)
    }

    /**
     * Loads expenses matching both search query and category filter.
     */
    private fun loadFilteredExpenses() {
        val expenses = repository.searchAndFilterExpenses(
            query = currentSearchQuery,
            category = currentCategoryFilter
        )
        adapter.submitList(expenses)

        val totalRecordsInDb = repository.getExpenseCount()
        val isFiltered = currentSearchQuery.isNotEmpty() || !currentCategoryFilter.equals("All", ignoreCase = true)

        tvListHeaderCount.text = "${expenses.size} ${if (expenses.size == 1) "item" else "items"}"

        if (expenses.isEmpty()) {
            rvExpenses.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE

            if (totalRecordsInDb == 0) {
                // Entire database is empty
                tvEmptyTitle.text = getString(R.string.empty_expenses_title)
                tvEmptySubtitle.text = getString(R.string.empty_expenses_subtitle)
                btnEmptyAction.visibility = View.GONE
            } else if (isFiltered) {
                // No records matching search / filter
                tvEmptyTitle.text = "No Matching Expenses"
                tvEmptySubtitle.text = getString(R.string.empty_filter_subtitle)
                btnEmptyAction.visibility = View.VISIBLE
            }
        } else {
            rvExpenses.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
        }
    }

    /**
     * Clears search input and resets category filter back to "All".
     */
    private fun resetFilters() {
        etSearch.text?.clear()
        chipGroupCategory.check(R.id.chipAll)
        currentCategoryFilter = "All"
        loadFilteredExpenses()
    }

    /**
     * Displays a confirmation dialog before deleting an expense.
     */
    private fun showDeleteConfirmationDialog(expense: Expense) {
        val message = getString(R.string.delete_dialog_message, expense.title)

        AlertDialog.Builder(this)
            .setTitle(R.string.delete_dialog_title)
            .setMessage(message)
            .setPositiveButton(R.string.btn_delete) { dialog, _ ->
                val success = repository.deleteExpense(expense.id)
                if (success) {
                    Toast.makeText(this, getString(R.string.expense_deleted_success), Toast.LENGTH_SHORT).show()
                    refreshDashboard()
                } else {
                    Toast.makeText(this, getString(R.string.expense_deleted_failed), Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}