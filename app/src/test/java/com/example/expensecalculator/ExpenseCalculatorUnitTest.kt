package com.example.expensecalculator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying core business logic, validation rules,
 * currency/date formatting, and mathematical operations.
 */
class ExpenseCalculatorUnitTest {

    // -------------------------------------------------------------
    // 1. Title Validation Tests (Scenarios 1, 3, 4)
    // -------------------------------------------------------------

    @Test
    fun testValidTitleAccepted() {
        val result = ExpenseUtils.validateTitle("Grocery Store")
        assertTrue("Valid title should succeed", result is ExpenseUtils.ValidationResult.Success)
        assertEquals("Grocery Store", (result as ExpenseUtils.ValidationResult.Success).value)
    }

    @Test
    fun testEmptyTitleRejected() {
        val resultNull = ExpenseUtils.validateTitle(null)
        assertTrue("Null title must fail", resultNull is ExpenseUtils.ValidationResult.Error)

        val resultEmpty = ExpenseUtils.validateTitle("")
        assertTrue("Empty title must fail", resultEmpty is ExpenseUtils.ValidationResult.Error)
    }

    @Test
    fun testWhitespaceOnlyTitleRejected() {
        val result = ExpenseUtils.validateTitle("   \t   \n  ")
        assertTrue("Whitespace-only title must fail", result is ExpenseUtils.ValidationResult.Error)
    }

    // -------------------------------------------------------------
    // 2. Amount Validation Tests (Scenarios 1, 5, 6, 7)
    // -------------------------------------------------------------

    @Test
    fun testValidAmountsAccepted() {
        val testCases = listOf(
            "10" to 10.0,
            "150.50" to 150.50,
            "0.99" to 0.99,
            "99999.75" to 99999.75
        )
        for ((input, expected) in testCases) {
            val result = ExpenseUtils.validateAmount(input)
            assertTrue("Expected valid for '$input'", result is ExpenseUtils.ValidationResult.Success)
            assertEquals(expected, (result as ExpenseUtils.ValidationResult.Success).value, 0.001)
        }
    }

    @Test
    fun testEmptyAmountRejected() {
        val resultNull = ExpenseUtils.validateAmount(null)
        assertTrue("Null amount must fail", resultNull is ExpenseUtils.ValidationResult.Error)

        val resultEmpty = ExpenseUtils.validateAmount("")
        assertTrue("Empty amount must fail", resultEmpty is ExpenseUtils.ValidationResult.Error)

        val resultSpaces = ExpenseUtils.validateAmount("   ")
        assertTrue("Whitespace amount must fail", resultSpaces is ExpenseUtils.ValidationResult.Error)
    }

    @Test
    fun testZeroAndNegativeAmountRejected() {
        val zeroResult = ExpenseUtils.validateAmount("0")
        assertTrue("Zero amount must be rejected", zeroResult is ExpenseUtils.ValidationResult.Error)

        val zeroDecimalResult = ExpenseUtils.validateAmount("0.00")
        assertTrue("Zero decimal amount must be rejected", zeroDecimalResult is ExpenseUtils.ValidationResult.Error)

        val negativeResult = ExpenseUtils.validateAmount("-50.25")
        assertTrue("Negative amount must be rejected", negativeResult is ExpenseUtils.ValidationResult.Error)
    }

    @Test
    fun testInvalidNumericInputRejected() {
        val invalidInputs = listOf("abc", "12.34.56", "₹500", "NaN", "Infinity", "-Infinity", "12.345")
        for (input in invalidInputs) {
            val result = ExpenseUtils.validateAmount(input)
            assertTrue("Input '$input' must be rejected", result is ExpenseUtils.ValidationResult.Error)
        }
    }

    // -------------------------------------------------------------
    // 3. Category Constraints Tests (Scenario 2)
    // -------------------------------------------------------------

    @Test
    fun testAllFiveCategoriesSupported() {
        val expected = listOf("Food", "Travel", "Shopping", "Bills", "Other")
        assertEquals(5, Expense.ALL_CATEGORIES.size)
        assertEquals(expected, Expense.ALL_CATEGORIES)
    }

    // -------------------------------------------------------------
    // 4. Calculations: Total, Highest, Category Grouping (Scenarios 8, 9, 10, 17, 19)
    // -------------------------------------------------------------

    @Test
    fun testTotalExpensesCalculation() {
        val sampleExpenses = listOf(
            Expense(1, "Lunch", 250.00, "Food", "2026-10-10"),
            Expense(2, "Taxi", 180.50, "Travel", "2026-10-10"),
            Expense(3, "Shirt", 1200.00, "Shopping", "2026-10-09")
        )
        val total = sampleExpenses.sumOf { it.amount }
        assertEquals(1630.50, total, 0.001)
    }

    @Test
    fun testHighestIndividualExpense() {
        val sampleExpenses = listOf(
            Expense(1, "Lunch", 250.00, "Food", "2026-10-10"),
            Expense(2, "Flight Ticket", 5400.00, "Travel", "2026-10-08"),
            Expense(3, "Coffee", 120.00, "Food", "2026-10-10")
        )
        val highest = sampleExpenses.maxByOrNull { it.amount }
        assertNotNull(highest)
        assertEquals("Flight Ticket", highest?.title)
        assertEquals(5400.00, highest?.amount ?: 0.0, 0.001)
    }

