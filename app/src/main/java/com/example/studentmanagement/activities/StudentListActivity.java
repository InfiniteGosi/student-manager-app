package com.example.studentmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.adapters.StudentAdapter;
import com.example.studentmanagement.data.Student;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class StudentListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private ArrayList<Student> studentList;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> addStudentLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_list);

        db = FirebaseFirestore.getInstance();
        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(this, studentList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);

        loadStudentsFromFirestore();

        addStudentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra("newStudent")) {
                            Student newStudent = (Student) data.getSerializableExtra("newStudent");
                            if (newStudent != null) {
                                // Lưu sinh viên mới vào Firestore
                                db.collection("students").document(newStudent.getStudentID()).set(newStudent);
                            }
                            // Cập nhật danh sách sinh viên
                            Toast.makeText(this, "Danh sách đã cập nhật", Toast.LENGTH_SHORT).show();
                            studentList.add(newStudent);
                            studentAdapter.notifyItemInserted(studentList.size() - 1);
                        }
                    }
                });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.item_student) {
                Toast.makeText(StudentListActivity.this, "Bạn đã ở danh sách", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.item_search) {
                Toast.makeText(StudentListActivity.this, "Search clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.item_add) {
                showAddDialog();
                return true;
            } else if (id == R.id.item_staff) {
                Toast.makeText(StudentListActivity.this, "Staffs clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.item_profile) {
                Toast.makeText(StudentListActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                return false;
            }
        });
    }

    public void refreshStudentList() {
        loadStudentsFromFirestore(); // Gọi lại phương thức để tải lại danh sách
    }

    private void loadStudentsFromFirestore() {
        db.collection("students")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        studentList.clear(); // Xóa danh sách cũ trước khi thêm mới
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Lấy dữ liệu từ Firestore và tạo đối tượng Student
                            String fullName = document.getString("fullName");
                            String dateOfBirth = document.getString("dateOfBirth");
                            String nationality = document.getString("nationality");
                            String phoneNumber = document.getString("phoneNumber");
                            String email = document.getString("email");
                            String studentID = document.getString("studentID");
                            String currentClass = document.getString("currentClass");
                            float gpa = document.getDouble("gpa").floatValue();
                            String guardianName = document.getString("guardianName");
                            String guardianPhone = document.getString("guardianPhone");

                            Student student = new Student(fullName, dateOfBirth, nationality, phoneNumber,
                                    email, studentID, currentClass, gpa, guardianName, guardianPhone);
                            studentList.add(student); // Thêm sinh viên vào danh sách
                        }
                        studentAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
                    } else {
                        Toast.makeText(this, "Lỗi tải dữ liệu: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bạn muốn thêm");
        String[] options = {"Sinh Viên", "Nhân Viên"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Khởi chạy AddStudentActivity và đợi kết quả
                Intent intent = new Intent(StudentListActivity.this, AddStudentActivity.class);
                addStudentLauncher.launch(intent);
            } else {
                Intent intent = new Intent(StudentListActivity.this, AddStaffActivity.class);
                startActivity(intent);
            }
        });
        builder.show();
    }
}
