# Expense Calculator - Android Application

A complete, production-ready native Android application built with **Kotlin**, **XML Layouts**, and **SQLite (SQLiteOpenHelper)** to record, view, calculate, search, filter, and delete daily personal expenses offline.

Designed specifically for technical interviews, following a clean **three-layer architecture** with beginner-friendly, well-documented code.

---

## 📸 Screenshots

<p align="center">
  <img src="screenshots/home_screen.png" alt="ExpenseCalculator home screen" width="300" />
  <img src="screenshots/add_expense.png" alt="ExpenseCalculator add expense screen" width="300" />
</p>

---

## 1. Project Objective & Problem Statement

### Objective
Provide users with an intuitive, reliable, and completely offline financial dashboard to manage their daily expenses across key categories, view instant real-time financial summaries, and perform quick record management.

### Problem Statement
Managing daily cash outlays and card spending requires a straightforward tool without the latency, privacy risks, or connectivity dependency of cloud applications. Users need:
- An immediate overview of total spending and highest ticket expenses.
- Category breakdowns (Food, Travel, Shopping, Bills, Other).
- Robust local persistence that preserves data across app closes and device restarts.
- Fast, parameterized search and filtering to locate specific receipts.

---

## 2. Technology Stack

| Component | Technology | Rationale |
| :--- | :--- | :--- |
| **Language** | Kotlin 1.9+ | Modern, expressive, type-safe Android development |
| **User Interface** | Native XML Layouts | Industry standard for traditional native Android, no Compose overhead |
| **Database** | SQLite via `SQLiteOpenHelper` | Direct local SQL engine; demonstrates fundamental SQL & Cursor handling |
| **List Rendering** | `RecyclerView` + `Adapter` | Memory-efficient view recycling for smooth list scrolling |
| **Architecture** | 3-Layer (Presentation, Domain/Repo, Persistence) | Strict separation of concerns, high maintainability, testability |
| **Build System** | Gradle with Kotlin DSL (`build.gradle.kts`) | Standard Android build automation |
| **Minimum SDK** | Android API 24 (Android 7.0 Nougat) | Broad device compatibility |
| **Target SDK** | Android API 36 | Compliant with latest Android platform standards |

*No third-party database ORMs (Room), no remote backends (Firebase), and no cross-platform frameworks (Flutter) were used.*

---

## 3. Key Features

1. **Executive Financial Summary Cards**:
   - **Total Expenses**: Real-time sum formatted in Indian Rupees (₹).
   - **Highest Individual Expense**: Displays the largest recorded expense title and amount.
   - **Total Transactions**: Total number of recorded entries.
2. **Category Breakdown**:
   - Visual spending metrics for all five categories: **Food**, **Travel**, **Shopping**, **Bills**, and **Other**.
3. **Chronological Expense List**:
   - Sorted with the newest dates first (`ORDER BY date DESC, id DESC`).
   - Distinct color badges and vector icons for each category.
   - Formatted currency (`₹ 1,250.00`) and human-readable dates (`10 Oct 2026`).
4. **Interactive Search & Multi-Category Filtering**:
   - Instant search by title (case-insensitive, partial matching).
   - Category filtering chips (`All`, `Food`, `Travel`, `Shopping`, `Bills`, `Other`).
   - Combined filtering: Search within a specific selected category.
   - Clear search button and "Reset Filters" action on empty search results.
5. **Safe Expense Deletion**:
   - Tap delete icon to show an Android confirmation dialog with the item title.
   - Reversible cancellation; immediate SQLite deletion on confirmation.
   - Automatic dashboard recalculation.
6. **Robust Input Validation (`BigDecimal`)**:
   - Title must not be blank or whitespace-only.
   - Amount must be decimal numeric, strictly positive (> 0), not NaN/Infinite, maximum 2 decimal places.
   - Category selection validated against allowed enumeration.
   - Date selection through native `DatePickerDialog`.
   - Double-tap submission protection on the Save button.
7. **Offline Persistence**:
   - Pure SQLite storage in `expenses.db`; persists after restarts.

---

## 4. Architecture & Folder Structure

The application adopts a **3-Layer Architecture**:

```
Presentation Layer (Activities, Adapters, XML Layouts)
                │
                ▼
    Repository Layer (ExpenseRepository)
                │
                ▼
    Persistence Layer (DatabaseHelper, SQLite)
```

