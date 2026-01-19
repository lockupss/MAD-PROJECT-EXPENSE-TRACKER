package com.example.expense;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tabExpenses;
    LinearLayout layoutExpenses;

    RecyclerView recyclerExpenses;
    ExpenseAdapter adapter;

    ExpenseDatabase db;
    List<Expense> allExpenses = new ArrayList<>();
    SessionManager session;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        session = new SessionManager(this);
        userId = session.getUserId();

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // --- Initialize DB and Views ---
        db = ExpenseDatabase.getInstance(this);

        tabExpenses = findViewById(R.id.tabExpenses);
        layoutExpenses = findViewById(R.id.layoutExpenses);

        recyclerExpenses = findViewById(R.id.recyclerExpenses);
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ExpenseAdapter(new ArrayList<>(), this, expenseActionListener);
        recyclerExpenses.setAdapter(adapter);

        // --- Load User Expenses ---
        db.expenseDao()
                .getExpensesForUser(userId)
                .observe(this, expenses -> {
                    allExpenses = expenses;
                    adapter.updateList(expenses);
                });

        // --- Add Expense Button ---
        findViewById(R.id.btnAddExpense)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, AddExpenseActivity.class)));
    }

    // ================= EDIT & DELETE HANDLER =================
    private final ExpenseAdapter.OnExpenseActionListener expenseActionListener =
            new ExpenseAdapter.OnExpenseActionListener() {

                @Override
                public void onEdit(Expense expense) {
                    openEditDialog(expense);
                }

                @Override
                public void onDelete(Expense expense) {
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Expense")
                            .setMessage("Are you sure?")
                            .setPositiveButton("Delete", (d, w) ->
                                    new Thread(() ->
                                            db.expenseDao().deleteExpense(expense)).start())
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            };

    // ================= EDIT DIALOG =================
    private void openEditDialog(Expense expense) {
        View view = getLayoutInflater().inflate(R.layout.dialog_edit_expense, null);

        EditText etTitle = view.findViewById(R.id.etTitle);
        EditText etAmount = view.findViewById(R.id.etAmount);
        EditText etCategory = view.findViewById(R.id.etCategory);

        etTitle.setText(expense.getTitle());
        etAmount.setText(String.valueOf(expense.getAmount()));
        etCategory.setText(expense.getCategory());

        new AlertDialog.Builder(this)
                .setTitle("Edit Expense")
                .setView(view)
                .setPositiveButton("Save", (d, w) -> {
                    expense.setTitle(etTitle.getText().toString());
                    expense.setAmount(
                            Double.parseDouble(etAmount.getText().toString()));
                    expense.setCategory(etCategory.getText().toString());

                    new Thread(() ->
                            db.expenseDao().updateExpense(expense)).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
