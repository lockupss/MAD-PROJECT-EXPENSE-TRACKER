package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ExpenseListFragment extends Fragment implements ExpenseAdapter.OnItemClickListener {

    private ExpenseManager expenseManager;
    private ExpenseAdapter adapter;
    private List<Expense> displayedExpenses = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private SearchView searchView;

    private boolean isLoading = false;
    private int currentPage = 0;
    private static final int PAGE_SIZE = 20;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthManager authManager = new AuthManager(requireContext());
        expenseManager = new ExpenseManager(requireContext(), authManager.getUserEmail());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense_list, container, false);

        recyclerView = view.findViewById(R.id.recycler);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        searchView = view.findViewById(R.id.searchView);

        setupRecyclerView();
        setupSearchView();
        loadExpenses();

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseAdapter(displayedExpenses, this);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == displayedExpenses.size() - 1 && !isLoading) {
                    loadMoreExpenses();
                }
            }
        });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    resetAndLoadExpenses();
                } else {
                    filterExpenses(newText);
                }
                return true;
            }
        });
    }

    private void loadExpenses() {
        isLoading = true;
        List<Expense> newExpenses = expenseManager.getExpenses(currentPage, PAGE_SIZE);
        displayedExpenses.addAll(newExpenses);
        adapter.notifyDataSetChanged();
        updateEmptyView();
        isLoading = false;
    }

    private void loadMoreExpenses() {
        currentPage++;
        loadExpenses();
    }

    private void resetAndLoadExpenses() {
        currentPage = 0;
        displayedExpenses.clear();
        loadExpenses();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetAndLoadExpenses();
    }

    private void filterExpenses(String query) {
        List<Expense> allExpenses = expenseManager.getExpenses();
        List<Expense> filteredExpenses = new ArrayList<>();
        for (Expense expense : allExpenses) {
            if (expense.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredExpenses.add(expense);
            }
        }
        displayedExpenses.clear();
        displayedExpenses.addAll(filteredExpenses);
        adapter.notifyDataSetChanged();
        updateEmptyView();
    }

    private void updateEmptyView() {
        if (displayedExpenses.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onEditClick(Expense expense) {
        Intent intent = new Intent(getActivity(), AddEditExpenseActivity.class);
        intent.putExtra("expense", expense);
        startActivity(intent);
    }

    @Override
    public void onDeleteClick(Expense expense) {
        expenseManager.deleteExpense(expense.getId());
        resetAndLoadExpenses();
    }
}