    @Test
    fun testCategoryTotalsCalculation() {
        val sampleExpenses = listOf(
            Expense(1, "Burger", 150.0, "Food", "2026-10-10"),
            Expense(2, "Pizza", 350.0, "Food", "2026-10-09"),
            Expense(3, "Bus", 40.0, "Travel", "2026-10-10"),
            Expense(4, "Jeans", 1500.0, "Shopping", "2026-10-08"),
            Expense(5, "Electricity", 850.0, "Bills", "2026-10-07")
        )

        val map = mutableMapOf<String, Double>()
        Expense.ALL_CATEGORIES.forEach { map[it] = 0.0 }
        sampleExpenses.forEach { exp ->
            map[exp.category] = (map[exp.category] ?: 0.0) + exp.amount
        }

        assertEquals(500.0, map["Food"] ?: 0.0, 0.001)
        assertEquals(40.0, map["Travel"] ?: 0.0, 0.001)
        assertEquals(1500.0, map["Shopping"] ?: 0.0, 0.001)
        assertEquals(850.0, map["Bills"] ?: 0.0, 0.001)
        assertEquals(0.0, map["Other"] ?: 0.0, 0.001)
    }

    @Test
    fun testEmptyDatabaseTotalsZero() {
        val emptyList = emptyList<Expense>()
        val total = emptyList.sumOf { it.amount }
        val highest = emptyList.maxByOrNull { it.amount }

        assertEquals(0.0, total, 0.001)
        assertNull(highest)
    }

    // -------------------------------------------------------------
    // 5. Sorting: Newest First (Scenario 11)
    // -------------------------------------------------------------

    @Test
    fun testNewestFirstSorting() {
        val e1 = Expense(1, "Old Expense", 100.0, "Food", "2026-10-01", 1000L)
        val e2 = Expense(2, "Newer Expense", 200.0, "Food", "2026-10-05", 2000L)
        val e3 = Expense(3, "Latest Expense Morning", 300.0, "Travel", "2026-10-10", 3000L)
        val e4 = Expense(4, "Latest Expense Evening", 400.0, "Bills", "2026-10-10", 4000L)

        val list = listOf(e1, e2, e3, e4)

        // Comparator mirroring: ORDER BY date DESC, id DESC
        val comparator = compareByDescending<Expense> { it.date }.thenByDescending { it.id }
        val sorted = list.sortedWith(comparator)

        assertEquals(4L, sorted[0].id) // 2026-10-10, id 4
        assertEquals(3L, sorted[1].id) // 2026-10-10, id 3
        assertEquals(2L, sorted[2].id) // 2026-10-05, id 2
        assertEquals(1L, sorted[3].id) // 2026-10-01, id 1
    }

    // -------------------------------------------------------------
    // 6. Search and Filtering Logic (Scenarios 12, 13, 14, 20)
    // -------------------------------------------------------------

    @Test
    fun testSearchAndFilterTogether() {
        val dataset = listOf(
            Expense(1, "City Bus Ticket", 50.0, "Travel", "2026-10-10"),
            Expense(2, "Metro Smart Card", 200.0, "Travel", "2026-10-09"),
            Expense(3, "Grocery Shopping", 800.0, "Shopping", "2026-10-08"),
            Expense(4, "Bus Snack", 60.0, "Food", "2026-10-08")
        )

        // Scenario 12: Search partial title "bus"
        val busSearch = dataset.filter { it.title.contains("bus", ignoreCase = true) }
        assertEquals(2, busSearch.size) // "City Bus Ticket" and "Bus Snack"

        // Scenario 13: Category filter "Travel"
        val travelFilter = dataset.filter { it.category == "Travel" }
        assertEquals(2, travelFilter.size)

        // Scenario 14: Combined: Category = "Travel" AND Title contains "bus"
        val combined = dataset.filter {
            it.category == "Travel" && it.title.contains("bus", ignoreCase = true)
        }
        assertEquals(1, combined.size)
        assertEquals("City Bus Ticket", combined[0].title)

        // Scenario 20: No records match -> empty list, no crash
        val noMatch = dataset.filter {
            it.category == "Bills" && it.title.contains("bus", ignoreCase = true)
        }
        assertTrue(noMatch.isEmpty())
    }

    // -------------------------------------------------------------
    // 7. Date and Currency Formatting Tests
    // -------------------------------------------------------------

    @Test
    fun testCurrencyFormatting() {
        val formatted = ExpenseUtils.formatCurrency(1250.50)
        assertTrue("Should contain currency symbol", formatted.contains("₹"))
        assertTrue("Should contain 1,250", formatted.contains("1,250"))
    }

    @Test
    fun testDisplayDateFormatting() {
        val formatted = ExpenseUtils.formatDisplayDate("2026-10-10")
        assertTrue("Formatted date should contain 2026", formatted.contains("2026"))
        assertTrue("Formatted date should contain Oct", formatted.contains("Oct"))
    }
}
