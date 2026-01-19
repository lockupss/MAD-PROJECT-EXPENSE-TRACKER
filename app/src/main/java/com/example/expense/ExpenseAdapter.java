//ExpenseAdapter.java
package com.example.expense;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    public interface OnExpenseActionListener {
        void onEdit(Expense expense);
        void onDelete(Expense expense);
    }

    List<Expense> list;
    Context context;
    OnExpenseActionListener listener;

    public ExpenseAdapter(List<Expense> list, Context context, OnExpenseActionListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    public void updateList(List<Expense> newList) {
        this.list = newList;
        notifyDataSetChanged();
    }

    @Override
    public ExpenseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ExpenseViewHolder holder, int position) {
        Expense e = list.get(position);

        holder.tvTitle.setText(e.getTitle());
        holder.tvCategory.setText(e.getCategory());
        holder.tvAmount.setText("ETB " + e.getAmount());
        holder.tvDate.setText(e.getDate());

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(e));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(e));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount, tvDate;
        Button btnEdit, btnDelete;

        public ExpenseViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
