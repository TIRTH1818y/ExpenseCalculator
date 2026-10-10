package com.example.expensecalculator

import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility functions for currency formatting, date transformations, and input validations.
 */
object ExpenseUtils {

    private const val STORAGE_DATE_PATTERN = "yyyy-MM-dd"
    private const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"

    private val storageDateFormat = SimpleDateFormat(STORAGE_DATE_PATTERN, Locale.US).apply {
        isLenient = false
    }

    private val displayDateFormat = SimpleDateFormat(DISPLAY_DATE_PATTERN, Locale.getDefault())

    /**
     * Formats a monetary amount into standard Indian Rupees representation (e.g., "₹ 1,250.00").
     */
    fun formatCurrency(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("en-IN")).apply {
            currencySymbol = "₹"
        }
        val formatter = DecimalFormat("₹ #,##,##0.00", symbols)
        return formatter.format(amount)
    }

    /**
     * Formats a monetary amount into compact display without unnecessary trailing zeros (e.g., "₹ 1,250").
     */
    fun formatCurrencyCompact(amount: Double): String {
        val symbols = DecimalFormatSymbols(Locale.forLanguageTag("en-IN")).apply {
            currencySymbol = "₹"
        }
        val pattern = if (amount % 1.0 == 0.0) "₹ #,##,##0" else "₹ #,##,##0.00"
        val formatter = DecimalFormat(pattern, symbols)
        return formatter.format(amount)
    }

    /**
     * Converts a storage date string ("yyyy-MM-dd") into a human-readable display string ("10 Oct 2026").
     */
    fun formatDisplayDate(storageDateString: String): String {
        return try {
            val date = storageDateFormat.parse(storageDateString)
            if (date != null) {
                displayDateFormat.format(date)
            } else {
                storageDateString
            }
        } catch (e: ParseException) {
            storageDateString
        }
    }

    /**
     * Returns today's date formatted as "yyyy-MM-dd" for SQLite storage.
     */
    fun getTodayStorageDate(): String {
        return storageDateFormat.format(Date())
    }

    /**
     * Formats year, month (0-indexed Calendar month), and day into "yyyy-MM-dd".
     */
    fun formatStorageDate(year: Int, monthOfYear: Int, dayOfMonth: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthOfYear)
            set(Calendar.DAY_OF_MONTH, dayOfMonth)
        }
        return storageDateFormat.format(calendar.time)
    }

    /**
     * Validates an expense title.
     * Must not be empty or blank after trimming.
     */
    fun validateTitle(title: String?): ValidationResult<String> {
        if (title.isNullOrBlank()) {
            return ValidationResult.Error("Expense title cannot be empty.")
        }
        val trimmed = title.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult.Error("Expense title cannot be only whitespace.")
        }
        return ValidationResult.Success(trimmed)
    }

    /**
     * Validates a currency amount string using BigDecimal to prevent precision loss,
     * overflow, NaN, infinite, or non-positive values.
     */
    fun validateAmount(amountString: String?): ValidationResult<Double> {
        if (amountString.isNullOrBlank()) {
            return ValidationResult.Error("Amount is required.")
        }

        val trimmed = amountString.trim()
        return try {
            val bigDecimal = BigDecimal(trimmed)

            if (bigDecimal.scale() > 2) {
                return ValidationResult.Error("Amount can have at most 2 decimal places.")
            }

            if (bigDecimal <= BigDecimal.ZERO) {
                return ValidationResult.Error("Amount must be greater than zero.")
            }

            val doubleVal = bigDecimal.toDouble()
            if (doubleVal.isNaN() || doubleVal.isInfinite()) {
                return ValidationResult.Error("Invalid numerical value.")
            }

            ValidationResult.Success(doubleVal)
        } catch (e: NumberFormatException) {
            ValidationResult.Error("Please enter a valid numeric amount.")
        }
    }

    /**
     * Result sealed class representing the outcome of an input validation.
     */
    sealed class ValidationResult<out T> {
        data class Success<T>(val value: T) : ValidationResult<T>()
        data class Error(val message: String) : ValidationResult<Nothing>()

        val isSuccess: Boolean get() = this is Success
    }
}
