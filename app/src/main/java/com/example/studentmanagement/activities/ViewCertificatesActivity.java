package com.example.studentmanagement.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.studentmanagement.R;
import com.example.studentmanagement.adapters.CertificateAdapter;
import com.example.studentmanagement.data.Certificate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ViewCertificatesActivity extends AppCompatActivity {
    private List<Certificate> certificateList;
    private CertificateAdapter adapter;
    private RecyclerView recyclerViewCertificates;
    ImageView imgDown;
    private DatabaseReference databaseReference;
    private ValueEventListener certificateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_certificate);

        imgDown = findViewById(R.id.imgDown);

        // Khởi tạo RecyclerView
        recyclerViewCertificates = findViewById(R.id.recyclerViewCertificates);
        recyclerViewCertificates.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo danh sách và adapter
        certificateList = new ArrayList<>();
        adapter = new CertificateAdapter(certificateList, this);
        recyclerViewCertificates.setAdapter(adapter);

        // Tải dữ liệu từ Firebase
        loadCertificateFromDatabase();

        databaseReference = FirebaseDatabase.getInstance().getReference("certificates");
        imgDown.setOnClickListener(v -> exportToCSV());
    }

    private void loadCertificateFromDatabase() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("certificates");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                certificateList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Certificate certificate = data.getValue(Certificate.class);
                    if (certificate != null) {
                        certificateList.add(certificate);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewCertificatesActivity.this, "Tải thông tin thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Gỡ bỏ Listener khi Activity bị hủy để tránh rò rỉ bộ nhớ
        if (databaseReference != null && certificateListener != null) {
            databaseReference.removeEventListener(certificateListener);
        }
    }

    private void exportToCSV() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder csvData = new StringBuilder();
                csvData.append("Name,Issue Date,Issuer\n"); // Header

                for (DataSnapshot certificateSnapshot : dataSnapshot.getChildren()) {
                    String name = certificateSnapshot.child("name").getValue(String.class);
                    String issueDate = certificateSnapshot.child("issueDate").getValue(String.class);
                    String issuer = certificateSnapshot.child("issuer").getValue(String.class);

                    csvData.append(name).append(",")
                            .append(issueDate).append(",")
                            .append(issuer).append("\n");
                }

                writeCSVFile(csvData.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ViewCertificatesActivity.this,
                        "Lỗi khi tải dữ liệu: " + databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void writeCSVFile(String data) {
        try {
            File path = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (path == null) {
                Toast.makeText(this, "Không thể truy cập thư mục", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(path, "student_certificates.csv");
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            osw.write(data);
            osw.flush();
            osw.close();

            Toast.makeText(this, "Đã xuất dữ liệu: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi khi xuất dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

}
