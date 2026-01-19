package com.example.expense;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    TextView tabExpenses, tabSummary;
    LinearLayout layoutExpenses, layoutSummary;

    RecyclerView recyclerExpenses, recyclerCategoryExpenses;

    ExpenseAdapter adapter, categoryAdapter;

    TextView tvTotalSpent, tvThisMonthTotal;
    PieChart pieChart;

    LinearLayout layoutSelectedCategory;
    TextView tvSelectedCategory, tvSelectedAmount;

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
        tabSummary = findViewById(R.id.tabSummary);
        layoutExpenses = findViewById(R.id.layoutExpenses);
        layoutSummary = findViewById(R.id.layoutSummary);

        recyclerExpenses = findViewById(R.id.recyclerExpenses);
        recyclerExpenses.setLayoutManager(new LinearLayoutManager(this));

        recyclerCategoryExpenses = findViewById(R.id.recyclerCategoryExpenses);
        recyclerCategoryExpenses.setLayoutManager(new LinearLayoutManager(this));

        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvThisMonthTotal = findViewById(R.id.tvThisMonthTotal);

        pieChart = findViewById(R.id.pieChart);

        layoutSelectedCategory = findViewById(R.id.layoutSelectedCategory);
        tvSelectedCategory = findViewById(R.id.tvSelectedCategory);
        tvSelectedAmount = findViewById(R.id.tvSelectedAmount);

        adapter = new ExpenseAdapter(new ArrayList<>(), this, expenseActionListener);
        categoryAdapter = new ExpenseAdapter(new ArrayList<>(), this, expenseActionListener);

        recyclerExpenses.setAdapter(adapter);
        recyclerCategoryExpenses.setAdapter(categoryAdapter);

        // --- Load User Expenses ---
        db.expenseDao()
                .getExpensesForUser(userId)
                .observe(this, expenses -> {
                    allExpenses = expenses;
                    adapter.updateList(expenses);
                    refreshSummary(expenses);
                });

        // --- Buttons ---
        findViewById(R.id.btnAddExpense)
                .setOnClickListener(v -> startActivity(new Intent(this, AddExpenseActivity.class)));

        tabExpenses.setOnClickListener(v -> showExpenses());
        tabSummary.setOnClickListener(v -> showSummary());
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
                                    new Thread(() -> db.expenseDao().deleteExpense(expense)).start())
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
                    expense.setAmount(Double.parseDouble(etAmount.getText().toString()));
                    expense.setCategory(etCategory.getText().toString());

                    new Thread(() -> db.expenseDao().updateExpense(expense)).start();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ================= SUMMARY =================
    private void refreshSummary(List<Expense> expenses) {

        float total = 0f;
        Map<String, Float> categoryTotals = new HashMap<>();

        for (Expense e : expenses) {
            total += e.getAmount();
            categoryTotals.put(
                    e.getCategory(),
                    categoryTotals.getOrDefault(e.getCategory(), 0f) + (float) e.getAmount()
            );
        }

        tvTotalSpent.setText("ETB " + total);
        tvThisMonthTotal.setText("ETB " + calculateThisMonthTotal(expenses));

        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        PieData data = new PieData(dataSet);
        pieChart.setUsePercentValues(true);
        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                PieEntry pe = (PieEntry) e;
                String category = pe.getLabel();

                List<Expense> filtered = new ArrayList<>();
                float sum = 0f;

                for (Expense ex : allExpenses) {
                    if (ex.getCategory().equals(category)) {
                        filtered.add(ex);
                        sum += ex.getAmount();
                    }
                }

                tvSelectedCategory.setText(category);
                tvSelectedAmount.setText("ETB " + sum);

                layoutSelectedCategory.setVisibility(View.VISIBLE);
                recyclerCategoryExpenses.setVisibility(View.VISIBLE);
                categoryAdapter.updateList(filtered);
            }

            @Override
            public void onNothingSelected() {
                layoutSelectedCategory.setVisibility(View.GONE);
                recyclerCategoryExpenses.setVisibility(View.GONE);
            }
        });
    }

    // ================= THIS MONTH CALCULATION =================
    private float calculateThisMonthTotal(List<Expense> expenses) {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        float total = 0f;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (Expense e : expenses) {
            try {
                Calendar expenseDate = Calendar.getInstance();
                expenseDate.setTime(sdf.parse(e.getDate()));

                if (expenseDate.get(Calendar.MONTH) == currentMonth &&
                        expenseDate.get(Calendar.YEAR) == currentYear) {
                    total += e.getAmount();
                }
            } catch (Exception ignored) {}
        }
        return total;
    }

    private void showExpenses() {
        layoutExpenses.setVisibility(View.VISIBLE);
        layoutSummary.setVisibility(View.GONE);
    }

    private void showSummary() {
        layoutExpenses.setVisibility(View.GONE);
        layoutSummary.setVisibility(View.VISIBLE);
    }
}  
