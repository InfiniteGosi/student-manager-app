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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;

import com.example.studentmanagement.fragments.StudentListFragment;
import com.example.studentmanagement.activities.StudentDetail;
import com.example.studentmanagement.data.Student;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.ViewHolder> {

    private ArrayList<Student> studentList;
    private FirebaseFirestore db;
    private WeakReference<Fragment> fragmentRef;
    private WeakReference<Context> context;

    public StudentAdapter(Context context, Fragment fragment, ArrayList<Student> studentList) {
        this.context = new WeakReference<>(context); // Use WeakReference to avoid memory leaks
        this.fragmentRef = new WeakReference<>(fragment);
        this.studentList = studentList;
        this.db = FirebaseFirestore.getInstance();
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

        // Thiết lập sự kiện nhấp vào hình ảnh xóa
        holder.imgDelete.setOnClickListener(v -> {
            // Gọi phương thức xóa sinh viên
            deleteStudent(student.getStudentID(), holder.getAdapterPosition());
        });

        // Thêm sự kiện nhấp vào hình ảnh sửa nếu cần
        holder.imgPen.setOnClickListener(v -> {
            // Xử lý sửa sinh viên nếu cần
            Context conText = context.get();
            if(conText != null){
                Intent intent = new Intent(conText, StudentDetail.class);
                intent.putExtra("STUDENT_ID", student.getStudentID());
                intent.putExtra("isEditMode", true);
                conText.startActivity(intent);
            }
        });

        // Sự kiện click xem thông tin sinh viên
        holder.linearLayout.setOnClickListener(v -> {
            Context conText = context.get();
            if(conText != null){
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
                        db.collection("students")
                                .document(studentID)
                                .delete();
                        showToast("Đã xóa sinh viên");
                        if (fragment instanceof StudentListFragment) {
                            ((StudentListFragment) fragment).refreshStudentList();
                        }
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
