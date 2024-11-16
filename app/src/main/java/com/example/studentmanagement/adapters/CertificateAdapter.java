package com.example.studentmanagement.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.activities.UpdateCertificateActivity;
import com.example.studentmanagement.data.Certificate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {
    private List<Certificate> certificateList;
    private Context context;

    public CertificateAdapter(List<Certificate> certificateList, Context context) {
        this.certificateList = certificateList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_certificate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Certificate certificate = certificateList.get(position);

        holder.tvCertificateName.setText(certificate.getName());
        holder.tvIssuer.setText("Issuer: " + certificate.getIssuer());
        holder.tvIssueDate.setText("Date: " + certificate.getIssueDate());

        holder.imgDelete.setOnClickListener(v -> {
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("students")
                    .child("studentId")
                    .child("certificates")
                    .child(certificate.getId());

            ref.removeValue()
                    .addOnSuccessListener(aVoid -> Toast.makeText(context, "Deleted!", Toast.LENGTH_SHORT).show());
        });

        holder.imgPen.setOnClickListener(v -> {
            if (context != null) {
                Intent intent = new Intent(context, UpdateCertificateActivity.class);
                intent.putExtra("CERTIFICATE_ID", certificate.getId());
                context.startActivity(intent);
//                loadStudentsFromRealtimeDatabase();
            }
        });
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCertificateName, tvIssuer, tvIssueDate;
        ImageView imgDelete, imgPen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCertificateName = itemView.findViewById(R.id.tvCertificateName);
            tvIssuer = itemView.findViewById(R.id.tvIssuer);
            tvIssueDate = itemView.findViewById(R.id.tvIssueDate);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            imgPen = itemView.findViewById(R.id.imgPen);
        }
    }
}

