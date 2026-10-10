package com.example.expensecalculator

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter for displaying expense items.
 *
 * Exposes a lambda callback [onDeleteClicked] so that business logic and SQLite
 * operations remain strictly inside the Repository / Activity.
 */
class ExpenseAdapter(
    private val onDeleteClicked: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    private val expenses = mutableListOf<Expense>()

    fun submitList(newList: List<Expense>) {
        expenses.clear()
        expenses.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(expenses[position])
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconContainer: FrameLayout = itemView.findViewById(R.id.iconCategoryContainer)
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.ivCategoryIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvExpenseTitle)
        private val tvCategoryBadge: TextView = itemView.findViewById(R.id.tvCategoryBadge)
        private val tvDate: TextView = itemView.findViewById(R.id.tvExpenseDate)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvExpenseAmount)
        private val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteExpense)

        fun bind(expense: Expense) {
            val context = itemView.context
            tvTitle.text = expense.title
            tvAmount.text = ExpenseUtils.formatCurrency(expense.amount)
            tvDate.text = ExpenseUtils.formatDisplayDate(expense.date)
            tvCategoryBadge.text = expense.category

            configureCategoryVisuals(context, expense.category)

            btnDelete.setOnClickListener {
                onDeleteClicked(expense)
            }
        }

        private fun configureCategoryVisuals(context: Context, category: String) {
            when (category) {
                Expense.CATEGORY_FOOD -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_food)
                    ivCategoryIcon.setImageResource(R.drawable.ic_food)
                    ivCategoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cat_food_text))
                    tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.cat_food_text))
                }
                Expense.CATEGORY_TRAVEL -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_travel)
                    ivCategoryIcon.setImageResource(R.drawable.ic_travel)
                    ivCategoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cat_travel_text))
                    tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.cat_travel_text))
                }
                Expense.CATEGORY_SHOPPING -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_shopping)
                    ivCategoryIcon.setImageResource(R.drawable.ic_shopping)
                    ivCategoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cat_shopping_text))
                    tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.cat_shopping_text))
                }
                Expense.CATEGORY_BILLS -> {
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_bills)
                    ivCategoryIcon.setImageResource(R.drawable.ic_bills)
                    ivCategoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cat_bills_text))
                    tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.cat_bills_text))
                }
                else -> {
                    // Category Other
                    iconContainer.setBackgroundResource(R.drawable.bg_badge_other)
                    ivCategoryIcon.setImageResource(R.drawable.ic_other)
                    ivCategoryIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.cat_other_text))
                    tvCategoryBadge.setTextColor(ContextCompat.getColor(context, R.color.cat_other_text))
                }
            }
        }
    }
}
