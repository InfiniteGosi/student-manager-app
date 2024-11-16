package com.example.studentmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.studentmanagement.R;
import com.example.studentmanagement.adapters.ViewPagerAdapter;
import com.example.studentmanagement.data.Student;
import com.example.studentmanagement.fragments.StudentListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class MainActivity extends FragmentActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<Intent> addStudentLauncher;
    private DatabaseReference studentRef;  // Thay đổi từ Firestore sang Realtime Database
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo đối tượng Firebase Realtime Database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        studentRef = database.getReference("students");  // Thay đổi từ Firestore collection sang Realtime Database reference

        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Khởi tạo launcher để nhận kết quả từ AddStudentActivity
        addStudentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            // Kiểm tra xem dữ liệu có phải là danh sách sinh viên
                            if (data.hasExtra("studentsList")) {
                                List<Student> studentsList = (List<Student>) data.getSerializableExtra("studentsList");
                                if (studentsList != null && !studentsList.isEmpty()) {
                                    // Gửi danh sách sinh viên đến Realtime Database
                                    for (Student student : studentsList) {
                                        studentRef.child(student.getStudentID()).setValue(student)  // Sử dụng Realtime Database để lưu
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(MainActivity.this, "Sinh viên đã được thêm vào hệ thống.", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Toast.makeText(MainActivity.this, "Lỗi khi thêm sinh viên", Toast.LENGTH_SHORT).show();
                                                });
                                    }

                                    // Làm mới danh sách sinh viên trong StudentListFragment
                                    StudentListFragment fragment = (StudentListFragment) getSupportFragmentManager()
                                            .findFragmentByTag("f" + 1); // "f" + vị trí của Fragment trong ViewPager
                                    if (fragment != null) {
                                        fragment.loadStudentsFromDatabase(); // Làm mới danh sách từ Realtime Database
                                    }
                                }
                            } else if (data.hasExtra("newStudent")) {
                                // Chỉ thêm một sinh viên nếu chỉ có một sinh viên mới
                                Student newStudent = (Student) data.getSerializableExtra("newStudent");
                                if (newStudent != null) {
                                    // Lưu sinh viên mới vào Realtime Database
                                    studentRef.child(newStudent.getStudentID()).setValue(newStudent);

                                    // Cập nhật danh sách sinh viên trong StudentListFragment
                                    Toast.makeText(this, "Danh sách đã cập nhật", Toast.LENGTH_SHORT).show();

                                    // Làm mới danh sách sinh viên
                                    StudentListFragment fragment = (StudentListFragment) getSupportFragmentManager()
                                            .findFragmentByTag("f" + 1); // "f" + vị trí của Fragment trong ViewPager
                                    if (fragment != null) {
                                        fragment.loadStudentsFromDatabase(); // Làm mới danh sách từ Realtime Database
                                    }
                                }
                            }
                        }
                    }
                }
        );

        checkUserRole();
    }

    private void checkUserRole() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            userRef.get().addOnSuccessListener(dataSnapshot -> {
                if (dataSnapshot.exists()) {
                    String role = dataSnapshot.child("role").getValue(String.class);
                    if ("Employee".equals(role)) {
                        bottomNavigationView.getMenu().findItem(R.id.item_add).setVisible(false);
                        bottomNavigationView.getMenu().findItem(R.id.item_staff).setVisible(false);

                    }
                    if ("Manager".equals(role)) {
                        bottomNavigationView.getMenu().findItem(R.id.item_staff).setVisible(false);
                    }
                    setupViewPager(role);
                    setupBottomNavigation(role);
                }
            });
        }
    }

    private void setupViewPager(String role) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle(), role);
        viewPager.setAdapter(adapter);

        // Đăng ký sự kiện khi trang được thay đổi trong ViewPager
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if ("Employee".equals(userRole)) {
                    if (position == 0) {
                        bottomNavigationView.setSelectedItemId(R.id.item_home);
                    } else if (position == 1) {
                        bottomNavigationView.setSelectedItemId(R.id.item_student);
                    } else if (position == 2) {
                        bottomNavigationView.setSelectedItemId(R.id.item_profile);
                    }
                } else if ("Manager".equals(userRole)) {
                    if (position == 0) {
                        bottomNavigationView.setSelectedItemId(R.id.item_home);
                    } else if (position == 1) {
                        bottomNavigationView.setSelectedItemId(R.id.item_student);
                    } else if (position == 2) {

                    } else if (position == 3) {
                        bottomNavigationView.setSelectedItemId(R.id.item_profile);
                    }

                } else {
                    if (position == 0) {
                        bottomNavigationView.setSelectedItemId(R.id.item_home);
                    } else if (position == 1) {
                        bottomNavigationView.setSelectedItemId(R.id.item_student);
                    } else if (position == 2) {

                    } else if (position == 3) {
                        bottomNavigationView.setSelectedItemId(R.id.item_staff);
                    } else if (position == 4) {
                        bottomNavigationView.setSelectedItemId(R.id.item_profile);
                    }
                }
            }
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bạn muốn thêm");
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userRef.get().addOnSuccessListener(dataSnapshot -> {
            if (dataSnapshot.exists()) {
                String role = dataSnapshot.child("role").getValue(String.class);
                if ("Manager".equals(role)) {
                    String[] options = {"Sinh Viên"};
                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Chọn Sinh Viên -> Mở AddStudentActivity
                            Intent intent = new Intent(this, AddStudentActivity.class);
                            addStudentLauncher.launch(intent);
                        }
                    });
                    builder.show();
                }
                else{
                    String[] options = {"Sinh Viên", "Nhân Viên"};
                    builder.setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            // Chọn Sinh Viên -> Mở AddStudentActivity
                            Intent intent = new Intent(this, AddStudentActivity.class);
                            addStudentLauncher.launch(intent);
                        } else {
                            // Chọn Nhân Viên -> Mở AddStaffActivity
                            Intent intent = new Intent(this, AddUserActivity.class);
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }
            }
        });

    }

    private void setupBottomNavigation(String role) {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            if ("Employee".equals(role)){
                if (item.getItemId() == R.id.item_home) {
                    viewPager.setCurrentItem(0);
                }
                if (item.getItemId() == R.id.item_student) {
                    viewPager.setCurrentItem(1);
                }
                if (item.getItemId() == R.id.item_profile) {
                    viewPager.setCurrentItem(2);
                }
            } else if ("Manager".equals(role)){
                if (item.getItemId() == R.id.item_home) {
                    viewPager.setCurrentItem(0);
                }
                if (item.getItemId() == R.id.item_student) {
                    viewPager.setCurrentItem(1);
                }
                if (item.getItemId() == R.id.item_add) {
                    showAddDialog();
                }
                if (item.getItemId() == R.id.item_profile) {
                    viewPager.setCurrentItem(3);
                }
            } else {
                if (item.getItemId() == R.id.item_home) {
                    viewPager.setCurrentItem(0);
                }
                if (item.getItemId() == R.id.item_student) {
                    viewPager.setCurrentItem(1);
                }
                if (item.getItemId() == R.id.item_add) {
                    showAddDialog();
                }
                if (item.getItemId() == R.id.item_staff) {
                    viewPager.setCurrentItem(3);
                }
                if (item.getItemId() == R.id.item_profile) {
                    viewPager.setCurrentItem(4);
                }
            }
            return true;
        });
    }
}
