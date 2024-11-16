package com.example.studentmanagement.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studentmanagement.R;
import com.example.studentmanagement.data.Certificate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UpdateCertificateActivity extends AppCompatActivity {
    private DatabaseReference db = FirebaseDatabase.getInstance().getReference("certificates");
    private EditText etCertificateName, etIssuer, etIssueDate;
    private AppCompatButton btnSave;
    private Certificate certificate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_certificate);

        etCertificateName = findViewById(R.id.etCertificateName);
        etIssuer = findViewById(R.id.etIssuer);
        etIssueDate = findViewById(R.id.etIssueDate);
        btnSave = findViewById(R.id.btnSave);

        String certificateId = getIntent().getStringExtra("CERTIFICATE_ID");
        if(certificateId != null){
            getCertificateDetail(certificateId);

            btnSave.setOnClickListener(v -> {
                saveUpdateInfo(certificateId);
            });
        } else {
            Toast.makeText(this, "ID Chứng chỉ không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
    private void getCertificateDetail(String certificateId){
        db.child(certificateId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String issuer = snapshot.child("issuer").getValue(String.class);
                    String issueDate = snapshot.child("issueDate").getValue(String.class);

                    etCertificateName.setText(name);
                    etIssuer.setText(issuer);
                    etIssueDate.setText(issueDate);
                } else {
                    Toast.makeText(UpdateCertificateActivity.this, "Chứng chỉ không tồn tại!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UpdateCertificateActivity.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveUpdateInfo(String certificateId) {
        String name = etCertificateName.getText().toString();
        String issuer = etIssuer.getText().toString();
        String issueDate = etIssueDate.getText().toString();


        Map<String, Object> certificateUpdates = new HashMap<>();
        certificateUpdates.put("name", name);
        certificateUpdates.put("issuer", issuer);
        certificateUpdates.put("issueDate", issueDate);

        db.child(certificateId).updateChildren(certificateUpdates).addOnSuccessListener(aVoid -> {
            Toast.makeText(UpdateCertificateActivity.this, "Cập nhật thông tin chứng chỉ thành công!", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            Toast.makeText(UpdateCertificateActivity.this, "Lỗi cập nhật thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
        finish();
    }
}
