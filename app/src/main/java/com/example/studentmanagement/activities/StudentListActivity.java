package com.example.studentmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuInflater;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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
import java.util.Collections;
import java.util.Comparator;

public class StudentListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private ArrayList<Student> studentList;
    private ArrayList<Student> originalStudentList;
    private FirebaseFirestore db;
    private ActivityResultLauncher<Intent> addStudentLauncher;
    private SearchView searchView;
    private ImageView imgSort;
    private boolean isAscending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_list);

        imgSort = findViewById(R.id.imgSort);

        db = FirebaseFirestore.getInstance();
        studentList = new ArrayList<>();
        originalStudentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(this, studentList);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(studentAdapter);

        searchView = findViewById(R.id.searchView);

        loadStudentsFromFirestore();

        // Lọc danh sách khi người dùng nhập từ khóa vào SearchView
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    // Nếu tìm kiếm rỗng, hiển thị lại danh sách gốc
                    refreshStudentList();
                } else {
                    filterStudentList(newText); // Gọi phương thức lọc khi người dùng thay đổi từ khóa
                }
                return true;
            }
        });

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

        imgSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(StudentListActivity.this, imgSort);
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu_sort, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.sort_by_name) {
                    sortStudentListByName();
                    return true;
                } else if (item.getItemId() == R.id.sort_by_dob) {
                    sortStudentListByDob();
                    return true;
                } else if (item.getItemId() == R.id.sort_by_student_id) {
                    sortStudentListByStudentId();
                    return true;
                } else if (item.getItemId() == R.id.sort_by_class) {
                    sortStudentListByClass();
                    return true;
                } else if (item.getItemId() == R.id.sort_by_gpa) {
                    sortStudentListByGpa();
                    return true;
                } else {
                    return false;
                }
            });

            popupMenu.show(); // Hiển thị menu
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

    private void filterStudentList(String query) {
        ArrayList<Student> filteredList = new ArrayList<>();
        for (Student student : studentList) {
            if (student.getFullName().toLowerCase().contains(query.toLowerCase()) ||
                    student.getStudentID().toLowerCase().contains(query.toLowerCase()) ||
                    student.getEmail().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(student);
            }
        }
        studentAdapter.setStudentList(filteredList); // Cập nhật danh sách sinh viên trong adapter
    }

    // Các phương thức sắp xếp
    private void sortStudentListByName() {
        if (isAscending) {
            Collections.sort(studentList, (s1, s2) -> s1.getFullName().compareTo(s2.getFullName()));
        } else {
            Collections.sort(studentList, (s1, s2) -> s2.getFullName().compareTo(s1.getFullName()));
        }
        studentAdapter.notifyDataSetChanged();
        isAscending = !isAscending; // Đổi trạng thái sắp xếp
    }

    private void sortStudentListByDob() {
        if (isAscending) {
            Collections.sort(studentList, (s1, s2) -> s1.getDateOfBirth().compareTo(s2.getDateOfBirth()));
        } else {
            Collections.sort(studentList, (s1, s2) -> s2.getDateOfBirth().compareTo(s1.getDateOfBirth()));
        }
        studentAdapter.notifyDataSetChanged();
        isAscending = !isAscending; // Đổi trạng thái sắp xếp
    }

    private void sortStudentListByStudentId() {
        if (isAscending) {
            Collections.sort(studentList, (s1, s2) -> s1.getStudentID().compareTo(s2.getStudentID()));
        } else {
            Collections.sort(studentList, (s1, s2) -> s2.getStudentID().compareTo(s1.getStudentID()));
        }
        studentAdapter.notifyDataSetChanged();
        isAscending = !isAscending; // Đổi trạng thái sắp xếp
    }

    private void sortStudentListByClass() {
        if (isAscending) {
            Collections.sort(studentList, (s1, s2) -> s1.getCurrentClass().compareTo(s2.getCurrentClass()));
        } else {
            Collections.sort(studentList, (s1, s2) -> s2.getCurrentClass().compareTo(s1.getCurrentClass()));
        }
        studentAdapter.notifyDataSetChanged();
        isAscending = !isAscending; // Đổi trạng thái sắp xếp
    }

    private void sortStudentListByGpa() {
        if (isAscending) {
            Collections.sort(studentList, (s1, s2) -> Double.compare(s1.getGpa(), s2.getGpa()));
        } else {
            Collections.sort(studentList, (s1, s2) -> Double.compare(s2.getGpa(), s1.getGpa())); // Sắp xếp từ cao đến thấp
        }
        studentAdapter.notifyDataSetChanged();
        isAscending = !isAscending; // Đổi trạng thái sắp xếp
    }
}
