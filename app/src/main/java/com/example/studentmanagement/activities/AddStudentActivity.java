package com.example.studentmanagement.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.studentmanagement.R;
import com.example.studentmanagement.data.Student;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddStudentActivity extends AppCompatActivity {

    private EditText etFullName, etDateOfBirth, etNationality, etPhoneNumber, etEmail;
    private EditText etStudentID, etCurrentClass, etGPA;
    private EditText etGuardianName, etGuardianPhone;
    private AppCompatButton btnSave;
    private static final int PICK_CSV_FILE = 1;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        readFileAndUpload(uri, ",");
                    }
                } else {
                    Toast.makeText(this, "Tải file thất bại", Toast.LENGTH_SHORT).show();
                }
            }
    );

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

        TextView addFromFile = findViewById(R.id.addFromFile);
        addFromFile.setOnClickListener(v -> selectFile());

        // Thiết lập sự kiện khi nhấn nút Lưu
        btnSave.setOnClickListener(view -> saveStudentInfo());
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*"); // Cho phép tất cả các loại file văn bản
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/csv", "application/vnd.ms-excel", "text/*"});
        activityResultLauncher.launch(intent);
    }

    private void readFileAndUpload(Uri uri, String delimiter) {
        if (uri == null) {
            Toast.makeText(this, "URI không hợp lệ hoặc tệp không được chọn.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Student> studentsList = new ArrayList<>(); // List to hold student objects

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(delimiter);

                if (fields.length == 10) {
                    // Create a new student object
                    Student student = new Student(
                            fields[0].trim(),
                            fields[1].trim(),
                            fields[2].trim(),
                            fields[3].trim(),
                            fields[4].trim(),
                            fields[5].trim(),
                            fields[6].trim(),
                            Float.parseFloat(fields[7].trim()),
                            fields[8].trim(),
                            fields[9].trim()
                    );

                    // Add the student to the list
                    studentsList.add(student);
                } else {
                    Log.e("File Error", "Dữ liệu không đúng định dạng.");
                }
            }

            // After reading all students, send them to MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("studentsList", new ArrayList<>(studentsList));  // Sending the list as an extra
            setResult(RESULT_OK, intent);

            Toast.makeText(this, "Thêm sinh viên thành công", Toast.LENGTH_SHORT).show();

            // Kết thúc AddStudentActivity và quay về
            new Handler().postDelayed(() -> {
                finish(); // Kết thúc Activity sau thời gian chờ
            }, 2000);

            Toast.makeText(this, "Thêm sinh viên từ file thành công!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e("File Error", "Lỗi khi đọc file", e);
            Toast.makeText(this, "Lỗi khi đọc file", Toast.LENGTH_SHORT).show();
        }
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

        // Validate inputs
        if (fullName.isEmpty() || dateOfBirth.isEmpty() || nationality.isEmpty() ||
                phoneNumber.isEmpty() || studentID.isEmpty() || currentClass.isEmpty() ||
                guardianName.isEmpty() || guardianPhone.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền hết tất cả thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Định dạng email không đúng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhoneNumber(phoneNumber)) {
            Toast.makeText(this, "Định dạng số điện thoại không đúng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDate(dateOfBirth)) {
            Toast.makeText(this, "Định dạng ngày không đúng (dd/MM/yyyy).", Toast.LENGTH_SHORT).show();
            return;
        }

        float gpa;
        try {
            gpa = Float.parseFloat(etGPA.getText().toString().trim());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid GPA.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if studentID exists in Firebase
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference studentRef = database.getReference("students");

        studentRef.child(studentID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                Toast.makeText(this, "Student ID already exists. Please use a different ID.", Toast.LENGTH_SHORT).show();
            } else {
                // Save the new student
                Student newStudent = new Student(fullName, dateOfBirth, nationality, phoneNumber,
                        email, studentID, currentClass, gpa, guardianName, guardianPhone);

                studentRef.child(studentID).setValue(newStudent)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Student added successfully.", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(this::finish, 2000);
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to add student.", Toast.LENGTH_SHORT).show());
            }
        });
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
