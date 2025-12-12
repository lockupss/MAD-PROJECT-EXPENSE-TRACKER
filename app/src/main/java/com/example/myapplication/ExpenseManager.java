package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ExpenseManager {
    private static final String PREF_NAME = "expenses_pref";
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public ExpenseManager(Context context, String userEmail) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME + "_" + userEmail, Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    public List<Expense> getExpenses() {
        String json = sharedPreferences.getString("expenses", null);
        if (json == null) {
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Expense>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public List<Expense> getExpenses(int page, int pageSize) {
        List<Expense> allExpenses = getExpenses();
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allExpenses.size());

        if (start >= allExpenses.size()) {
            return new ArrayList<>();
        }

        return new ArrayList<>(allExpenses.subList(start, end));
    }

    public void saveExpenses(List<Expense> expenses) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(expenses);
        editor.putString("expenses", json);
        editor.apply();
    }

    public void addExpense(Expense expense) {
        List<Expense> expenses = getExpenses();
        expenses.add(0, expense);
        saveExpenses(expenses);
    }

    public void updateExpense(String id, Expense updatedExpense) {
        List<Expense> expenses = getExpenses();
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(id)) {
                expenses.set(i, new Expense(id, updatedExpense.getName(), updatedExpense.getCategory(), updatedExpense.getAmount(), updatedExpense.getDate()));
                break;
            }
        }
        saveExpenses(expenses);
    }

    public void deleteExpense(String id) {
        List<Expense> expenses = getExpenses();
        for (int i = 0; i < expenses.size(); i++) {
            if (expenses.get(i).getId().equals(id)) {
                expenses.remove(i);
                break;
            }
        }
        saveExpenses(expenses);
    }
}
