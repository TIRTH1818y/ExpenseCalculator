# Expense Calculator - Technical Interview Guide

This guide is designed to help you explain and demonstrate the **Expense Calculator** project during a technical interview. It covers elevator pitches, architectural walkthroughs, database design rationale, code explanations, 16 common interview questions with beginner-friendly answers, and a live demonstration script.

---

## 1. 1-Minute Project Introduction (Elevator Pitch)

> *"Expense Calculator is a native Android application built with Kotlin, XML layouts, and local SQLite storage using `SQLiteOpenHelper`. It helps users track, calculate, search, filter, and delete their daily personal expenses completely offline. The dashboard delivers real-time financial metrics, including total expenditure in Indian Rupees, the highest single ticket expense, total transaction count, and category-wise spending across Food, Travel, Shopping, Bills, and Other. I followed a clean three-layer architecture to ensure strict separation of concerns between the UI, the repository, and the database, while avoiding external dependencies to keep the project lightweight, reliable, and easy to maintain."*

---

## 2. 2–3 Minute In-Depth Project Explanation

> *"When designing Expense Calculator, I had three core engineering goals in mind:*
>
> *1. **Reliable Local Persistence Without External Overhead**: Rather than pulling in heavy ORM frameworks like Room or remote solutions like Firebase, I used Android's native `SQLiteOpenHelper`. This demonstrates direct command over SQL queries, schema creation, `ContentValues`, parameterized statements, and leak-free `Cursor` resource management using Kotlin's `.use` block.*
>
> *2. **Clean 3-Tier Architecture**: The application is split into three well-defined layers:*
> - *The **Presentation Layer** contains `MainActivity`, `AddExpenseActivity`, and `ExpenseAdapter`. Activities are only responsible for UI lifecycle, user input, and displaying state.*
> - *The **Repository Layer** (`ExpenseRepository`) serves as the single source of truth for the UI, keeping SQL queries and data transformations completely out of the Activities.*
> - *The **Persistence Layer** (`DatabaseHelper`) encapsulates the database lifecycle, table creation, CRUD operations, and aggregate mathematical queries (`SUM`, `COUNT`, `GROUP BY`).*
>
> *3. **Defensive Input Handling & User Experience**: Monetary values are validated using `BigDecimal` rather than raw floating-point parsing to prevent precision issues, NaN, or infinite values. Zero and negative amounts are strictly forbidden both in Kotlin validation and via SQLite `CHECK` constraints. The dashboard provides instant search by title, multi-category filtering, deletion confirmation dialogs, and clean empty states with zero crashes.*
>
> *The resulting application is responsive, looks modern with a navy-blue and emerald-green theme, and strictly obeys Android best practices."*

---

## 3. Architecture & Data Flow

```
┌────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER                   │
│                                                        │
│  MainActivity  ◄────►  ExpenseAdapter  ◄────► Layouts  │
│       ▲                                                │
│       │ (Intent)                                       │
│       ▼                                                │
│  AddExpenseActivity                                    │
└───────────────────────────┬────────────────────────────┘
                            │ Method Calls
                            ▼
┌────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                    │
│                                                        │
│                  ExpenseRepository                     │
└───────────────────────────┬────────────────────────────┘
                            │ Domain Operations
                            ▼
┌────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                    │
│                                                        │
│                    DatabaseHelper                      │
│                           │                            │
│                           ▼                            │
│               SQLite Engine (expenses.db)              │
└────────────────────────────────────────────────────────┘
```

### Data Flow for Adding an Expense:
1. User enters title, amount, selects category, and picks date in `AddExpenseActivity`.
2. `ExpenseUtils.validateTitle()` and `ExpenseUtils.validateAmount()` perform validation.
3. If valid, an `Expense` data class object is constructed.
4. `AddExpenseActivity` calls `repository.addExpense(expense)`.
5. `ExpenseRepository` calls `dbHelper.insertExpense(expense)`.
6. `DatabaseHelper` binds values to `ContentValues` and calls `db.insert()`.
7. Activity receives the inserted row ID, shows a success toast, and calls `finish()`.
8. `MainActivity.onResume()` fires, fetching updated totals (`SUM`, `GROUP BY`, `COUNT`) and refreshes the RecyclerView.

