package com.example.studentmanagement.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.studentmanagement.R;
import com.example.studentmanagement.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class UpdateUserActivity extends AppCompatActivity {

    private EditText etFullName, etAge, etPhoneNumber, etEmail;
    private Spinner spinnerRole;
    private CheckBox checkboxStatus;
    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        // Initialize UI components
        etFullName = findViewById(R.id.etFullName);
        etAge = findViewById(R.id.etAge);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        spinnerRole = findViewById(R.id.spinner_role);
        checkboxStatus = findViewById(R.id.checkbox_status);
        btnSave = findViewById(R.id.btnSave);

        String name = getIntent().getStringExtra("USER_NAME");
        String phone = getIntent().getStringExtra("USER_PHONE");
        String email = getIntent().getStringExtra("USER_EMAIL");
        String age = getIntent().getStringExtra("USER_AGE");
        boolean isLocked = getIntent().getBooleanExtra("USER_STATUS", false);

        // Set data to fields
        etFullName.setText(name);
        etPhoneNumber.setText(phone);
        etEmail.setText(email);
        checkboxStatus.setChecked(isLocked);
        etAge.setText(String.valueOf(age));

        // Define the roles array
        String[] rolesArray = {"Manager", "Employee"};

        // Set up ArrayAdapter for the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rolesArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        // Retrieve data from the intent
        String userRole = getIntent().getStringExtra("USER_ROLE");

        // Set the spinner selection based on the user's role
        int rolePosition = adapter.getPosition(userRole);
        if (rolePosition >= 0) {
            spinnerRole.setSelection(rolePosition);
        }

        // Save button event to handle the update logic
        // Inside btnSave.setOnClickListener in UpdateUserActivity
        btnSave.setOnClickListener(v -> {
            // Get updated data from UI
            String updatedName = etFullName.getText().toString();
            String updatedPhone = etPhoneNumber.getText().toString();
            String updatedEmail = etEmail.getText().toString();
            String updatedRole = spinnerRole.getSelectedItem().toString();  // Selected role from Spinner
            boolean updatedStatus = checkboxStatus.isChecked();
            String updatedAge = etAge.getText().toString();

            if (!isValidPhoneNumber(updatedPhone)) {
                Toast.makeText(this, "Định dạng số điện thoại không đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidEmail(updatedEmail)) {
                Toast.makeText(this, "Định dạng email không đúng", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isValidAges(Integer.parseInt(updatedAge))) {
                Toast.makeText(this, "Tuổi không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create an updated User object
            User updatedUser = new User(updatedName, Integer.parseInt(updatedAge), updatedPhone, updatedEmail, updatedRole, updatedStatus);

            // Get Firebase reference to update the user
            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
            databaseRef.orderByChild("email").equalTo(updatedEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        // Update the user node with new values
                        userSnapshot.getRef().setValue(updatedUser)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(UpdateUserActivity.this, "Dữ liệu người dùng đã được lưu", Toast.LENGTH_SHORT).show();
                                    setResult(RESULT_OK);  // Set result to OK to notify that update was successful
                                    finish();  // Close the activity
                                })
                                .addOnFailureListener(e -> Toast.makeText(UpdateUserActivity.this, "Lưu dữ liệu thất bại", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(UpdateUserActivity.this, "Lỗi khi cập nhật người dùng", Toast.LENGTH_SHORT).show();
                }
            });
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