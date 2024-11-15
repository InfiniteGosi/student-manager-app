package com.example.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanagement.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class StudentDetail extends AppCompatActivity {
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("students");
    ImageView imageView_profile_dp;
    TextView textView_show_welcome, textView_show_id_student, textView_show_full_name,
            textView_show_national, textView_show_email, textView_show_dob,
            textView_show_current_class, textView_show_gpa, textView_show_student_phone,
            textView_show_parent_name, textView_show_parent_phone;
    EditText editName, editNational, editEmail, editDob, editClass, editGpa, editStPhone,
            editParentName, editParentPhone;
    Button saveInfoStudent;

    RelativeLayout relativeLayout;

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
        saveInfoStudent = findViewById(R.id.saveInfoStudent);
        relativeLayout = findViewById(R.id.relativeLayout);

        boolean checkEditMode = getIntent().getBooleanExtra("isEditMode", false);
        if (checkEditMode) {
            saveInfoStudent.setVisibility(View.VISIBLE);
        } else {
            saveInfoStudent.setVisibility(View.GONE);
        }

        String studentId = getIntent().getStringExtra("STUDENT_ID");
        if (studentId != null) {
            getStudentDetail(studentId, checkEditMode);

            saveInfoStudent.setOnClickListener(v -> {
                saveUpdateInfo(studentId);
                Toast.makeText(this, studentId, Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(this, "ID sinh viên không hợp lệ",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void getStudentDetail(String studentId, boolean isEditMode) {
        db.child(studentId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String dateOfBirth = snapshot.child("dateOfBirth").getValue(String.class);
                    String nationality = snapshot.child("nationality").getValue(String.class);
                    String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String currentClass = snapshot.child("currentClass").getValue(String.class);
                    float gpa = snapshot.child("gpa").getValue(Float.class);
                    String guardianName = snapshot.child("guardianName").getValue(String.class);
                    String guardianPhone = snapshot.child("guardianPhone").getValue(String.class);

                    textView_show_welcome.setText(fullName);
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

                    if (isEditMode) {
                        edtToTextView(textView_show_full_name, fullName);
                        edtToTextView(textView_show_national, nationality);
                        edtToTextView(textView_show_email, email);
                        edtToTextView(textView_show_dob, dateOfBirth);
                        edtToTextView(textView_show_current_class, currentClass);
                        edtToTextView(textView_show_student_phone, phoneNumber);
                        edtToTextView(textView_show_parent_name, guardianName);
                        edtToTextView(textView_show_parent_phone, guardianPhone);
                    }
                } else {
                    Toast.makeText(StudentDetail.this, "Sinh viên không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(StudentDetail.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUpdateInfo(String studentId) {
        String fullName = editName.getText().toString();
        String nationality = editNational.getText().toString();
        String email = editEmail.getText().toString();
        String dateOfBirth = editDob.getText().toString();
        String currentClass = editClass.getText().toString();
        String phoneNumber = editStPhone.getText().toString();
        String guardianName = editParentName.getText().toString();
        String guardianPhone = editParentPhone.getText().toString();

        Map<String, Object> studentUpdates = new HashMap<>();
        studentUpdates.put("fullName", fullName);
        studentUpdates.put("email", email);
        studentUpdates.put("dateOfBirth", dateOfBirth);
        studentUpdates.put("nationality", nationality);
        studentUpdates.put("phoneNumber", phoneNumber);
        studentUpdates.put("currentClass", currentClass);
        studentUpdates.put("guardianName", guardianName);
        studentUpdates.put("guardianPhone", guardianPhone);

        db.child(studentId).updateChildren(studentUpdates).addOnSuccessListener(aVoid -> {
            Toast.makeText(StudentDetail.this, "Cập nhật thông tin sinh viên thành công!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(StudentDetail.this, "Lỗi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
        finish();
    }

    private void edtToTextView(TextView info, String data) {
        EditText nameField = new EditText(this);
        nameField.setId(info.getId());
        nameField.setText(data);
        nameField.setLayoutParams(info.getLayoutParams());

        nameField.setInputType(InputType.TYPE_CLASS_TEXT);

        ViewParent parent = info.getParent();
        if (parent instanceof RelativeLayout) {
            RelativeLayout parentLayout = (RelativeLayout) parent;

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) info.getLayoutParams();
            nameField.setLayoutParams(layoutParams);

            parentLayout.removeView(info);
            parentLayout.addView(nameField);

            if (info == textView_show_full_name) {
                editName = nameField;
            } else if (info == textView_show_national) {
                editNational = nameField;
            } else if (info == textView_show_email) {
                editEmail = nameField;
            } else if (info == textView_show_dob) {
                editDob = nameField;
            } else if (info == textView_show_current_class) {
                editClass = nameField;
            } else if (info == textView_show_student_phone) {
                editStPhone = nameField;
            } else if (info == textView_show_parent_name) {
                editParentName = nameField;
            } else if (info == textView_show_parent_phone) {
                editParentPhone = nameField;
            }
        } else {
            Toast.makeText(this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }
}