---

## 4. Database Design Explanation

### Why SQLiteOpenHelper over Room for this Interview?
1. **Demonstrates Core Android Competency**: Shows that you understand how SQLite works underneath Android, including cursors, column indices, database lifecycles, and raw SQL.
2. **Zero Dependencies**: Keeps the build fast, eliminates annotation processing overhead (KAPT / KSP), and avoids external library conflicts.

### Schema Rationale
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

- **`id INTEGER PRIMARY KEY AUTOINCREMENT`**: Unique identifier for deletion and tie-breaking.
- **`CHECK(amount > 0)`**: Database-level constraint guaranteeing that no negative or zero expenses ever enter the database.
- **`CHECK(category IN (...))`**: Restricts categories to valid domain entities.
- **`date TEXT NOT NULL`**: Dates stored in ISO `"yyyy-MM-dd"` format. This allows standard lexicographical sorting in SQL (`ORDER BY date DESC`).
- **`created_at INTEGER NOT NULL`**: Millisecond timestamp to preserve the exact insertion order when expenses have identical dates.

---

## 5. Important Function Explanations

### 1. Safe Parameterized Queries (`searchAndFilterExpenses`)
```kotlin
fun searchAndFilterExpenses(query: String?, category: String?): List<Expense>
```
- **Why it matters**: Demonstrates SQL injection prevention by using selection arguments (`?`) rather than string concatenation.
- **Logic**: Gracefully handles 4 permutation states: (1) category + search, (2) category only, (3) search only, (4) all records.

### 2. Reliable Resource Management (`.use { cursor -> ... }`)
```kotlin
db.rawQuery(query, null).use { cursor ->
    if (cursor.moveToFirst()) cursor.getDouble(0) else 0.0
}
```
- **Why it matters**: Demonstrates memory leak prevention. In Android, failing to close a `Cursor` leads to `CursorWindowAllocationException` and memory leaks. Kotlin's `.use` function guarantees `cursor.close()` is executed inside an implicit `finally` block.

### 3. Aggregate Calculations with COALESCE
```sql
SELECT COALESCE(SUM(amount), 0) FROM expenses;
```
- **Why it matters**: Standard `SUM()` on an empty table returns `NULL`. Wrapping with `COALESCE(..., 0)` guarantees a clean numeric `0.0` rather than null pointer exceptions.

### 4. Currency Validation using BigDecimal
```kotlin
val bigDecimal = BigDecimal(trimmed)
if (bigDecimal.scale() > 2) return Error("Max 2 decimal places")
if (bigDecimal <= BigDecimal.ZERO) return Error("Must be > 0")
```
- **Why it matters**: Avoids binary floating-point roundoff errors inherent in IEEE 754 `Double` representations when dealing with financial transactions.

---

## 6. 16 Technical Interview Questions & Answers

### Q1: Why did you use `SQLiteOpenHelper` instead of Room?
**Answer:** While Room is recommended by Google for production apps as an abstraction layer, I used `SQLiteOpenHelper` here to demonstrate a deep understanding of core Android fundamentals: writing raw SQL statements, managing database version lifecycles (`onCreate`, `onUpgrade`), mapping `Cursor` data to model objects, and manually handling database transactions. It also avoids KSP/annotation processing dependencies.

### Q2: What is the 3-tier architecture and why did you choose it?
**Answer:** 3-tier architecture divides the app into:
1. **Presentation Layer**: Activities, Views, and Adapters.
2. **Repository Layer**: Coordinates data flow and encapsulates business logic.
3. **Data Layer**: Direct SQLite database helper.
This separation ensures that if we ever want to swap SQLite for Room or an API, the UI layer does not need to change a single line of code.

### Q3: How do you prevent memory leaks when working with SQLite Cursors?
**Answer:** In Java, you'd use a `try-finally` block to call `cursor.close()`. In Kotlin, the standard idiom is `cursor.use { ... }`, which implements `AutoCloseable`. Even if an unhandled runtime exception occurs inside the block, the cursor is guaranteed to be closed.

