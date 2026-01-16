package com.example.expense;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.expense.data.AppDatabase;
import com.example.expense.data.entity.User;


public class SignUpActivity extends AppCompatActivity {

    EditText nameEt, emailEt, passwordEt, confirmPasswordEt;
    Button signUpBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameEt = findViewById(R.id.nameEt);
        emailEt = findViewById(R.id.emailEt);
        passwordEt = findViewById(R.id.passwordEt);
        confirmPasswordEt = findViewById(R.id.confirmPasswordEt);
        signUpBtn = findViewById(R.id.signUpBtn);

        AppDatabase db = AppDatabase.getInstance(this);

        signUpBtn.setOnClickListener(v -> {
            String name = nameEt.getText().toString().trim();
            String email = emailEt.getText().toString().trim();
            String password = passwordEt.getText().toString();
            String confirm = confirmPasswordEt.getText().toString();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "All fields required", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirm)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (db.userDao().getUserByEmail(email) != null) {
                Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show();
                return;
            }

            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password); // (plain for now)

            db.userDao().insert(user);

            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
            finish(); // go back to login
        });
    }
}
