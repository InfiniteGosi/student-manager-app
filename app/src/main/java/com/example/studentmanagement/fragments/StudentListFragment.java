package com.example.studentmanagement.fragments;

import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.adapters.StudentAdapter;
import com.example.studentmanagement.data.Student;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class StudentListFragment extends Fragment {
    private RecyclerView recyclerView;
    private StudentAdapter studentAdapter;
    private ArrayList<Student> studentList;
    private FirebaseDatabase database;
    private DatabaseReference studentRef;
    private SearchView searchView;
    private ImageView imgSort;
    private ImageView imgDown;
    private boolean isAscending = true;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_list, container, false);

        imgSort = view.findViewById(R.id.imgSort);
        searchView = view.findViewById(R.id.searchView);
        recyclerView = view.findViewById(R.id.std_list_recyclerview);
        imgDown = view.findViewById(R.id.imgDown);


        // Khởi tạo Realtime Database
        database = FirebaseDatabase.getInstance();
        studentRef = database.getReference("students");

        studentList = new ArrayList<>();
        studentAdapter = new StudentAdapter(getContext(), this, studentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(studentAdapter);

        loadStudentsFromDatabase();

        imgDown.setOnClickListener(v -> downloadStudentList());

        setupSearchView();
        setupSortMenu();

        return view;
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) refreshStudentList();
                else filterStudentList(newText);
                return true;
            }
        });
    }

    private void setupSortMenu() {
        imgSort.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), imgSort);
            MenuInflater inflater = popupMenu.getMenuInflater();
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
            popupMenu.show();
        });
    }

    public void loadStudentsFromDatabase() {
        studentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Student student = snapshot.getValue(Student.class);
                    if (student != null) {
                        studentList.add(student);
                    }
                }
                studentAdapter.notifyDataSetChanged(); // Cập nhật lại RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error loading data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void refreshStudentList() {
        loadStudentsFromDatabase();
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
        studentAdapter.setStudentList(filteredList);
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

    private void downloadStudentList() {
        // Kiểm tra nếu danh sách rỗng
        if (studentList.isEmpty()) {
            Toast.makeText(getContext(), "Không có học sinh để tải về", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ghi danh sách vào file CSV
        writeStudentsToCsv();
    }

    private void writeStudentsToCsv() {
        // Đường dẫn lưu file vào thư mục Downloads
        String fileName = "student_list.csv";
        File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadDir, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            // Ghi dòng tiêu đề
            writer.append("Student ID,Full Name,Date of Birth,Nationality,Phone Number,Email,Class,GPA,Guardian Name,Guardian Phone\n");

            // Ghi dữ liệu sinh viên
            for (Student student : studentList) {
                writer.append(student.getStudentID()).append(",")
                        .append(student.getFullName()).append(",")
                        .append(student.getDateOfBirth()).append(",")
                        .append(student.getNationality()).append(",")
                        .append(student.getPhoneNumber()).append(",")
                        .append(student.getEmail()).append(",")
                        .append(student.getCurrentClass()).append(",")
                        .append(String.valueOf(student.getGpa())).append(",")
                        .append(student.getGuardianName()).append(",")
                        .append(student.getGuardianPhone()).append("\n");
            }

            Toast.makeText(getContext(), "File đã được lưu vào: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Có lỗi khi ghi file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
