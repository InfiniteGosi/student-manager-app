package com.example.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanagement.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class StudentDetail extends AppCompatActivity {
    private FirebaseFirestore db;
    ImageView imageView_profile_dp;
    TextView textView_show_welcome, textView_show_id_student, textView_show_full_name,
            textView_show_national, textView_show_email, textView_show_dob,
            textView_show_current_class, textView_show_gpa, textView_show_student_phone,
            textView_show_parent_name, textView_show_parent_phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        imageView_profile_dp = findViewById(R.id.imageView_profile_dp);
        textView_show_welcome = findViewById(R.id.textView_show_welcome);
        textView_show_id_student = findViewById(R.id.textView_show_id_student);
        textView_show_full_name = findViewById(R.id.textView_show_full_name);
        textView_show_national = findViewById(R.id.textView_show_national);
        textView_show_email = findViewById(R.id.textView_show_email);
        textView_show_dob = findViewById(R.id.textView_show_dob);
        textView_show_current_class = findViewById(R.id.textView_show_current_class);
        textView_show_gpa = findViewById(R.id.textView_show_gpa);
        textView_show_student_phone = findViewById(R.id.textView_show_student_phone);
        textView_show_parent_name = findViewById(R.id.textView_show_parent_name);
        textView_show_parent_phone = findViewById(R.id.textView_show_parent_phone);

        String studentId = getIntent().getStringExtra("STUDENT_ID");
        if(studentId != null){
            getStudentDetail(studentId);
        }
    }
    private void getStudentDetail(String studentId){
        db = FirebaseFirestore.getInstance();

        db.collection("students")
                .document(studentId)
                .get()
                .addOnCompleteListener(task -> {
                   if(task.isSuccessful()){
                       DocumentSnapshot document = task.getResult();
                       if(document != null && document.exists()){
                           String fullName = document.getString("fullName");
                           String dateOfBirth = document.getString("dateOfBirth");
                           String nationality = document.getString("nationality");
                           String phoneNumber = document.getString("phoneNumber");
                           String email = document.getString("email");
                           String currentClass = document.getString("currentClass");
                           float gpa = document.getDouble("gpa").floatValue();
                           String guardianName = document.getString("guardianName");
                           String guardianPhone = document.getString("guardianPhone");

                           textView_show_welcome.setText("Welcome " + fullName);
                           textView_show_id_student.setText(studentId);
                           textView_show_full_name.setText(fullName);
                           textView_show_national.setText(nationality);
                           textView_show_email.setText(email);
                           textView_show_dob.setText(dateOfBirth);
                           textView_show_current_class.setText(currentClass);
                           textView_show_student_phone.setText(phoneNumber);
                           textView_show_gpa.setText(String.valueOf(gpa));
                           textView_show_parent_name.setText(guardianName);
                           textView_show_parent_phone.setText(guardianPhone);
                       } else {
                           Toast.makeText(this, "Sinh viên không tồn tại!",
                                   Toast.LENGTH_SHORT).show();
                       }
                   } else {
                       Toast.makeText(this, "Lỗi tải dữ liệu: " + task.getException().getMessage(),
                               Toast.LENGTH_SHORT).show();
                   }
                });
    }
}

