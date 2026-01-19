//Expense.java
package com.example.expense;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "expenses")
public class Expense {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private int userId; // <-- assign this when inserting
    private String title;
    private String extraField;
    private double amount;
    private String date;
    private String category;

    // ===== Full Constructor =====
    public Expense(int userId, String title, String extraField, double amount, String date, String category) {
        this.userId = userId;
        this.title = title;
        this.extraField = extraField;
        this.amount = amount;
        this.date = date;
        this.category = category;
    }

    public Expense() {}

    // ===== Getters & Setters =====
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getExtraField() { return extraField; }
    public void setExtraField(String extraField) { this.extraField = extraField; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
