package com.example.studentmanagement.fragments;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.studentmanagement.R;
import com.example.studentmanagement.activities.AddUserActivity;
import com.example.studentmanagement.activities.UpdateUserActivity;
import com.example.studentmanagement.adapters.UserAdapter;
import com.example.studentmanagement.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserFragment extends Fragment {

    private ImageView addUserBtn;
    private ActivityResultLauncher<Intent> addUserLauncher;
    private ActivityResultLauncher<Intent> editUserLauncher;
    private ArrayList<User> userArrayList;
    private DatabaseReference databaseUsers;
    private UserAdapter userAdapter;
    private RecyclerView userRecyclerView;

    public UserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize the ArrayList and Firebase reference
        userArrayList = new ArrayList<>();
        databaseUsers = FirebaseDatabase.getInstance().getReference("users");

        // Register the ActivityResultLauncher for launching AddUserActivity
        addUserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh user list after a new user is added
                        fetchUsersFromFirebase();
                    }
                }
        );

        // Register the ActivityResultLauncher for launching UpdateUserActivity
        editUserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Refresh user list after a user is edited
                        fetchUsersFromFirebase();
                    }
                }
        );
    }

    private void fetchUsersFromFirebase() {
        databaseUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userArrayList.clear(); // Clear the list to avoid duplication

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getEmail() != null) {
                        userArrayList.add(user); // Add each user to the list
                    }
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors here
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        addUserBtn = view.findViewById(R.id.add_bnt);
        userRecyclerView = view.findViewById(R.id.user_recycler_view);

        // Set up RecyclerView with a LinearLayoutManager and adapter
        userRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext(), userArrayList, user -> {
            // Launch UpdateUserActivity when edit action is triggered
            Intent intent = new Intent(getActivity(), UpdateUserActivity.class);
            intent.putExtra("USER_NAME", user.getName());
            intent.putExtra("USER_EMAIL", user.getEmail());
            intent.putExtra("USER_PHONE", user.getPhone());
            intent.putExtra("USER_ROLE", user.getRole());
            intent.putExtra("USER_STATUS", user.getLocked());
            intent.putExtra("USER_AGE", user.getAge().toString());
            editUserLauncher.launch(intent);
        });

        userRecyclerView.setAdapter(userAdapter);

        addUserBtn.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), AddUserActivity.class);
            addUserLauncher.launch(intent);
        });

        // Fetch users from Firebase
        fetchUsersFromFirebase();

        return view;
    }
}
