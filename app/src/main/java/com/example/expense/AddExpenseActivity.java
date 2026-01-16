package com.example.expense;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {

    EditText etTitle, etCategory, etNote, etAmount;
    Button btnSave;

    ExpenseDatabase db;
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);

        etTitle = findViewById(R.id.etTitle);
        etCategory = findViewById(R.id.etCategory);
        etNote = findViewById(R.id.etNote);
        etAmount = findViewById(R.id.etAmount);
        btnSave = findViewById(R.id.btnSave);

        db = ExpenseDatabase.getInstance(this);
        session = new SessionManager(this);

        btnSave.setOnClickListener(v -> {

            int userId = session.getUserId();
            if (userId == -1) {
                Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
                return;
            }

            String title = etTitle.getText().toString();
            String category = etCategory.getText().toString();
            String note = etNote.getText().toString();
            double amount = Double.parseDouble(etAmount.getText().toString());

            String date = new SimpleDateFormat(
                    "yyyy-MM-dd", Locale.getDefault()
            ).format(new Date());

            Expense expense = new Expense(
                    session.getUserId(), title, note, amount, date, category
            );

            new Thread(() ->
                    db.expenseDao().insertExpense(expense)
            ).start();

            finish();
        });
    }
}
