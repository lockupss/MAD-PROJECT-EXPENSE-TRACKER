package com.example.myapplication;

import java.io.Serializable;
import java.util.Objects;

public class Expense implements Serializable {
    private final String id;
    private final String name;
    private final String category;
    private final double amount;
    private final String date;

    public Expense(String id, String name, String category, double amount, String date) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expense expense = (Expense) o;
        return Double.compare(expense.amount, amount) == 0 &&
                id.equals(expense.id) &&
                Objects.equals(name, expense.name) &&
                Objects.equals(category, expense.category) &&
                Objects.equals(date, expense.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, category, amount, date);
    }
}
