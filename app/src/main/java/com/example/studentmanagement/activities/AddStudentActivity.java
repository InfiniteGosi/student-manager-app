package com.example.studentmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.studentmanagement.R;
import com.example.studentmanagement.data.Student;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddStudentActivity extends AppCompatActivity {

    private EditText etFullName, etDateOfBirth, etNationality, etPhoneNumber, etEmail;
    private EditText etStudentID, etCurrentClass, etGPA;
    private EditText etGuardianName, etGuardianPhone;
    private AppCompatButton btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_add);

        // Ánh xạ các thành phần giao diện
        etFullName = findViewById(R.id.etFullName);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        etNationality = findViewById(R.id.etNationality);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmail = findViewById(R.id.etEmail);
        etStudentID = findViewById(R.id.etStudentID);
        etCurrentClass = findViewById(R.id.etCurrentClass);
        etGPA = findViewById(R.id.etGPA);
        etGuardianName = findViewById(R.id.etGuardianName);
        etGuardianPhone = findViewById(R.id.etGuardianPhone);
        btnSave = findViewById(R.id.btnSave);

        // Thiết lập sự kiện khi nhấn nút Lưu
        btnSave.setOnClickListener(view -> saveStudentInfo());
    }

    private void saveStudentInfo() {
        String fullName = etFullName.getText().toString().trim();
        String dateOfBirth = etDateOfBirth.getText().toString().trim();
        String nationality = etNationality.getText().toString().trim();
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String studentID = etStudentID.getText().toString().trim();
        String currentClass = etCurrentClass.getText().toString().trim();
        String guardianName = etGuardianName.getText().toString().trim();
        String guardianPhone = etGuardianPhone.getText().toString().trim();

        // Kiểm tra thông tin đầu vào
        if (fullName.isEmpty() || dateOfBirth.isEmpty() || nationality.isEmpty() ||
                phoneNumber.isEmpty() || studentID.isEmpty() || currentClass.isEmpty() ||
                guardianName.isEmpty() || guardianPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra định dạng email
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra định dạng số điện thoại
        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra định dạng ngày sinh (định dạng: dd/MM/yyyy)
        if (!isValidDate(dateOfBirth)) {
            Toast.makeText(this, "Ngày sinh không hợp lệ. Định dạng: dd/MM/yyyy", Toast.LENGTH_SHORT).show();
            return;
        }

        float gpa;
        try {
            gpa = Float.parseFloat(etGPA.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Điểm GPA không hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo đối tượng sinh viên mới
        Student newStudent = new Student(fullName, dateOfBirth, nationality, phoneNumber,
                email, studentID, currentClass, gpa, guardianName, guardianPhone);

        // Truyền đối tượng sinh viên mới về StudentListActivity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newStudent", newStudent);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(this, "Thêm sinh viên thành công", Toast.LENGTH_SHORT).show();

        // Kết thúc AddStudentActivity và quay về StudentListActivity
        new Handler().postDelayed(() -> {
            finish(); // Kết thúc Activity sau thời gian chờ
        }, 2000);
    }
    // Phương thức kiểm tra định dạng email
    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        return email.matches(emailPattern);
    }

    // Phương thức kiểm tra định dạng số điện thoại (giả định số điện thoại Việt Nam 10 hoặc 11 chữ số)
    private boolean isValidPhoneNumber(String phoneNumber) {
        String phonePattern = "0\\d{9,10}"; // Bắt đầu bằng 0 và theo sau là 9 hoặc 10 chữ số
        return phoneNumber.matches(phonePattern);
    }

    // Phương thức kiểm tra định dạng ngày sinh
    private boolean isValidDate(String date) {
        String datePattern = "\\d{2}/\\d{2}/\\d{4}"; // Định dạng dd/MM/yyyy
        return date.matches(datePattern);
    }
}
