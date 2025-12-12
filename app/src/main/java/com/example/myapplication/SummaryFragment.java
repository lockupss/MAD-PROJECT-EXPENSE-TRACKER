package com.example.myapplication;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class SummaryFragment extends Fragment {

    private ExpenseManager expenseManager;
    private List<Expense> expenses;
    private TextView tvTotalExpenses;
    private BarChart barChart;
    private TextView tvDateFilter;
    private Calendar selectedDate = Calendar.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthManager authManager = new AuthManager(requireContext());
        expenseManager = new ExpenseManager(requireContext(), authManager.getUserEmail());
        expenses = expenseManager.getExpenses();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary, container, false);

        tvTotalExpenses = view.findViewById(R.id.tvTotalExpenses);
        barChart = view.findViewById(R.id.barChart);
        tvDateFilter = view.findViewById(R.id.tvDateFilter);

        tvDateFilter.setOnClickListener(v -> showDatePicker());

        updateSummary();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        expenses = expenseManager.getExpenses();
        updateSummary();
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            selectedDate.set(year, month, dayOfMonth);
            updateSummary();
        }, selectedDate.get(Calendar.YEAR), selectedDate.get(Calendar.MONTH), selectedDate.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateSummary() {
        if (expenses == null) return;

        List<Expense> filteredExpenses = filterExpensesByWeek();
        double totalAmount = 0;
        for (Expense expense : filteredExpenses) {
            totalAmount += expense.getAmount();
        }
        tvTotalExpenses.setText(String.format("Total Expenses: $%.2f", totalAmount));

        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        Calendar firstDayOfWeek = (Calendar) selectedDate.clone();
        firstDayOfWeek.set(Calendar.DAY_OF_WEEK, firstDayOfWeek.getFirstDayOfWeek());
        Calendar lastDayOfWeek = (Calendar) firstDayOfWeek.clone();
        lastDayOfWeek.add(Calendar.DAY_OF_WEEK, 6);
        tvDateFilter.setText(String.format("%s - %s", sdf.format(firstDayOfWeek.getTime()), sdf.format(lastDayOfWeek.getTime())));

        setupBarChart(filteredExpenses);
    }

    private List<Expense> filterExpensesByWeek() {
        List<Expense> filteredList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();

        for (Expense expense : expenses) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                cal.setTime(sdf.parse(expense.getDate()));

                if (isSameWeek(cal, selectedDate)) {
                    filteredList.add(expense);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return filteredList;
    }

    private boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    private void setupBarChart(List<Expense> expenses) {
        Map<String, Float> dayMap = new TreeMap<>();
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) selectedDate.clone();
            day.set(Calendar.DAY_OF_WEEK, day.getFirstDayOfWeek() + i);
            SimpleDateFormat sdf = new SimpleDateFormat("EEE", Locale.US);
            dayMap.put(sdf.format(day.getTime()), 0f);
        }

        for (Expense expense : expenses) {
            float amount = (float) expense.getAmount();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(expense.getDate()));
                SimpleDateFormat dayFormat = new SimpleDateFormat("EEE", Locale.US);
                String dayOfWeek = dayFormat.format(cal.getTime());
                dayMap.put(dayOfWeek, dayMap.getOrDefault(dayOfWeek, 0f) + amount);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Float> entry : dayMap.entrySet()) {
            entries.add(new BarEntry(i, entry.getValue()));
            labels.add(entry.getKey());
            i++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Daily Expenses");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.animateY(1000);
        barChart.invalidate();
    }
}