### Q4: Why did you use `BigDecimal` for amount validation instead of `Double.parseDouble()`?
**Answer:** Double uses binary floating-point arithmetic which can cause precision issues (e.g. `0.1 + 0.2 = 0.30000000000000004`). `BigDecimal` provides exact decimal representation, allows checking decimal scale (limiting to 2 decimal places for cents/paise), and rejects `NaN` and `Infinity` cleanly.

### Q5: How is SQL injection prevented in this application?
**Answer:** We never concatenate user strings directly into SQL queries. For all filtering and search queries, we use parameterized placeholders (`?`) and pass values via `selectionArgs: Array<String>`. The SQLite driver automatically escapes arguments.

### Q6: How does the RecyclerView improve performance over a ListView?
**Answer:** A `ListView` creates new views or relies on manual view holder implementations, whereas `RecyclerView` enforces the `ViewHolder` pattern at compile time. It recycles views that scroll off-screen, rebinding them with new data rather than inflating new XML view hierarchies, drastically lowering memory allocation and CPU usage.

### Q7: Why do we store dates as `"yyyy-MM-dd"` strings in SQLite?
**Answer:** SQLite doesn't have a dedicated `DATE` data type. Storing dates in ISO-8601 `"yyyy-MM-dd"` format guarantees that alphabetical sorting directly corresponds to chronological sorting. Therefore, `ORDER BY date DESC` correctly orders from newest to oldest.

### Q8: How did you implement newest-first sorting when two expenses occur on the same day?
**Answer:** We sort with a secondary tie-breaker: `ORDER BY date DESC, id DESC` or `created_at DESC`. Since `id` is an auto-incrementing integer, the higher ID was added later, guaranteeing deterministic newest-first order.

### Q9: What happens when the database is empty? How do aggregate queries behave?
**Answer:** SQLite `SUM(amount)` returns `NULL` when the table is empty. To prevent nullability issues, we query `SELECT COALESCE(SUM(amount), 0) FROM expenses`. This returns `0.0` safely. The UI also checks for empty lists and renders an empty state illustration with guidance.

### Q10: How does `AddExpenseActivity` communicate changes back to `MainActivity`?
**Answer:** In `MainActivity`, we refresh dashboard summaries and list data inside the `onResume()` lifecycle callback. When `AddExpenseActivity` inserts a record and calls `finish()`, control returns to `MainActivity`, triggering `onResume()` which fetches fresh totals from SQLite.

### Q11: How do you prevent users from submitting the form twice by tapping "Save" rapidly?
**Answer:** In `AddExpenseActivity.saveExpense()`, we maintain an `isSavingInProgress` boolean flag and immediately disable `btnSave.isEnabled = false` before executing the database write.

### Q12: Why are database operations kept out of the `ExpenseAdapter`?
**Answer:** The adapter's sole responsibility is view binding (Presentational). If an adapter executes database operations, it violates the Single Responsibility Principle and couples the UI with the persistence layer. Instead, the adapter exposes a callback lambda `onDeleteClicked: (Expense) -> Unit`, delegating the action to the Activity and Repository.

### Q13: What is the purpose of `ContentValues` in SQLite?
**Answer:** `ContentValues` is a key-value mapping class used by Android's `SQLiteDatabase.insert()` and `update()` methods. It automatically handles type conversion and sanitization for SQL statements.

### Q14: How does `onUpgrade()` work in SQLiteOpenHelper?
**Answer:** When the app's `DATABASE_VERSION` constant is incremented, Android calls `onUpgrade(db, oldVersion, newVersion)`. Here, schema migration scripts (such as `ALTER TABLE ADD COLUMN`) can be executed incrementally without calling `DROP TABLE` to prevent user data loss.

### Q15: How does combined search and filtering work simultaneously?
**Answer:** The method `searchAndFilterExpenses(query, category)` checks whether both query and category are active. If both are specified, it executes `WHERE category = ? AND title LIKE ?`. If only one is specified, it executes the corresponding single condition. If neither is specified, it returns all records.

