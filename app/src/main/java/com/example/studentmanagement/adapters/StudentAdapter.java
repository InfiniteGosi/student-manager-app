package com.example.studentmanagement.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.activities.StudentDetail;
import com.example.studentmanagement.data.Student;
import com.example.studentmanagement.fragments.StudentListFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private ArrayList<Student> studentList;
    private DatabaseReference databaseReference;
    private WeakReference<Fragment> fragmentRef;
    private WeakReference<Context> context;

    public StudentAdapter(Context context, Fragment fragment, ArrayList<Student> studentList) {
        this.context = new WeakReference<>(context); // Use WeakReference to avoid memory leaks
        this.fragmentRef = new WeakReference<>(fragment);
        this.studentList = studentList;
        this.databaseReference = FirebaseDatabase.getInstance().getReference("students");
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_student, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Student student = studentList.get(position);
        holder.tvFullName.setText("Họ và tên: " + student.getFullName());
        holder.tvStudentID.setText("Mã học sinh: " + student.getStudentID());
        holder.tvClass.setText("Lớp: " + student.getCurrentClass());
        holder.tvEmail.setText("Email: " + student.getEmail());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Truy vấn Firebase Realtime Database để lấy vai trò người dùng
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users").child(currentUser.getUid());
            usersRef.child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String role = snapshot.getValue(String.class);
                    if ("Employee".equals(role)) {
                        // Nếu vai trò là "employee", trả về true
                        holder.imgDelete.setVisibility(View.GONE);
                        holder.imgPen.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi nếu có
                }
            });
        }

        // Thiết lập sự kiện nhấp vào hình ảnh xóa
        holder.imgDelete.setOnClickListener(v -> deleteStudent(student.getStudentID(), holder.getAdapterPosition()));

        // Thêm sự kiện nhấp vào hình ảnh sửa
        holder.imgPen.setOnClickListener(v -> {
            Context conText = context.get();
            if (conText != null) {
                Intent intent = new Intent(conText, StudentDetail.class);
                intent.putExtra("STUDENT_ID", student.getStudentID());
                intent.putExtra("isEditMode", true);
                conText.startActivity(intent);
                loadStudentsFromRealtimeDatabase();
            }
        });

        // Sự kiện click xem thông tin sinh viên
        holder.linearLayout.setOnClickListener(v -> {
            Context conText = context.get();
            if (conText != null) {
                Intent intent = new Intent(conText, StudentDetail.class);
                intent.putExtra("STUDENT_ID", student.getStudentID());
                intent.putExtra("isEditMode", false);
                conText.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList.size(); // Trả về số lượng sinh viên trong danh sách
    }

    // Phương thức để xóa sinh viên
    private void deleteStudent(String studentID, int position) {
        if (studentID == null || studentID.isEmpty()) {
            showToast("ID sinh viên không hợp lệ");
            return;
        }

        Fragment fragment = fragmentRef.get();
        if (fragment != null && fragment.getActivity() != null) {
            new AlertDialog.Builder(fragment.getActivity())
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có chắc chắn muốn xóa sinh viên này?")
                    .setPositiveButton("Có", (dialog, which) -> {
                        databaseReference.child(studentID).removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    showToast("Đã xóa sinh viên");
                                    if (fragment instanceof StudentListFragment) {
                                        ((StudentListFragment) fragment).refreshStudentList();
                                    }
                                })
                                .addOnFailureListener(e -> showToast("Lỗi khi xóa sinh viên"));
                    })
                    .setNegativeButton("Không", null)
                    .show();
        }
    }

    // Phương thức để cập nhật danh sách sinh viên
    public void setStudentList(ArrayList<Student> newStudentList) {
        studentList.clear(); // Xóa danh sách cũ
        studentList.addAll(newStudentList); // Thêm danh sách mới
        notifyDataSetChanged(); // Cập nhật RecyclerView
    }

    private void showToast(String message) {
        Fragment fragment = fragmentRef.get();
        if (fragment != null && fragment.getContext() != null) {
            Toast.makeText(fragment.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Phương thức để tải danh sách sinh viên từ Realtime Database
    public void loadStudentsFromRealtimeDatabase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<Student> students = new ArrayList<>();
                for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                    Student student = studentSnapshot.getValue(Student.class);
                    if (student != null) {
                        students.add(student);
                    }
                }
                setStudentList(students); // Cập nhật danh sách vào Adapter
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showToast("Lỗi khi tải danh sách sinh viên");
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentID, tvFullName, tvClass, tvEmail;
        ImageView imgDelete, imgPen;
        LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentID = itemView.findViewById(R.id.tvStudentID);
            tvFullName = itemView.findViewById(R.id.tvFullName);
            tvClass = itemView.findViewById(R.id.tvClass);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgPen = itemView.findViewById(R.id.imgPen);
            linearLayout = itemView.findViewById(R.id.linearLayout);
        }
    }
}