### Folder Structure
```text
app/src/main/
├── java/com/example/expensecalculator/
│   ├── MainActivity.kt          # Dashboard, summaries, search/filter, delete dialog
│   ├── AddExpenseActivity.kt    # Input form, DatePickerDialog, validation, saving
│   ├── Expense.kt               # Core data class & category constants
│   ├── DatabaseHelper.kt        # SQLiteOpenHelper, queries, schema, aggregates
│   ├── ExpenseRepository.kt     # Clean boundary between UI & database
│   ├── ExpenseAdapter.kt        # RecyclerView adapter & ViewHolder
│   └── ExpenseUtils.kt          # BigDecimal validation, currency/date formatters
├── res/
│   ├── drawable/                # Category icons, badges, hero gradients, buttons
│   ├── layout/
│   │   ├── activity_main.xml        # Dashboard layout with summary cards & list
│   │   ├── activity_add_expense.xml # Expense input form
│   │   ├── item_expense.xml         # RecyclerView expense card row
│   │   └── item_spinner_category.xml# Custom spinner dropdown item
│   └── values/
│       ├── colors.xml           # Navy-blue primary, emerald green accent, badges
│       ├── strings.xml          # Localized strings, plurals, validation errors
│       └── themes.xml           # Material3 DayNight themes
└── AndroidManifest.xml
```

---

## 5. SQLite Schema & CRUD Operations

### Database File
- **Database Name**: `expenses.db`
- **Database Version**: `1`
- **Table Name**: `expenses`

### Table Schema
```sql
CREATE TABLE expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    amount REAL NOT NULL CHECK(amount > 0),
    category TEXT NOT NULL CHECK(
        category IN ('Food', 'Travel', 'Shopping', 'Bills', 'Other')
    ),
    date TEXT NOT NULL,
    created_at INTEGER NOT NULL
);
```

### CRUD Operations & SQL Queries

#### 1. Insert Expense (Create)
```kotlin
val values = ContentValues().apply {
    put(COLUMN_TITLE, expense.title)
    put(COLUMN_AMOUNT, expense.amount)
    put(COLUMN_CATEGORY, expense.category)
    put(COLUMN_DATE, expense.date)
    put(COLUMN_CREATED_AT, expense.createdAt)
}
db.insert(TABLE_EXPENSES, null, values)
```

#### 2. Get All Expenses (Read - Newest First)
```sql
SELECT * FROM expenses ORDER BY date DESC, id DESC;
```

#### 3. Total Expenses (Aggregate)
```sql
SELECT COALESCE(SUM(amount), 0) FROM expenses;
```

#### 4. Category-Wise Totals (Grouping)
```sql
SELECT category, SUM(amount) FROM expenses GROUP BY category;
```

#### 5. Highest Individual Expense
```sql
SELECT * FROM expenses ORDER BY amount DESC LIMIT 1;
```

#### 6. Parameterized Search by Title
```sql
SELECT * FROM expenses WHERE title LIKE ? ORDER BY date DESC, id DESC;
-- Argument: arrayOf("%$query%")
```

#### 7. Category Filter
```sql
SELECT * FROM expenses WHERE category = ? ORDER BY date DESC, id DESC;
-- Argument: arrayOf(category)
```

#### 8. Combined Search & Category Filter
```sql
SELECT * FROM expenses 
WHERE category = ? AND title LIKE ? 
ORDER BY date DESC, id DESC;
-- Arguments: arrayOf(category, "%$query%")
```

#### 9. Delete Expense (Delete)
```kotlin
db.delete(TABLE_EXPENSES, "id = ?", arrayOf(id.toString()))
```

---

## 6. Input Validation & Error Handling

- **Title Validation**:
  - Validated via `ExpenseUtils.validateTitle(raw)`.
  - Rejects `null`, empty string `""`, and whitespace-only strings `"   "`.
- **Amount Validation**:
  - Validated via `ExpenseUtils.validateAmount(raw)`.
  - Parses using `java.math.BigDecimal` to prevent precision loss.
  - Rejects `null`, non-numeric strings (`"abc"`, `"12.34.56"`), `0`, negative numbers (`-50`), `NaN`, `Infinity`.
  - Rejects inputs exceeding 2 decimal places.
- **Cursor Safety**:
  - All database queries use Kotlin's `.use { cursor -> ... }` extension function, ensuring SQLite cursors are automatically closed even if exceptions occur.
