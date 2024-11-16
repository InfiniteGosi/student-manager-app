package com.example.studentmanagement.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.studentmanagement.R;
import com.example.studentmanagement.data.Certificate;
import com.example.studentmanagement.data.Student;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddCertificate extends AppCompatActivity {
    private EditText etCertificateName, etIssuer, etIssueDate;
    TextView addFromFile;
    private AppCompatButton btnSave;
    private static final int PICK_CSV_FILE = 1;

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        readFileAndUpload(uri, ",");
                    }
                } else {
                    Toast.makeText(this, "Tải file thất bại", Toast.LENGTH_SHORT).show();
                }
            }
    );

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
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("text/*"); // Cho phép tất cả các loại file văn bản
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{"text/csv", "application/vnd.ms-excel", "text/*"});
        activityResultLauncher.launch(intent);
    }
    private void readFileAndUpload(Uri uri, String delimiter){
        if(uri == null){
            Toast.makeText(this, "URI không hợp lệ hoặc tệp không được chọn.",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        List<Certificate> certificatesList = new ArrayList<>();

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(delimiter);

                if (fields.length == 10) {
                    // Create a new student object
                    Certificate certificate = new Certificate(
                            fields[0].trim(),
                            fields[1].trim(),
                            fields[2].trim(),
                            fields[3].trim()
                    );

                    // Add the student to the list
                    certificatesList.add(certificate);
                } else {
                    Log.e("File Error", "Dữ liệu không đúng định dạng.");
                }
            }
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("certificatesList", new ArrayList<>(certificatesList));  // Sending the list as an extra
            setResult(RESULT_OK, intent);

            Toast.makeText(this, "Thêm sinh viên thành công", Toast.LENGTH_SHORT).show();

            // Kết thúc AddStudentActivity và quay về
            new Handler().postDelayed(() -> {
                finish(); // Kết thúc Activity sau thời gian chờ
            }, 2000);

            Toast.makeText(this, "Thêm sinh viên từ file thành công!", Toast.LENGTH_SHORT).show();
        } catch (Exception e){
            Log.e("File Error", "Lỗi khi đọc file", e);
            Toast.makeText(this, "Lỗi khi đọc file", Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isValidDate(String date) {
        String datePattern = "\\d{2}/\\d{2}/\\d{4}"; // Định dạng dd/MM/yyyy
        return date.matches(datePattern);
    }
}