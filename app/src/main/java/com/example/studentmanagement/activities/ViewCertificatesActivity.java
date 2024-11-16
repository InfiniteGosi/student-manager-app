package com.example.studentmanagement.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.example.studentmanagement.R;
import com.example.studentmanagement.adapters.CertificateAdapter;
import com.example.studentmanagement.data.Certificate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ViewCertificatesActivity extends AppCompatActivity {
    private List<Certificate> certificateList;
    private CertificateAdapter adapter;
    private RecyclerView recyclerViewCertificates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_certificate);

        recyclerViewCertificates = findViewById(R.id.recyclerViewCertificates);
        recyclerViewCertificates.setLayoutManager(new LinearLayoutManager(this));
        certificateList = new ArrayList<>();
        adapter = new CertificateAdapter(certificateList, this);
        recyclerViewCertificates.setAdapter(adapter);

        loadCertificateFromDatabase();
    }
    private void loadCertificateFromDatabase(){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("certificates");
        ref.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                certificateList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Certificate certificate = data.getValue(Certificate.class);
                    certificateList.add(certificate);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ViewCertificatesActivity.this, "Failed to load data",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