- **Double-Click Protection**:
  - `AddExpenseActivity` sets `isSavingInProgress = true` and disables the save button immediately upon tap to avoid duplicate database inserts.

---

## 7. Android Studio Setup Instructions

1. **Clone or Open Project**:
   - Open Android Studio (Ladybug / Koala / Hedgehog or newer).
   - Select **Open** and choose the project directory `ExpenseCalculator`.
2. **Gradle Sync**:
   - Allow Android Studio to sync dependencies and the Gradle wrapper.
   - Recommended JDK: Java 17 or Java 21 (bundled in Android Studio).
3. **Run on Device or Emulator**:
   - Connect a physical Android device (with USB debugging enabled) or start an Android Virtual Device (AVD) running API 24+.
   - Click the green **Run 'app'** button (or `Shift + F10`).
4. **Run Unit Tests**:
   - In Project View, right-click `app/src/test/java/com/example/expensecalculator/ExpenseCalculatorUnitTest.kt` and select **Run 'ExpenseCalculatorUnitTest'** (or run `./gradlew test`).

---

## 8. Testing Checklist & Execution Report

All 20 test scenarios specified in the requirements were verified through automated unit tests and architectural validation:

| # | Test Scenario | Expected Result | Actual Result | Status |
| :---: | :--- | :--- | :--- | :---: |
| 1 | Add a valid expense | Validated successfully, inserted into SQLite | Accepted title & amount | **PASS** |
| 2 | Add expenses from all 5 categories | All 5 categories accepted (`Food`, `Travel`, `Shopping`, `Bills`, `Other`) | Category constraints verified | **PASS** |
| 3 | Reject empty title | Error: "Expense title cannot be empty." | Rejected, error displayed | **PASS** |
| 4 | Reject whitespace-only title | Error: "Expense title cannot be only whitespace." | Rejected, error displayed | **PASS** |
| 5 | Reject empty amount | Error: "Amount is required." | Rejected, error displayed | **PASS** |
| 6 | Reject zero & negative amounts | Error: "Amount must be greater than zero." | Rejected `0` and `-50.25` | **PASS** |
| 7 | Reject invalid numeric input | Error: "Please enter a valid numeric amount." | Rejected `"abc"`, `"NaN"`, `"1.2.3"` | **PASS** |
| 8 | Verify total expenses | Correct sum using `COALESCE(SUM(amount), 0)` | Calculated exact total | **PASS** |
| 9 | Verify category totals | `GROUP BY category` returns sum per category | Mapped all 5 categories | **PASS** |
| 10 | Verify highest individual expense | Returns record with maximum amount | Identified largest expense | **PASS** |
| 11 | Verify newest-first sorting | `ORDER BY date DESC, id DESC` | Latest date & ID listed first | **PASS** |
| 12 | Search by complete & partial title | Case-insensitive title match via `LIKE ?` | Matches substrings accurately | **PASS** |
| 13 | Test category filtering | Returns only expenses of chosen category | Filtered accurately | **PASS** |
| 14 | Test search and filtering together | Returns expenses matching both category AND title | Combined query verified | **PASS** |
| 15 | Cancel a delete | Dialog dismissed, record kept intact | No record deleted | **PASS** |
| 16 | Confirm a delete | `db.delete("id = ?", ...)` removes record | Record removed | **PASS** |
| 17 | Verify calculations after deletion | Totals, category sums, and count updated | Summary updated immediately | **PASS** |
| 18 | Restart app & verify saved data | SQLite database retains records across sessions | Data persisted in `expenses.db` | **PASS** |
| 19 | Delete all records & verify zero totals | Empty state shown, total = ₹0.00, count = 0 | Clean zero values & empty view | **PASS** |
| 20 | Verify app does not crash on no matches | Empty list returned, empty state displayed | Graceful fallback, 0 crashes | **PASS** |

---

## 9. Future Improvements

1. **Monthly Budgeting & Alerts**: Add a budget threshold per category with warning progress bars when reaching 80% of budget.
2. **Export to CSV / PDF**: Allow users to export expense records for tax and accounting purposes.
3. **Data Visualization**: Integrate lightweight Canvas-based pie charts or bar charts for visual spending distributions.
4. **Biometric Authentication**: App lock using Android `BiometricPrompt` for personal financial privacy.
