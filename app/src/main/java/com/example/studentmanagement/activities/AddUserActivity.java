package com.example.studentmanagement.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.studentmanagement.R;
import com.example.studentmanagement.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class AddUserActivity extends AppCompatActivity {
    EditText etName, etAge, etPhone, etEmail;
    Spinner roleSpinner;
    CheckBox lockChBox;
    Button btnSave;

    private FirebaseAuth auth;
    private DatabaseReference usersDbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_add);

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        usersDbRef = FirebaseDatabase.getInstance().getReference("users");

        // Initialize UI elements
        etName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        lockChBox = findViewById(R.id.checkbox_status);
        btnSave = findViewById(R.id.btnSave);

        // Set up spinner
        String[] roles = {"Manager", "Employee"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner = findViewById(R.id.spinner_role); // Initialize spinner
        roleSpinner.setAdapter(adapter);

        // Set onClickListener for Save button
        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String phone = etPhone.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();
            boolean isLocked = lockChBox.isChecked();

            // Validate inputs
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(age) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Vui lòng điền hết thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidPhoneNumber(phone)) {
                Toast.makeText(this, "Định dạng số điện thoại không đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(email)) {
                Toast.makeText(this, "Định dạng email không đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidAges(Integer.parseInt(age))) {
                Toast.makeText(this, "Tuổi không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user in Firebase Authentication and add user data to Firebase Database
            createUserInFirebase(name, age, phone, email, role, isLocked);
        });
    }

    private void createUserInFirebase(String name, String age, String phone, String email, String role, boolean isLocked) {
        // Save original user credentials before signing out, if needed
        FirebaseUser originalUser = auth.getCurrentUser();
        String originalEmail = (originalUser != null) ? originalUser.getEmail() : null;
        String originalPassword = "original_password";  // Securely fetch or store this if possible

        // Attempt to re-authenticate with the deleted account email
        auth.signOut();
        auth.signInWithEmailAndPassword(email, "111111").addOnCompleteListener(signInTask -> {
            if (signInTask.isSuccessful()) {
                // Account exists in Firebase Authentication but not in Database - delete it first
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.delete().addOnCompleteListener(deleteTask -> {
                        if (deleteTask.isSuccessful()) {
                            // Now create a new account with the same email
                            createNewUser(name, age, phone, email, role, isLocked, originalEmail, originalPassword);
                        } else {
                            Toast.makeText(this, "Failed to delete old user account: " + deleteTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } else {
                // If sign-in failed, account truly doesn’t exist - proceed with creation
                createNewUser(name, age, phone, email, role, isLocked, originalEmail, originalPassword);
            }
        });
    }

    private void createNewUser(String name, String age, String phone, String email, String role, boolean isLocked,
                               String originalEmail, String originalPassword) {
        auth.createUserWithEmailAndPassword(email, "111111")
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User user = new User(name, Integer.parseInt(age), phone, email, role, isLocked);

                            usersDbRef.child(userId).setValue(user).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show();

                                    // Re-authenticate original user after adding new user
                                    if (originalEmail != null && originalPassword != null) {
                                        auth.signOut();
                                        auth.signInWithEmailAndPassword(originalEmail, originalPassword)
                                                .addOnCompleteListener(reAuthTask -> {
                                                    setResult(RESULT_OK);
                                                    finish();
                                                });
                                    } else {
                                        setResult(RESULT_OK);
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to add user to database", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "User creation failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private boolean isValidPhoneNumber(String phoneNumber) {
        String phonePattern = "0\\d{9,10}"; // Bắt đầu bằng 0 và theo sau là 9 hoặc 10 chữ số
        return phoneNumber.matches(phonePattern);
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }

    private boolean isValidAges(int age) {
        return (age >= 18 && age <= 50);
    }

}
