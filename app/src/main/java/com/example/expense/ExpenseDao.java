//ExpenseDao.java
package com.example.expense;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;
import com.example.expense.Expense;
@Dao
public interface ExpenseDao {

    @Insert
    void insertExpense(Expense expense);

    @Update
    void updateExpense(Expense expense);

    @Delete
    void deleteExpense(Expense expense);

    //  New: get expenses for a specific user
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    LiveData<List<Expense>> getExpensesForUser(int userId);
}
