# ExpenseCalculator

**Android Developer Interview Project — QTONZ**

An Android application developed using **Kotlin and SQLite** to record, organize, and calculate daily expenses. This project demonstrates fundamental Android development skills, local database management, form validation, and expense-related operations.

## 📌 Project Overview

ExpenseCalculator helps users maintain a record of their daily spending through a simple Android interface. Users can enter an expense title, amount, category, and date, and store the information locally on their device.

This project was developed as part of my preparation for an Android developer interview at **QTONZ**.

## 🎯 Objectives

- Develop an Android application using Kotlin.
- Implement local data persistence using SQLite.
- Create and manage expense records.
- Perform basic database CRUD operations.
- Validate user input before saving records.
- Calculate the total amount of recorded expenses.
- Demonstrate clean, readable, and maintainable code.

## ✨ Features

### 1. Add Expense
Enter the following information to create an expense:
- Expense title
- Amount
- Category
- Date

### 2. Expense Categories
Expenses can be organized into these categories:
- Food
- Travel
- Shopping
- Bills
- Other

### 3. View Expenses
Display saved expense records in the application.

### 4. Calculate Total Expenses
Calculate and display the total amount spent across saved expense records.

### 5. Edit and Delete Expenses
Update or remove existing expense records, if implemented.

### 6. Input Validation
Validate required fields and reject invalid or non-positive expense amounts.

### 7. Local Database
Store expense records in an SQLite database on the device.

## 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| Kotlin | Android application programming language |
| Android SDK | Android application development |
| SQLite | Local relational database |
| SQLiteOpenHelper | Database lifecycle management, if used |
| XML Layouts | UI design, if used |
| Android Studio | Development environment |
| Git and GitHub | Version control and project hosting |

## 🏗️ Application Architecture

The application separates user interaction, business logic, and data persistence.

**UI Layer**
Handles screens, forms, buttons, and expense lists.

**Business Logic Layer**
Validates inputs and performs calculations.

**Data Layer**
Executes database operations for expense records.

**Database Layer**
Stores expense information using SQLite.

### Application Workflow

User Input → Validation → Database Operation → Display Updated Expenses → Calculate Total

## 🗄️ Database Design

**Database:** ExpenseDatabase.db  
**Table:** expenses

| Column | Type | Purpose |
|---|---|---|
| id | INTEGER | Unique expense identifier |
| title | TEXT | Expense description |
| amount | REAL | Expense amount |
| category | TEXT | Expense category |
| date | TEXT | Expense date |

Example table definition:

```sql
CREATE TABLE expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    amount REAL NOT NULL,
    category TEXT NOT NULL,
    date TEXT NOT NULL
);
```

The schema shown above is illustrative. The actual column names and data types should match the implementation in the project.

## 🔄 CRUD Operations

- **Create:** Add a new expense.
- **Read:** Retrieve and display saved expenses.
- **Update:** Edit an existing expense.
- **Delete:** Remove an expense.

## 🧮 Total Expense Calculation

The total can be calculated using an SQL aggregate query:

```sql
SELECT COALESCE(SUM(amount), 0)
FROM expenses;
```

`SUM()` calculates the total amount. `COALESCE()` returns zero when no expense amounts are available.

## 📂 Project Structure

Example structure for a Kotlin project using XML layouts:

```text
ExpenseCalculator/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── your/package/name/
│   │       │       ├── MainActivity.kt
│   │       │       ├── Expense.kt
│   │       │       ├── DatabaseHelper.kt
│   │       │       └── ExpenseAdapter.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   ├── drawable/
│   │       │   └── values/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── screenshots/
├── README.md
├── .gitignore
└── LICENSE
```

Adapt the structure to match the actual project files.

## ⚙️ How to Run

### Requirements
- Android Studio
- Android SDK required by the project
- Android emulator or physical Android device

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/YOUR_USERNAME/ExpenseCalculator.git
   ```

2. Open the project in Android Studio.
3. Wait for Gradle synchronization.
4. Run the application on an emulator or Android device.

Replace `YOUR_USERNAME` with your GitHub username.

## 🧪 Testing Checklist

- [ ] Add an expense with valid details.
- [ ] Verify saved expenses appear in the list.
- [ ] Verify the total expense calculation.
- [ ] Confirm data persists after restarting the app.
- [ ] Test empty fields and invalid amounts.
- [ ] Test all available categories.
- [ ] Verify update and delete operations, if implemented.

## 🔒 Data Storage

Expense records are stored locally using SQLite. Basic expense operations do not require a remote database server.

## 🚀 Future Enhancements

- Monthly and weekly expense summaries
- Category-based filtering
- Expense search
- Budget management
- Spending charts
- CSV or PDF export
- Optional data backup and restore

## 📚 Learning Outcomes

Through this project, I demonstrate my understanding of:

- Kotlin programming
- Android UI development
- SQLite database operations
- CRUD functionality
- SQL aggregate queries
- Input validation
- Application testing and debugging
- Separation of UI and data responsibilities

## 👨‍💻 Developer

**Name:** Your Name

**Project:** ExpenseCalculator

**Interview Context:** QTONZ Android Developer Interview

**GitHub:** https://github.com/YOUR_USERNAME

## 📄 License

See the LICENSE file for the applicable license terms, if a license has been included.
