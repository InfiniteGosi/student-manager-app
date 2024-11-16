package com.example.studentmanagement.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.example.studentmanagement.R;
import com.example.studentmanagement.data.Certificate;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opencsv.CSVReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class AddCertificate extends AppCompatActivity {
    private EditText etCertificateName, etIssuer, etIssueDate;
    TextView addFromFile;
    private AppCompatButton btnSave;
    private DatabaseReference databaseReference;
    private static final int PICK_CSV_FILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_certificate);

        etCertificateName = findViewById(R.id.etCertificateName);
        etIssuer = findViewById(R.id.etIssuer);
        etIssueDate = findViewById(R.id.etIssueDate);
        addFromFile = findViewById(R.id.addFromFile);
        btnSave = findViewById(R.id.btnSave);

        addFromFile.setOnClickListener(v -> selectFile());

        btnSave.setOnClickListener(v -> {
            String name = etCertificateName.getText().toString();
            String issuer = etIssuer.getText().toString();
            String issueDate = etIssueDate.getText().toString();

            if(name.isEmpty() || issuer.isEmpty() || issueDate.isEmpty()){
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if(!isValidDate(issueDate)){
                Toast.makeText(this, "Định dạng ngày không đúng (dd/MM/yyyy).",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String certificateId = FirebaseDatabase.getInstance().getReference("certificates").push().getKey();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference studentRef = database.getReference("certificates");

            studentRef.child(certificateId).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    Toast.makeText(this, "Chứng chỉ đã tồn tại trên hệ thống",
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Save the new student
                    Certificate certificate = new Certificate(certificateId, name, issueDate, issuer);

                    studentRef.child(certificateId).setValue(certificate)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Thêm chứng chỉ thành công",
                                        Toast.LENGTH_SHORT).show();
                                new Handler().postDelayed(this::finish, 2000);
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Thêm chứng chỉ thất bại",
                                    Toast.LENGTH_SHORT).show());
                }
            });
        });
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        startActivityForResult(Intent.createChooser(intent, "Select CSV"), PICK_CSV_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CSV_FILE && resultCode == RESULT_OK && data != null) {
            Uri csvUri = data.getData();
            importCSV(csvUri);
        }
    }
    private void importCSV(Uri csvUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(csvUri);
            CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
            String[] line;

            while ((line = reader.readNext()) != null) {
                Map<String, Object> certificateData = new HashMap<>();
                certificateData.put("id", line[0]); // Cột 1: Tên chứng chỉ
                certificateData.put("name", line[1]); // Cột 2: Ngày cấp
                certificateData.put("issuer", line[2]); // Cột 3: Cấp bởi
                certificateData.put("issueDate", line[3]);

                String certificateId = FirebaseDatabase.getInstance().getReference("certificates").push().getKey();
                databaseReference.child(certificateId).child("certificates").push().setValue(certificateData)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Nhập chứng chỉ thành công", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Nhập dữ liệu thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi đọc file CSV", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidDate(String date) {
        String datePattern = "\\d{2}/\\d{2}/\\d{4}"; // Định dạng dd/MM/yyyy
        return date.matches(datePattern);
    }
}