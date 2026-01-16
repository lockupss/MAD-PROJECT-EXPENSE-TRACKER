package com.example.expense;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.expense.data.AppDatabase;
import com.example.expense.data.entity.User;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput, passwordInput;
    Button loginBtn;
    TextView signUpText, createAccountText;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init database
        db = AppDatabase.getInstance(this);

        // Bind views
        emailInput = findViewById(R.id.emailEt);
        passwordInput = findViewById(R.id.passwordEt);
        loginBtn = findViewById(R.id.loginBtn);
        signUpText = findViewById(R.id.signUpText);
        createAccountText = findViewById(R.id.createAccountText);

        // LOGIN BUTTON
        loginBtn.setOnClickListener(v -> loginUser());

        // SIGN UP LINKS
        signUpText.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        createAccountText.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Room DB query
        User user = db.userDao().login(email, password);

        if (user != null) {
            // Save user ID in session
            SessionManager session = new SessionManager(LoginActivity.this);
            session.saveUserId(user.getUid());

            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("userId", user.getUid());  // pass logged-in user ID
            startActivity(intent);
            finish();
            // prevent going back to login
        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
        }
    }

}
