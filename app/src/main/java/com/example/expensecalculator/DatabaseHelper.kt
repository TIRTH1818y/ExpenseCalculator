package com.example.expensecalculator

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * SQLiteOpenHelper implementation for managing local persistent storage of expenses.
 *
 * Provides thread-safe, sanitized, parameterized database operations.
 */
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "DatabaseHelper"

        const val DATABASE_NAME = "expenses.db"
        const val DATABASE_VERSION = 1

        const val TABLE_EXPENSES = "expenses"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_DATE = "date"
        const val COLUMN_CREATED_AT = "created_at"

        private const val CREATE_TABLE_QUERY = """
            CREATE TABLE $TABLE_EXPENSES (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL CHECK($COLUMN_AMOUNT > 0),
                $COLUMN_CATEGORY TEXT NOT NULL CHECK(
                    $COLUMN_CATEGORY IN ('Food', 'Travel', 'Shopping', 'Bills', 'Other')
                ),
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_CREATED_AT INTEGER NOT NULL
            );
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_QUERY)
        Log.d(TAG, "Database created with table: $TABLE_EXPENSES")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // In a production app, schema migrations are performed incrementally.
        // As per requirements: "Do not erase existing records during normal database upgrades."
        Log.i(TAG, "Upgrading database from version $oldVersion to $newVersion without erasing data.")
        // Future column migrations would be applied via ALTER TABLE here.
    }

    /**
     * Inserts a new expense into SQLite using ContentValues.
     *
     * @return The row ID of the newly inserted expense, or -1 on failure.
     */
    fun insertExpense(expense: Expense): Long {
        val db = writableDatabase
        return try {
            val values = ContentValues().apply {
                put(COLUMN_TITLE, expense.title)
                put(COLUMN_AMOUNT, expense.amount)
                put(COLUMN_CATEGORY, expense.category)
                put(COLUMN_DATE, expense.date)
                put(COLUMN_CREATED_AT, expense.createdAt)
            }
            db.insert(TABLE_EXPENSES, null, values)
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting expense: ${expense.title}", e)
            -1L
        }
    }

    /**
     * Retrieves all expenses, ordered newest date first (tie-breaking with ID DESC).
     */
    fun getAllExpenses(): List<Expense> {
        val query = "SELECT * FROM $TABLE_EXPENSES ORDER BY $COLUMN_DATE DESC, $COLUMN_ID DESC"
        return executeExpenseQuery(query, null)
    }

    /**
     * Deletes an expense by its unique ID.
     *
     * @return True if a record was deleted, false otherwise.
     */
    fun deleteExpense(id: Long): Boolean {
        val db = writableDatabase
        return try {
            val rowsDeleted = db.delete(
                TABLE_EXPENSES,
                "$COLUMN_ID = ?",
                arrayOf(id.toString())
            )
            rowsDeleted > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting expense with id: $id", e)
            false
        }
    }

    /**
     * Calculates the sum of all expenses using SQLite aggregate SUM.
     * Uses COALESCE to ensure 0.0 is returned if the table is empty.
     */
    fun getTotalExpenses(): Double {
        val query = "SELECT COALESCE(SUM($COLUMN_AMOUNT), 0) FROM $TABLE_EXPENSES"
        val db = readableDatabase
        return try {
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getDouble(0)
                } else {
                    0.0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating total expenses", e)
            0.0
        }
    }

    /**
     * Retrieves the single highest individual expense recorded.
     * Returns null if no records exist.
     */
    fun getHighestExpense(): Expense? {
        val query = "SELECT * FROM $TABLE_EXPENSES ORDER BY $COLUMN_AMOUNT DESC LIMIT 1"
        val list = executeExpenseQuery(query, null)
        return list.firstOrNull()
    }

    /**
     * Counts the total number of expense records.
     */
    fun getExpenseCount(): Int {
        val query = "SELECT COUNT(*) FROM $TABLE_EXPENSES"
        val db = readableDatabase
        return try {
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    cursor.getInt(0)
                } else {
                    0
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting expense count", e)
            0
        }
    }

    /**
     * Retrieves spending totals grouped by category.
     * Populates 0.0 for any categories that do not yet have expenses.
     */
    fun getCategoryTotals(): Map<String, Double> {
        val resultMap = mutableMapOf<String, Double>()
        // Initialize all supported categories to 0.0
        Expense.ALL_CATEGORIES.forEach { category ->
            resultMap[category] = 0.0
        }

        val query = "SELECT $COLUMN_CATEGORY, SUM($COLUMN_AMOUNT) FROM $TABLE_EXPENSES GROUP BY $COLUMN_CATEGORY"
        val db = readableDatabase
        try {
            db.rawQuery(query, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val category = cursor.getString(0)
                    val total = cursor.getDouble(1)
                    if (category != null) {
                        resultMap[category] = total
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching category totals", e)
        }

        return resultMap
    }

    /**
     * Searches expenses by title using a parameterized LIKE query.
     */
    fun searchExpensesByTitle(query: String): List<Expense> {
        val sql = "SELECT * FROM $TABLE_EXPENSES WHERE $COLUMN_TITLE LIKE ? ORDER BY $COLUMN_DATE DESC, $COLUMN_ID DESC"
        val args = arrayOf("%$query%")
        return executeExpenseQuery(sql, args)
    }

    /**
     * Filters expenses by category using a parameterized query.
     */
    fun getExpensesByCategory(category: String): List<Expense> {
        val sql = "SELECT * FROM $TABLE_EXPENSES WHERE $COLUMN_CATEGORY = ? ORDER BY $COLUMN_DATE DESC, $COLUMN_ID DESC"
        val args = arrayOf(category)
        return executeExpenseQuery(sql, args)
    }

    /**
     * Combined search and category filtering using parameterized queries.
     *
     * @param query Optional search text.
     * @param category Optional category name (or "All" / null for all categories).
     */
    fun searchAndFilterExpenses(query: String?, category: String?): List<Expense> {
        val hasCategory = !category.isNullOrBlank() && !category.equals("All", ignoreCase = true)
        val hasQuery = !query.isNullOrBlank()

        return when {
            hasCategory && hasQuery -> {
                val sql = """
                    SELECT * FROM $TABLE_EXPENSES 
                    WHERE $COLUMN_CATEGORY = ? AND $COLUMN_TITLE LIKE ? 
                    ORDER BY $COLUMN_DATE DESC, $COLUMN_ID DESC
                """.trimIndent()
                executeExpenseQuery(sql, arrayOf(category!!, "%${query!!.trim()}%"))
            }
            hasCategory -> {
                getExpensesByCategory(category!!)
            }
            hasQuery -> {
                searchExpensesByTitle(query!!.trim())
            }
            else -> {
                getAllExpenses()
            }
        }
    }

    /**
     * Helper method to execute a query, map Cursor rows to Expense objects,
     * and guarantee Cursor closure using `.use { ... }`.
     */
    private fun executeExpenseQuery(sql: String, selectionArgs: Array<String>?): List<Expense> {
        val expenses = mutableListOf<Expense>()
        val db = readableDatabase
        try {
            db.rawQuery(sql, selectionArgs).use { cursor ->
                val idIdx = cursor.getColumnIndexOrThrow(COLUMN_ID)
                val titleIdx = cursor.getColumnIndexOrThrow(COLUMN_TITLE)
                val amountIdx = cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)
                val categoryIdx = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)
                val dateIdx = cursor.getColumnIndexOrThrow(COLUMN_DATE)
                val createdAtIdx = cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)

                while (cursor.moveToNext()) {
                    expenses.add(
                        Expense(
                            id = cursor.getLong(idIdx),
                            title = cursor.getString(titleIdx),
                            amount = cursor.getDouble(amountIdx),
                            category = cursor.getString(categoryIdx),
                            date = cursor.getString(dateIdx),
                            createdAt = cursor.getLong(createdAtIdx)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error executing expense query: $sql", e)
        }
        return expenses
    }
}
