package com.example.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class StudentDetail extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        if(studentId != null){
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
    private void getStudentDetail(String studentId, boolean isEditMode){

        Log.d("StudentDetail", "Firestore instance retrieved.");
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

                           // Gọi edtToTextView sau khi dữ liệu đã được gán vào các TextView
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
                           Toast.makeText(this, "Sinh viên không tồn tại!",
                                   Toast.LENGTH_SHORT).show();
                       }
                   } else {
                       Toast.makeText(this, "Lỗi tải dữ liệu: " + task.getException().getMessage(),
                               Toast.LENGTH_SHORT).show();
                   }
                });
    }
    private void saveUpdateInfo(String studentId){
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

        DocumentReference studentRef = db.collection("students").document(studentId);
        studentRef.update(studentUpdates).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Cập nhật thông tin sinh viên thành công!",
                    Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            if (e instanceof FirebaseFirestoreException &&
                    ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                Toast.makeText(this, "Không có kết nối Internet. Vui lòng kiểm tra lại!",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi cập nhật thông tin: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        finish();
    }
    private void edtToTextView(TextView info, String data){
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
            Toast.makeText(this, "Lỗi khi tải dữ liệu",
                    Toast.LENGTH_SHORT).show();
        }
    }
}

