package com.example.studentmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studentmanagement.R;
import com.example.studentmanagement.models.LoginEntry;
import com.example.studentmanagement.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends AppCompatActivity {
    EditText emailText, passwordText;
    Button loginBtn;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = findViewById(R.id.login_email_edit_text);
        passwordText = findViewById(R.id.login_password_edit_text);
        loginBtn = findViewById(R.id.login_button);
        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email = emailText.getText().toString();
                password = passwordText.getText().toString();

                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                    emailText.requestFocus();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                    passwordText.requestFocus();
                    return;
                }

                signIn(email, password);
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void signIn(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

                            // Check if user record exists in Realtime Database
                            userRef.get().addOnCompleteListener(userTask -> {
                                if (userTask.isSuccessful() && userTask.getResult().exists()) {
                                    User userData = userTask.getResult().getValue(User.class);
                                    if (userData != null && Boolean.TRUE.equals(userData.getLocked())) {
                                        Toast.makeText(LoginActivity.this, "Tài khoản này đã bị khóa", Toast.LENGTH_SHORT).show();
                                        mAuth.signOut();  // Sign out immediately
                                    } else {
                                        // Log the login event
                                        logLoginEvent(userId);

                                        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Tài khoản này đã bị xóa", Toast.LENGTH_SHORT).show();
                                    mAuth.signOut();
                                }
                            });
                        }
                    } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        Toast.makeText(LoginActivity.this, "Tài khoảng không tồn tại", Toast.LENGTH_SHORT).show();
                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(LoginActivity.this, "Mật khẩu đăng nhập không đúng", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Đăng nhập thất bại, vui lòng thử lại sau", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void logLoginEvent(String userId) {
        DatabaseReference loginHistoryRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("loginHistory");

        // Create a LoginEntry object with the current timestamp
        String timestamp = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
        LoginEntry loginEntry = new LoginEntry(timestamp);

        // Add a new LoginEntry under the loginHistory node
        loginHistoryRef.push().setValue(loginEntry)
                .addOnSuccessListener(aVoid -> {
                    // Successfully logged the login event
                })
                .addOnFailureListener(e -> {
                    // Handle any error if necessary
                    Toast.makeText(LoginActivity.this, "Failed to log login event.", Toast.LENGTH_SHORT).show();
                });
    }




}