package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AddEditExpenseActivity extends AppCompatActivity {

    private ExpenseManager expenseManager;
    private EditText etTitle, etAmount, etCategory, etDate;
    private Expense existingExpense;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_expense);

        AuthManager authManager = new AuthManager(this);
        expenseManager = new ExpenseManager(this, authManager.getUserEmail());

        etTitle = findViewById(R.id.etTitle);
        etAmount = findViewById(R.id.etAmount);
        etCategory = findViewById(R.id.etCategory);
        etDate = findViewById(R.id.etDate);
        Button btnSave = findViewById(R.id.btnSave);

        existingExpense = (Expense) getIntent().getSerializableExtra("expense");

        if (existingExpense != null) {
            etTitle.setText(existingExpense.getName());
            etAmount.setText(String.valueOf(existingExpense.getAmount()));
            etCategory.setText(existingExpense.getCategory());
            etDate.setText(existingExpense.getDate());
        }

        btnSave.setOnClickListener(v -> saveExpense());
    }

    private void saveExpense() {
        String title = etTitle.getText().toString();
        String amountStr = etAmount.getText().toString();
        String category = etCategory.getText().toString();
        String date = etDate.getText().toString();

        if (title.isEmpty() || amountStr.isEmpty() || category.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        if (existingExpense != null) {
            Expense updatedExpense = new Expense(existingExpense.getId(), title, category, amount, date);
            expenseManager.updateExpense(existingExpense.getId(), updatedExpense);
            Toast.makeText(this, "Expense updated", Toast.LENGTH_SHORT).show();
        } else {
            String id = String.valueOf(System.currentTimeMillis());
            Expense newExpense = new Expense(id, title, category, amount, date);
            expenseManager.addExpense(newExpense);
            Toast.makeText(this, "Expense added", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
