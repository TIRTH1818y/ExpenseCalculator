package com.example.expensecalculator

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Calendar

/**
 * Screen for entering and saving a new expense transaction.
 *
 * Handles validation of title, amount (using BigDecimal), category selection,
 * and date selection via DatePickerDialog.
 */
class AddExpenseActivity : AppCompatActivity() {

    private lateinit var repository: ExpenseRepository

    private lateinit var tilTitle: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var tilAmount: TextInputLayout
    private lateinit var etAmount: TextInputEditText
    private lateinit var spinnerCategory: Spinner
    private lateinit var layoutDateSelector: LinearLayout
    private lateinit var tvSelectedDate: TextView
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnBack: ImageButton

    // Internal state for storage date ("yyyy-MM-dd")
    private var selectedStorageDate: String = ExpenseUtils.getTodayStorageDate()
    private val calendar = Calendar.getInstance()
    private var isSavingInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        repository = ExpenseRepository(this)

        initViews()
        setupCategorySpinner()
        setupDateSelector()
        setupActionListeners()
    }

    private fun initViews() {
        tilTitle = findViewById(R.id.tilTitle)
        etTitle = findViewById(R.id.etTitle)
        tilAmount = findViewById(R.id.tilAmount)
        etAmount = findViewById(R.id.etAmount)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        layoutDateSelector = findViewById(R.id.layoutDateSelector)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        btnSave = findViewById(R.id.btnSaveExpense)
        btnCancel = findViewById(R.id.btnCancelExpense)
        btnBack = findViewById(R.id.btnBack)

        // Set initial date text to today's friendly display date
        tvSelectedDate.text = ExpenseUtils.formatDisplayDate(selectedStorageDate)
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.item_spinner_category,
            Expense.ALL_CATEGORIES
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter
    }

    private fun setupDateSelector() {
        layoutDateSelector.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, selectedMonth)
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                selectedStorageDate = ExpenseUtils.formatStorageDate(selectedYear, selectedMonth, selectedDay)
                tvSelectedDate.text = ExpenseUtils.formatDisplayDate(selectedStorageDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun setupActionListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            saveExpense()
        }
    }

    private fun saveExpense() {
        if (isSavingInProgress) return

        // 1. Clear previous errors
        tilTitle.error = null
        tilAmount.error = null

        // 2. Validate Title
        val rawTitle = etTitle.text?.toString()
        val titleValidation = ExpenseUtils.validateTitle(rawTitle)
        if (titleValidation is ExpenseUtils.ValidationResult.Error) {
            tilTitle.error = titleValidation.message
            etTitle.requestFocus()
            return
        }
        val validatedTitle = (titleValidation as ExpenseUtils.ValidationResult.Success).value

        // 3. Validate Amount
        val rawAmount = etAmount.text?.toString()
        val amountValidation = ExpenseUtils.validateAmount(rawAmount)
        if (amountValidation is ExpenseUtils.ValidationResult.Error) {
            tilAmount.error = amountValidation.message
            etAmount.requestFocus()
            return
        }
        val validatedAmount = (amountValidation as ExpenseUtils.ValidationResult.Success).value

        // 4. Validate Category
        val selectedCategory = spinnerCategory.selectedItem?.toString()
        if (selectedCategory.isNullOrBlank() || !Expense.ALL_CATEGORIES.contains(selectedCategory)) {
            Toast.makeText(this, getString(R.string.error_category_invalid), Toast.LENGTH_SHORT).show()
            return
        }

        // Prevent duplicate submissions from rapid multi-clicks
        isSavingInProgress = true
        btnSave.isEnabled = false

        val newExpense = Expense(
            title = validatedTitle,
            amount = validatedAmount,
            category = selectedCategory,
            date = selectedStorageDate,
            createdAt = System.currentTimeMillis()
        )

        val insertedId = repository.addExpense(newExpense)
        if (insertedId > 0) {
            Toast.makeText(this, getString(R.string.expense_saved_success), Toast.LENGTH_SHORT).show()
            finish()
        } else {
            isSavingInProgress = false
            btnSave.isEnabled = true
            Toast.makeText(this, getString(R.string.expense_save_failed), Toast.LENGTH_LONG).show()
        }
    }
}