### Q16: Why did you use `ExtendedFloatingActionButton` and `MaterialCardView`?
**Answer:** These are standard Material Design 3 components provided by Google's Material Components library. They provide built-in elevation shadows, rounded corner clipping, ripple feedback, and accessibility compliance out of the box.

---

## 7. Step-by-Step Live Demonstration Plan

Follow these steps during an interview to showcase every feature smoothly:

### Step 1: Initial Empty State
1. Launch the app.
2. Point out the dashboard cards showing:
   - Total Expenses: `₹ 0.00`
   - Highest Expense: `₹ 0.00 (None)`
   - Total Transactions: `0 entries`
   - Category Breakdown: All categories show `₹ 0`
3. Point out the empty state view: *"No Expenses Found. You haven't recorded any expenses yet."*

### Step 2: Input Validation Demonstration
1. Tap the **"+ Add Expense"** FAB.
2. Leave fields blank and tap **"Save Expense"**.
   - Show title error: *"Expense title cannot be empty."*
3. Enter only spaces `"   "` in the title and tap Save.
   - Show error: *"Expense title cannot be only whitespace."*
4. Enter title `"Dinner"`, leave amount empty, and tap Save.
   - Show amount error: *"Amount is required."*
5. Enter amount `"0"` or `"-50"` or `"abc"` and tap Save.
   - Show error: *"Amount must be greater than zero."*

### Step 3: Adding Expenses Across Multiple Categories
1. **Expense 1**: Title = `"Grocery Mart"`, Amount = `1450.00`, Category = `Food`, Date = Today. Tap **Save**.
   - Notice instant refresh: Total = `₹ 1,450.00`, Food = `₹ 1,450`, Highest = `Grocery Mart (₹ 1,450.00)`.
2. **Expense 2**: Title = `"Uber to Airport"`, Amount = `620.00`, Category = `Travel`, Date = Today. Tap **Save**.
3. **Expense 3**: Title = `"Winter Jacket"`, Amount = `3500.00`, Category = `Shopping`, Date = Yesterday. Tap **Save**.
   - Notice: Highest expense automatically updates to `"Winter Jacket (Shopping) - ₹ 3,500.00"`.
4. **Expense 4**: Title = `"Electricity Bill"`, Amount = `1850.00`, Category = `Bills`, Date = 3 days ago. Tap **Save**.
5. **Expense 5**: Title = `"Books"`, Amount = `400.00`, Category = `Other`, Date = Today. Tap **Save**.

### Step 4: Sorting & Chronological Verification
1. Show that today's expenses appear at the top of the list.
2. Between items with today's date, show that the most recently added item appears first due to the tie-breaking `id DESC` sort.

### Step 5: Search Demonstration
1. Type `"uber"` in the search bar.
   - List instantly filters to `"Uber to Airport"`.
2. Tap the `X` button to clear search.
   - Full list restores instantly.

### Step 6: Category Filter Demonstration
1. Tap the **"Food"** chip.
   - Only Food expenses appear.
2. Tap **"Travel"**, **"Shopping"**, etc.
   - Respective expenses appear.
3. Tap **"All"** to restore.

### Step 7: Combined Search & Category Filter
1. Select **"Travel"** category.
2. In the search bar, type `"airport"`.
   - Displays the matching Travel expense.
3. In the search bar, type `"grocery"`.
   - Displays *"No Matching Expenses"* empty state with a **"Reset Filters"** button.
4. Tap **"Reset Filters"**.
   - Filters reset back to All, clearing the search box.

### Step 8: Deletion with Confirmation
1. Tap the red trash icon on any expense.
2. Point out the native `AlertDialog` asking: *"Are you sure you want to delete '[Title]'? This action cannot be undone."*
3. Tap **"Cancel"** -> Show that the item is NOT deleted.
4. Tap the trash icon again, and tap **"Delete"**.
   - Show success toast: *"Expense deleted successfully"*.
   - Point out immediate recalculation of Total Expenses, Category Breakdown, and Transaction Count.

### Step 9: Persistence Test
1. Close the application completely from the Recent Apps switcher.
2. Re-open the application.
3. Verify that all previously recorded expenses and calculations are still present.
