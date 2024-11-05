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
import com.example.studentmanagement.activities.LoginListActivity;
import com.example.studentmanagement.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private ArrayList<User> userList;
    private OnEditUserListener editUserListener;

    public interface OnEditUserListener {
        void onEditUser(User user);
    }

    public UserAdapter(Context context, ArrayList<User> userList, OnEditUserListener listener) {
        this.userList = userList;
        this.editUserListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);

        if (user != null) {
            holder.textName.setText("Họ và tên: " + user.getName());
            holder.textPhone.setText("Điện thoại: " + user.getPhone());
            holder.textEmail.setText("Email: " + user.getEmail());
            holder.textRole.setText("Quyền: " + user.getRole());

            if (user.getLocked()) {
                holder.imageStatus.setVisibility(View.VISIBLE);
            }
            else {
                holder.imageStatus.setVisibility(View.INVISIBLE);
            }

            holder.imagePen.setOnClickListener(v -> {
                // Sự kiện sửa user
                if (editUserListener != null) {
                    editUserListener.onEditUser(user);
                }
            });

            holder.imageDelete.setOnClickListener(v -> {
                // Create a confirmation dialog
                new AlertDialog.Builder(holder.itemView.getContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete this user?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // Proceed with deletion if confirmed
                            DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");

                            // Query the "users" node for the specific email
                            databaseRef.orderByChild("email").equalTo(user.getEmail())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                                // Delete each matching user node
                                                userSnapshot.getRef().removeValue()
                                                        .addOnSuccessListener(aVoid ->
                                                                Toast.makeText(holder.itemView.getContext(),
                                                                        "User deleted successfully", Toast.LENGTH_SHORT).show())
                                                        .addOnFailureListener(e ->
                                                                Toast.makeText(holder.itemView.getContext(),
                                                                        "Failed to delete user", Toast.LENGTH_SHORT).show());
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(holder.itemView.getContext(), "Error deleting user",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        })
                        .setNegativeButton("No", (dialog, which) -> dialog.dismiss()) // Close the dialog if the user cancels
                        .show();
            });

            // Inside UserAdapter.java, in onBindViewHolder method:
            holder.imageHistory.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), LoginListActivity.class);
                intent.putExtra("USER_EMAIL", user.getEmail()); // Pass user email to LoginListActivity
                v.getContext().startActivity(intent);
            });


        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textPhone, textEmail, textRole;
        ImageView imageStatus, imagePen, imageDelete, imageHistory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.tvUsername);
            textPhone = itemView.findViewById(R.id.tvUserPhone);
            textEmail = itemView.findViewById(R.id.tvUserEmail);
            textRole = itemView.findViewById(R.id.tvUserRole);
            imageStatus = itemView.findViewById(R.id.imgStatus);
            imagePen = itemView.findViewById(R.id.imgPenUser);
            imageDelete = itemView.findViewById(R.id.imgDeleteUser);
            imageHistory = itemView.findViewById(R.id.imgHistory);
        }
    }
}
