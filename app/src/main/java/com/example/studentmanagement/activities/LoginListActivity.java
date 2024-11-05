package com.example.studentmanagement.activities;


import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.adapters.LoginEntryAdapter;
import com.example.studentmanagement.models.LoginEntry;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LoginListActivity extends AppCompatActivity {

    private RecyclerView loginRecyclerView;
    private LoginEntryAdapter loginEntryAdapter;
    private ArrayList<LoginEntry> loginEntries;
    private String userEmail; // Variable to store the user's email
    TextView textUserName;
    DatabaseReference databaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_list);

        textUserName = findViewById(R.id.textUserName);

        // Get the user's email from the intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        textUserName.setText("Lịch sử đăng nhập của " + userEmail);
        loginEntries = new ArrayList<>();
        loginRecyclerView = findViewById(R.id.user_recycler_view);
        loginRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        loginEntryAdapter = new LoginEntryAdapter(loginEntries);
        loginRecyclerView.setAdapter(loginEntryAdapter);

        fetchLoginEntries();
    }

    private void fetchLoginEntries() {
        // Access the "users" node in Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Query to find the user ID by email
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    // Check if this user's email matches the provided email
                    String email = userSnapshot.child("email").getValue(String.class);
                    if (userEmail.equals(email)) {
                        // Get the loginHistory node for this user
                        DatabaseReference loginHistoryRef = userSnapshot.child("loginHistory").getRef();

                        // Retrieve login history entries
                        loginHistoryRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot loginSnapshot) {
                                loginEntries.clear();
                                for (DataSnapshot entrySnapshot : loginSnapshot.getChildren()) {
                                    LoginEntry entry = entrySnapshot.getValue(LoginEntry.class);
                                    if (entry != null) {
                                        loginEntries.add(entry);
                                    }
                                }
                                loginEntryAdapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                // Handle database error
                            }
                        });
                        break; // Stop searching once the user is found
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

}
