package com.example.studentmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.models.LoginEntry;

import java.util.ArrayList;

public class LoginEntryAdapter extends RecyclerView.Adapter<LoginEntryAdapter.ViewHolder> {

    private ArrayList<LoginEntry> loginEntries;

    public LoginEntryAdapter(ArrayList<LoginEntry> loginEntries) {
        this.loginEntries = loginEntries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_login_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoginEntry loginEntry = loginEntries.get(position);
        holder.timestampTextView.setText(loginEntry.getTimestamp());
    }

    @Override
    public int getItemCount() {
        return loginEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timestampTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timestampTextView = itemView.findViewById(R.id.timestamp_text_view);
        }
    }
}
