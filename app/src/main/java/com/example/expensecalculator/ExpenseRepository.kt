package com.example.expensecalculator

import android.content.Context

/**
 * Repository layer adhering to the 3-tier architecture.
 *
 * Decouples the presentation layer (Activities/Adapters) from direct SQLite operations,
 * providing clean domain methods and facilitating testing.
 */
class ExpenseRepository(context: Context) {

    private val dbHelper = DatabaseHelper(context.applicationContext)

    /**
     * Inserts an expense record into the database.
     *
     * @return Inserted ID on success (> 0), or -1 on failure.
     */
    fun addExpense(expense: Expense): Long {
        return dbHelper.insertExpense(expense)
    }

    /**
     * Deletes an expense record by its unique ID.
     */
    fun deleteExpense(id: Long): Boolean {
        return dbHelper.deleteExpense(id)
    }

    /**
     * Retrieves all recorded expenses sorted newest-first.
     */
    fun getAllExpenses(): List<Expense> {
        return dbHelper.getAllExpenses()
    }

    /**
     * Searches and filters expenses based on an optional search query and category.
     */
    fun searchAndFilterExpenses(query: String?, category: String?): List<Expense> {
        return dbHelper.searchAndFilterExpenses(query, category)
    }

    /**
     * Returns the sum total of all recorded expenses.
     */
    fun getTotalExpenses(): Double {
        return dbHelper.getTotalExpenses()
    }

    /**
     * Returns the highest individual expense recorded, or null if none exist.
     */
    fun getHighestExpense(): Expense? {
        return dbHelper.getHighestExpense()
    }

    /**
     * Returns the count of total recorded expense transactions.
     */
    fun getExpenseCount(): Int {
        return dbHelper.getExpenseCount()
    }

    /**
     * Returns a map containing the total amount spent per category.
     */
    fun getCategoryTotals(): Map<String, Double> {
        return dbHelper.getCategoryTotals()
    }
}
