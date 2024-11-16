package com.example.studentmanagement.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.activities.UpdateCertificateActivity;
import com.example.studentmanagement.data.Certificate;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.ViewHolder> {
    private final List<Certificate> certificateList;
    private final Context context;

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

        if (certificate != null) {
            holder.tvCertificateName.setText(certificate.getName());
            holder.tvIssuer.setText("Issuer: " + certificate.getIssuer());
            holder.tvIssueDate.setText("Date: " + certificate.getIssueDate());
        }

        // Xử lý nút xóa
        holder.imgDelete.setOnClickListener(v -> {
            String certificateId = certificate.getId();
            if (certificateId != null) {
                showDeleteConfirmationDialog(certificateId, position);
            } else {
                Toast.makeText(context, "ID chứng chỉ không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        // Xử lý nút sửa
        holder.imgPen.setOnClickListener(v -> {
            if (context != null) {
                Intent intent = new Intent(context, UpdateCertificateActivity.class);
                intent.putExtra("CERTIFICATE_ID", certificate.getId());
                context.startActivity(intent);
            }
        });
    }

    private void showDeleteConfirmationDialog(String certificateId, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc chắn muốn xóa chứng chỉ này?")
                .setPositiveButton("Có", (dialog, which) -> deleteCertificate(certificateId, position))
                .setNegativeButton("Không", null)
                .show();
    }

    private void deleteCertificate(String certificateId, int position) {
        if (certificateId == null || certificateId.isEmpty() || position < 0 || position >= certificateList.size()) {
            Toast.makeText(context, "ID hoặc vị trí chứng chỉ không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("certificates");
        databaseReference.child(certificateId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    // Chỉ cập nhật danh sách sau khi Firebase báo thành công
                    certificateList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, certificateList.size());
                    Toast.makeText(context, "Đã xóa chứng chỉ", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa chứng chỉ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
