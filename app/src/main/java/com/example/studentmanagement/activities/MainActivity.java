package com.example.studentmanagement.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.example.studentmanagement.R;
import com.example.studentmanagement.adapters.ViewPagerAdapter;
import com.example.studentmanagement.data.Student;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private ActivityResultLauncher<Intent> addStudentLauncher;
    private boolean isFirstTime = true;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    bottomNavigationView.setSelectedItemId(R.id.item_student);
                } else if (position == 1) {

                } else if (position == 2) {

                } else if (position == 3) {
                    bottomNavigationView.setSelectedItemId(R.id.item_staff);
                } else if (position == 4) {
                    bottomNavigationView.setSelectedItemId(R.id.item_profile);
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.item_profile) {
                viewPager.setCurrentItem(4);
            }
            if (item.getItemId() == R.id.item_staff) {
                viewPager.setCurrentItem(3);
            }
            if (item.getItemId() == R.id.item_student) {
                // Chuyển đến StudentListActivity
                Intent intent = new Intent(MainActivity.this, StudentListActivity.class);
                startActivity(intent);
            }
            if (item.getItemId() == R.id.item_add) {
                showAddDialog();
            }
            return true;
        });

        // Khởi tạo launcher để nhận kết quả từ AddStudentActivity
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
                            Intent intent = new Intent(this, StudentListActivity.class);
                            startActivity(intent);
                        }
                    }
                }
        );

        if (isFirstTime) {
            isFirstTime = false; // Đánh dấu là đã vào lần đầu
            viewPager.setCurrentItem(4); // Mở trang mặc định là StudentList
            bottomNavigationView.setSelectedItemId(R.id.item_profile); // Chọn mục Student
        }
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bạn muốn thêm");
        String[] options = {"Sinh Viên", "Nhân Viên"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                Intent intent = new Intent(this, AddStudentActivity.class);
                addStudentLauncher.launch(intent);
            } else {
                Intent intent = new Intent(this, AddStaffActivity.class);
                startActivity(intent);
            }
        });
        builder.show();
    }
}